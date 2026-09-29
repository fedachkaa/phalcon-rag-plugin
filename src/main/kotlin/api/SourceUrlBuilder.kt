package com.fedachkaa.api

object SourceUrlBuilder {
    private const val PHALCON_VERSION = "v5.20.0"
    private const val SOURCE_BASE_URL = "https://github.com/phalcon/cphalcon/blob/$PHALCON_VERSION/phalcon"

    private const val DOCS_BRANCH = "5.20.x"
    private const val DOCS_BASE_URL = "https://github.com/phalcon/documentation/blob/$DOCS_BRANCH/docs"

    fun build(source: SourceResponse): String? {
        return when (source.type) {
            "phalcon_source_code" -> buildSourceCodeUrl(source)
            "phalcon_docs" -> buildDocumentationUrl(source)
            else -> null
        }
    }

    private fun buildSourceCodeUrl(source: SourceResponse): String {
        var url = "$SOURCE_BASE_URL/${source.file}"

        if (source.startLine != null) {
            url += "#L${source.startLine}"

            if (source.endLine != null && source.endLine != source.startLine) {
                url += "-L${source.endLine}"
            }
        }

        return url
    }

    private fun buildDocumentationUrl(source: SourceResponse): String {
        val file = source.file.replace(".mdx", ".md")

        return "$DOCS_BASE_URL/$file"
    }
}