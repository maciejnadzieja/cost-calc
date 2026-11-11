package com.costcalc.api.search

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.NodeTraversor
import org.jsoup.select.NodeVisitor
import java.io.IOException
import java.net.URI
import java.nio.charset.Charset

internal data class ExtractedMaterial(val name: String, val description: String, val imageUrl: String?)

internal object MaterialHtmlExtractor {
    private val blockedHeading = Regex(
        """^(?:(?:please\s+)?(?:sign\s*in|log\s*in|zaloguj się)|logowanie|access denied|forbidden|page not found|not found|brak dostępu|captcha|verify you are human|checking your browser|just a moment|(?:error|błąd)(?:\s+[45]\d\d)?|[45]\d\d)(?=$|[\s,–—|:!.-])""",
        RegexOption.IGNORE_CASE,
    )
    private val hiddenStyle = Regex("""\b(?:display\s*:\s*none|visibility\s*:\s*hidden)\b""", RegexOption.IGNORE_CASE)
    private val blur = Regex("""\bblur\s*\(""", RegexOption.IGNORE_CASE)
    private val nonPhoto = Regex("""\b(?:logo|icon|sprite|placeholder|avatar|banner|login)\b""", RegexOption.IGNORE_CASE)
    private val restrictedPhoto = Regex("""\b(?:log\s?in|sign\s?in|zaloguj|zalogowaniu|logowania)\b""", RegexOption.IGNORE_CASE)
    private val word = Regex("""[\p{L}\p{N}]{4,}""")
    private val spacing = Regex("""[\s\p{Z}]+""")
    private val contentBreaks = setOf("br", "li", "p", "div")

    fun extract(page: FetchedMaterialPage): ExtractedMaterial {
        val charsetName = page.contentType.split(';').drop(1)
            .firstOrNull { it.substringBefore('=').trim().equals("charset", ignoreCase = true) }
            ?.substringAfter('=', "")?.trim()?.trim('"', '\'')
            ?.let { runCatching { Charset.forName(it).name() }.getOrNull() }
        val document = try {
            Jsoup.parse(page.body.inputStream(), charsetName, page.url.toASCIIString())
        } catch (_: IOException) {
            fail(ExternalMaterialSearchError.UNSUPPORTED_PAGE)
        }
        document.select("aside, button, footer, nav, noscript, script, style, template, [hidden], [aria-hidden=true]").remove()
        document.select("[style]").asSequence().filter { hiddenStyle.containsMatchIn(it.attr("style")) }.forEach(Element::remove)
        val content = document.selectFirst("main") ?: document.selectFirst("article") ?: document.body()
        val name = content.selectFirst("h1")?.text()?.takeIf { it.isNotBlank() } ?: document.title()
        if (name.isBlank() || blockedHeading.containsMatchIn(name) || blockedHeading.containsMatchIn(document.title()) ||
            (content.selectFirst("form input[type=password]") != null &&
                content.selectFirst("p, dl, table") == null)
        ) fail(ExternalMaterialSearchError.UNSUPPORTED_PAGE)

        document.select("form").remove()
        val paragraphs = content.select("p").map(Element::text).filter(String::isNotBlank)
        val description = document.selectFirst("meta[name=description]")?.attr("content")
            ?.let(::plain)?.takeIf(String::isNotBlank)
            ?: paragraphs.firstOrNull { it.length > 35 }
            ?: paragraphs.firstOrNull().orEmpty()
        val fields = content.select("dl, table").asSequence()
            .flatMap { if (it.normalName() == "dl") definitionFields(it) else tableFields(it) }
            .filter { (label, values) -> label.isNotBlank() && values.isNotEmpty() }
            .take(20)
            .joinToString("\n") { (label, values) -> "$label: " + values.joinToString(", ") }
        val combined = listOf(description, fields).filter(String::isNotBlank).joinToString("\n\n")
        val preview = document.selectFirst("meta[property=og:image]")?.attr("content")
        return ExtractedMaterial(name, combined, photo(content, name, preview, page)?.toASCIIString())
    }

