package com.costcalc.api.service

import com.ibm.icu.text.Transliterator
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.util.Locale

/** Filters a user's existing records without relying on a stored search key or locale-specific character table. */
internal object AccentInsensitiveSearch {
    private val transliterator = ThreadLocal.withInitial { Transliterator.getInstance("Any-Latin; Latin-ASCII") }

    fun <T> filterAndPage(
        items: List<T>,
        query: String,
        pageable: Pageable,
        name: (T) -> String,
        description: (T) -> String,
    ): Page<T> {
        val needle = fold(query)
        val content = ArrayList<T>(minOf(pageable.pageSize, items.size))
        var matches = 0L

        for (item in items) {
            if (fold(name(item)).contains(needle) || fold(description(item)).contains(needle)) {
                if (matches >= pageable.offset && content.size < pageable.pageSize) {
                    content.add(item)
                }
                matches++
            }
        }

        return PageImpl(content, pageable, matches)
    }

    private fun fold(value: String): String = transliterator.get().transliterate(value).lowercase(Locale.ROOT)
}
