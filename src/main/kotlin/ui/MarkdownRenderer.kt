package com.fedachkaa.ui

import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

class MarkdownRenderer {
    private val parser = Parser.builder().build()
    private val renderer = HtmlRenderer.builder().build()

    fun render(markdown: String): String {
        val document = parser.parse(markdown)
        val html = renderer.render(document)

        return """
            <html>
            <head>
                <style>
                    body {
                        font-family: sans-serif;
                        font-size: 13px;
                        margin: 4px;
                        line-height: 1.3;
                    }

                    p {
                        margin-top: 2px;
                        margin-bottom: 4px;
                    }

                    ol, ul {
                        margin-top: 2px;
                        margin-bottom: 4px;
                        margin-left: 20px;
                    }

                    li {
                        margin-top: 1px;
                        margin-bottom: 1px;
                    }

                    pre {
                        margin-top: 3px;
                        margin-bottom: 5px;
                    }

                    code {
                        font-family: monospace;
                    }
                </style>
            </head>
            <body>
                $html
            </body>
            </html>
        """.trimIndent()
    }
}