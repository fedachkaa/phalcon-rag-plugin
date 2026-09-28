package com.fedachkaa

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.JBUI
import javax.swing.JButton
import java.awt.BorderLayout
import java.awt.Dimension
import com.intellij.openapi.application.ApplicationManager
import com.fedachkaa.api.PhalconRagApiClient

class MyToolWindowFactory : ToolWindowFactory {
    override fun shouldBeAvailable(project: Project) = true

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow()
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    class MyToolWindow {
        private val apiClient = PhalconRagApiClient()
        private val content = JBPanel<JBPanel<*>>(BorderLayout(0, 8)).apply {
            border = JBUI.Borders.empty(10)

            val questionField = JBTextField()
            val askButton = JButton(
                MyMessageBundle.message("toolwindow.MyToolWindow.ask.button")
            )

            val answerArea = JBTextArea().apply {
                isEditable = false
                lineWrap = true
                wrapStyleWord = true
            }

            val questionPanel = JBPanel<JBPanel<*>>(BorderLayout(8, 0)).apply {
                add(questionField, BorderLayout.CENTER)
                add(askButton, BorderLayout.EAST)
            }

            val answerScrollPane = JBScrollPane(answerArea)

            add(questionPanel, BorderLayout.NORTH)
            add(answerScrollPane, BorderLayout.CENTER)

            askButton.addActionListener {
                val question = questionField.text

                answerArea.text = "Loading..."
                askButton.isEnabled = false

                ApplicationManager.getApplication().executeOnPooledThread {
                    try {
                        val response = apiClient.ask(question)

                        ApplicationManager.getApplication().invokeLater {
                            val sourcesText = response.sources.joinToString("\n") { source ->
                                "• ${source.file}" +
                                    (source.method?.let { " — $it" } ?: "") +
                                    (source.section?.let { " — $it" } ?: "")
                            }

                            answerArea.text = buildString {
                                append(response.answer)

                                if (response.sources.isNotEmpty()) {
                                    append("\n\nSources:\n")
                                    append(sourcesText)
                                }
                            }

                            askButton.isEnabled = true
                        }
                    } catch (e: Exception) {
                        ApplicationManager.getApplication().invokeLater {
                            answerArea.text = "Error: ${e.message}"
                            askButton.isEnabled = true
                        }
                    }
                }
            }
        }

        fun getContent(): JBPanel<JBPanel<*>> = content
    }
}
