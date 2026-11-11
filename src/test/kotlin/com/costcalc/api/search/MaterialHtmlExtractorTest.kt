package com.costcalc.api.search

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.net.URI

class MaterialHtmlExtractorTest {
    @Test
    fun extractsCatalogueDescriptionAndSeparateTableValues() {
        val details = extract(
            "catalog.example",
            """
            <title>Plant catalogue</title>
            <meta name="description" content="Krzew o białych kwiatach.">
            <meta property="og:image" content="/api/image.php?id=20635">
            <nav><h1>Catalogue menu</h1></nav>
            <main><h1>Spiraea alba</h1>
              <img alt="" src="/api/image.php?id=20635">
              <table><tr><td>Nasłonecznienie</td><td>cień<br>półcień<br>słońce</td></tr></table>
              <table><tr><td>Strefa</td><td>Temperatura</td></tr><tr><td>5B</td><td>-26°C</td></tr></table>
            </main>
            """,
        )

        assertEquals("Spiraea alba", details.name)
        assertEquals("Krzew o białych kwiatach.\n\nNasłonecznienie: cień, półcień, słońce", details.description)
        assertEquals("https://catalog.example/api/image.php?id=20635", details.imageUrl)
    }

    @Test
    fun extractsTitleAndDefinitionListWhileSkippingRestrictedPreviews() {
        val details = extract(
            "atlas.example",
            """
            <title>Spiraea alba (tawuła biała)</title>
            <header><h1>Site title</h1></header>
            <main><p hidden>Hidden paragraph with misleading material information.</p>
              <p>Publiczny opis krzewu i warunków, w jakich rośnie.</p>
              <dl><dt>Gleba<dd><ul><li>gliniasta<li>piaszczysta</ul><dd>przepuszczalna</dl>
              <figure><img style="filter:blur(20px)" alt="Spiraea alba" src="/photo.jpg">
                <figcaption>Spiraea alba</figcaption></figure>
              <img alt="Spiraea alba" src="https://unlisted.example/photo.jpg">
            </main>
            """,
        )

        assertEquals("Spiraea alba (tawuła biała)", details.name)
        assertEquals("Publiczny opis krzewu i warunków, w jakich rośnie.\n\nGleba: gliniasta, piaszczysta, przepuszczalna",
            details.description)
        assertNull(details.imageUrl)
    }

    @Test
    fun rejectsLoginPagesAndKeepsOverlengthDescriptionForReview() {
        for (heading in listOf("Logowanie | Atlas", "Sign in to continue", "Verify you are human")) {
            val error = assertThrows(ExternalMaterialSearchException::class.java) {
                extract("atlas.example", "<main><h1>$heading</h1><p>Please authenticate.</p><form><input type=password></form></main>")
            }
            assertEquals(ExternalMaterialSearchError.UNSUPPORTED_PAGE, error.error)
        }
        val longText = "x".repeat(501)
        val details = extract("atlas.example", "<main><h1>Plant</h1><p>$longText</p></main>")
        assertEquals(longText, details.description)
    }

    @Test
    fun rejectsAnImageOnAnotherHostEvenIfItIsTheWwwAliasOfTheSource() {
        val details = extract("catalog.example", """
            <main><h1>Spiraea alba</h1><img alt="Spiraea alba" src="https://www.catalog.example/photo.jpg"></main>
        """)
        assertNull(details.imageUrl)
    }

    private fun extract(domain: String, html: String) = MaterialHtmlExtractor.extract(
        FetchedMaterialPage(
            URI("https://$domain/plant/1"),
            ExternalMaterialSource("Plant catalogue", domain, "/plant/[0-9]+"),
            "text/html; charset=UTF-8",
            html.toByteArray(),
        ),
    )
}
