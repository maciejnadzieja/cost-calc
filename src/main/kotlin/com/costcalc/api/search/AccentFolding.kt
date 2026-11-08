package com.costcalc.api.search

import com.ibm.icu.text.Transliterator
import java.util.Locale

/** Produces a locale-neutral search form without maintaining language-specific character tables. */
object AccentFolding {
    private val transliterator = ThreadLocal.withInitial { Transliterator.getInstance("Any-Latin; Latin-ASCII") }

    fun fold(value: String): String = transliterator.get().transliterate(value).lowercase(Locale.ROOT)
}