    private fun tableFields(table: Element): Sequence<Pair<String, List<String>>> {
        val rows = table.children().flatMap {
            when (it.normalName()) {
                "tr" -> listOf(it)
                "tbody" -> it.children().toList().filter { row -> row.normalName() == "tr" }
                else -> emptyList()
            }
        }
        val cells = rows.map { row -> row.children().toList().filter { it.normalName() in setOf("th", "td") } }
        val labelledPairs = cells.isNotEmpty() && table.children().none { it.normalName() == "thead" } &&
            cells.all { row ->
                row.size == 2 && row.all { it.normalName() == "td" } &&
                    row[0].text().let { it.length in 1..100 && it.first().isLetter() }
            }
        return cells.asSequence().filter { row ->
            row.size >= 2 && row.drop(1).all { it.normalName() == "td" } &&
                (labelledPairs || row[0].normalName() == "th" && row[0].attr("scope") in setOf("", "row")) &&
                row.none { it.selectFirst("table, figure") != null }
        }.map { row -> row[0].text() to row.drop(1).flatMap(::values) }
    }

    private fun definitionFields(list: Element): Sequence<Pair<String, List<String>>> = sequence {
        val terms = mutableListOf<String>()
        val definitions = mutableListOf<String>()
        val items = list.children().flatMap { if (it.normalName() == "div") it.children() else listOf(it) }
        for (item in items) {
            when (item.normalName()) {
                "dt" -> {
                    if (definitions.isNotEmpty()) {
                        for (term in terms) yield(term to definitions.toList())
                        terms.clear()
                        definitions.clear()
                    }
                    terms.add(item.text())
                }
                "dd" -> definitions.addAll(values(item))
            }
        }
        for (term in terms) yield(term to definitions.toList())
    }

    private fun values(element: Element): List<String> {
        val text = StringBuilder()
        NodeTraversor.traverse(object : NodeVisitor {
            override fun head(node: Node, depth: Int) {
                if (node is TextNode) text.append(spacing.replace(node.wholeText, " "))
                if (depth > 0 && node.normalName() in contentBreaks) text.append('\n')
            }

            override fun tail(node: Node, depth: Int) {
                if (depth > 0 && node.normalName() in contentBreaks) text.append('\n')
            }
        }, element)
        return text.lines().map(String::trim).filter(String::isNotBlank)
    }

    private fun photo(content: Element, name: String, preview: String?, page: FetchedMaterialPage): URI? {
        val preferred = preview?.let { imageUri(it, page) }
        val nameWords = words(name)
        val candidates = linkedSetOf<URI>()
        for (image in content.select("img[src]")) {
            val context = image.parents()
            if (blur.containsMatchIn(image.attr("style")) || context.any { blur.containsMatchIn(it.attr("style")) }) continue
            val alt = image.attr("alt")
            val caption = context.firstOrNull { it.normalName() == "figure" }
                ?.selectFirst("figcaption")?.text().orEmpty()
            if (nonPhoto.containsMatchIn(alt) || restrictedPhoto.containsMatchIn("$alt $caption")) continue
            val candidate = imageUri(image.attr("src"), page) ?: continue
            if (candidate == preferred) return candidate
            if (words("$alt $caption").any(nameWords::contains)) candidates.add(candidate)
        }
        return candidates.singleOrNull()
    }

    private fun imageUri(value: String, page: FetchedMaterialPage): URI? =
        parseUri(value.trim())?.let(page.url::resolve)?.takeIf {
            page.source.allowsImage(page.url, it) && !it.path.orEmpty().endsWith(".svg", ignoreCase = true) &&
                !nonPhoto.containsMatchIn(it.path.orEmpty().replace('_', ' '))
        }

    private fun words(text: String): Set<String> = word.findAll(text.lowercase()).map { it.value }.toSet()

    private fun plain(text: String): String = spacing.replace(text, " ").trim()
}
