package com.fedachkaa

import com.fedachkaa.api.PhalconRagApiClient
import com.fedachkaa.api.SourceUrlBuilder
import com.fedachkaa.ui.MarkdownRenderer
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.labels.LinkLabel
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JEditorPane

class MyToolWindowFactory : ToolWindowFactory {
    override fun shouldBeAvailable(project: Project) = true

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(project)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    class MyToolWindow(private val project: Project) {
        private val apiClient = PhalconRagApiClient()
        private val markdownRenderer = MarkdownRenderer()
        private var selectedContext: String? = null

        private val content = JBPanel<JBPanel<*>>(BorderLayout(0, 8)).apply {
            border = JBUI.Borders.empty(10)

            val questionField = JBTextField()

            val askButton = JButton(
                MyMessageBundle.message("toolwindow.MyToolWindow.ask.button")
            )

            val addSelectionButton = JButton("Add Selection")

            val contextLabel = JBLabel()

            val answerPane = JEditorPane().apply {
                contentType = "text/html"
                isEditable = false
                isOpaque = false
                border = null
            }

            val actionsPanel = JBPanel<JBPanel<*>>().apply {
                add(addSelectionButton)
                add(askButton)
            }

            val inputPanel = JBPanel<JBPanel<*>>(BorderLayout(8, 0)).apply {
                add(questionField, BorderLayout.CENTER)
                add(actionsPanel, BorderLayout.EAST)
            }

            val questionPanel = JBPanel<JBPanel<*>>(BorderLayout(0, 4)).apply {
                add(inputPanel, BorderLayout.NORTH)
                add(contextLabel, BorderLayout.SOUTH)
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
                handleAsk(questionField, answerPane, sourcesPanel, askButton)
            }

            addSelectionButton.addActionListener {
                handleAddSelection(contextLabel)
            }
        }

        private fun handleAsk(
            questionField: JBTextField,
            answerPane: JEditorPane,
            sourcesPanel: JBPanel<*>,
            askButton: JButton
        ) {
            val question = questionField.text

            answerPane.text = "Loading..."
            sourcesPanel.removeAll()
            sourcesPanel.revalidate()
            sourcesPanel.repaint()

            askButton.isEnabled = false

            ApplicationManager.getApplication().executeOnPooledThread {
                try {
                    val response = apiClient.ask(
                        question = question,
                        context = selectedContext
                    )

                    ApplicationManager.getApplication().invokeLater {
                        answerPane.text = markdownRenderer.render(response.answer)
                        answerPane.caretPosition = 0

                        sourcesPanel.removeAll()

                        if (response.sources.isNotEmpty()) {
                            sourcesPanel.add(JBLabel("Sources"))

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

                                val url = SourceUrlBuilder.build(source)

                                if (url != null) {
                                    val link = LinkLabel<Any>("• $text", null)

                                    link.setListener({ _, _ ->
                                        BrowserUtil.browse(url)
                                    }, null)

                                    sourcesPanel.add(link)
                                } else {
                                    sourcesPanel.add(JBLabel("• $text"))
                                }
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

        private fun handleAddSelection(contextLabel: JBLabel) {
            val editor = FileEditorManager.getInstance(project).selectedTextEditor
            val selectedText = editor?.selectionModel?.selectedText

            if (selectedText.isNullOrBlank()) {
                contextLabel.text = "No code selected"
                return
            }

            selectedContext = selectedText

            val lineCount = selectedText.lines().size
            contextLabel.text = "✓ $lineCount lines of code attached"
        }

        fun getContent(): JBPanel<JBPanel<*>> = content
    }
}
