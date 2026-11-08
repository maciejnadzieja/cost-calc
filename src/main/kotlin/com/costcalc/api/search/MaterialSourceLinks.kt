package com.costcalc.api.search

import org.springframework.stereotype.Component

@Component
class MaterialSourceLinks(private val properties: ExternalMaterialSearchProperties) {
    fun validate(sourceUrl: String, imageUrl: String?) {
        val page = parseUri(sourceUrl) ?: throw IllegalArgumentException("Invalid material source URL")
        val source = properties.categories.values.asSequence().flatten().distinct()
            .map(properties.sources::getValue).firstOrNull { it.allowsPage(page) }
            ?: throw IllegalArgumentException("Invalid material source URL")
        if (imageUrl != null) {
            val image = parseUri(imageUrl)
            require(image != null && source.allowsOrigin(image) && image.path.orEmpty().startsWith('/')) {
                "Invalid material image URL"
            }
        }
    }
}
