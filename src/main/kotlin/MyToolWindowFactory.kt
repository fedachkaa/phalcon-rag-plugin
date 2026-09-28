package com.fedachkaa

import com.fedachkaa.api.PhalconRagApiClient
import com.fedachkaa.ui.MarkdownRenderer
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JEditorPane

class MyToolWindowFactory : ToolWindowFactory {
    override fun shouldBeAvailable(project: Project) = true

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow()
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    class MyToolWindow {
        private val apiClient = PhalconRagApiClient()
        private val markdownRenderer = MarkdownRenderer()

        private val content = JBPanel<JBPanel<*>>(BorderLayout(0, 8)).apply {
            border = JBUI.Borders.empty(10)

            val questionField = JBTextField()
            val askButton = JButton(
                MyMessageBundle.message("toolwindow.MyToolWindow.ask.button")
            )

            val answerPane = JEditorPane().apply {
                contentType = "text/html"
                isEditable = false
                isOpaque = false
                border = null
            }

            val questionPanel = JBPanel<JBPanel<*>>(BorderLayout(8, 0)).apply {
                add(questionField, BorderLayout.CENTER)
                add(askButton, BorderLayout.EAST)
            }

            val sourcesPanel = JBPanel<JBPanel<*>>().apply {
               layout = BoxLayout(this, BoxLayout.Y_AXIS)
               border = JBUI.Borders.empty(8, 0, 0, 0)
            }

            val resultPanel = JBPanel<JBPanel<*>>(BorderLayout(0, 8)).apply {
                add(JBScrollPane(answerPane), BorderLayout.CENTER)
                add(sourcesPanel, BorderLayout.SOUTH)
            }

            add(questionPanel, BorderLayout.NORTH)
            add(resultPanel, BorderLayout.CENTER)

            askButton.addActionListener {
                val question = questionField.text

                answerPane.text = "Loading..."
                sourcesPanel.removeAll()
                sourcesPanel.revalidate()
                sourcesPanel.repaint()

                askButton.isEnabled = false

                ApplicationManager.getApplication().executeOnPooledThread {
                    try {
                        val response = apiClient.ask(question)

                        ApplicationManager.getApplication().invokeLater {
                            answerPane.text = markdownRenderer.render(response.answer)
                            answerPane.caretPosition = 0

                            sourcesPanel.removeAll()

                            if (response.sources.isNotEmpty()) {
                                sourcesPanel.add(
                                    JBLabel("Sources")
                                )

                                response.sources.forEach { source ->
                                    val text = buildString {
                                        append(source.file)

                                        source.method?.let {
                                            append(" — $it")
                                        }

                                        source.section?.let {
                                            append(" — $it")
                                        }
                                    }

                                    sourcesPanel.add(JBLabel("• $text"))
                                }
                            }

                            sourcesPanel.revalidate()
                            sourcesPanel.repaint()

                            askButton.isEnabled = true
                        }
                    } catch (e: Exception) {
                        ApplicationManager.getApplication().invokeLater {
                            answerPane.text = "Error: ${e.message}"
                            askButton.isEnabled = true
                        }
                    }
                }
            }
        }

        fun getContent(): JBPanel<JBPanel<*>> = content
    }
}
