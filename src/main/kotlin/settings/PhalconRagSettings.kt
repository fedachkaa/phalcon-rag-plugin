package com.fedachkaa.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service

@Service(Service.Level.APP)
@State(
    name = "PhalconRagSettings",
    storages = [Storage("PhalconRagSettings.xml")]
)
class PhalconRagSettings : PersistentStateComponent<PhalconRagSettings.State> {

    data class State(
        var apiBaseUrl: String = "http://localhost:8000"
    )

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    companion object {
        fun getInstance(): PhalconRagSettings =
            service()
    }
}