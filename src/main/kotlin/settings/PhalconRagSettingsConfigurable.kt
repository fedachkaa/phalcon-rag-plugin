package com.fedachkaa.settings

import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

class PhalconRagSettingsConfigurable : Configurable {

    private val settings = PhalconRagSettings.getInstance()
    private val apiBaseUrlField = JBTextField()

    override fun getDisplayName(): String = "Phalcon RAG"

    override fun createComponent(): JComponent {
        apiBaseUrlField.text = settings.state.apiBaseUrl

        return panel {
            row("API Base URL:") {
                cell(apiBaseUrlField)
            }
        }
    }

    override fun isModified(): Boolean {
        return apiBaseUrlField.text != settings.state.apiBaseUrl
    }

    override fun apply() {
        settings.state.apiBaseUrl = apiBaseUrlField.text.trimEnd('/')
    }

    override fun reset() {
        apiBaseUrlField.text = settings.state.apiBaseUrl
    }
}