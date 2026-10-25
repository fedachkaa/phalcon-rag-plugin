# Phalcon RAG — PhpStorm Plugin

A PhpStorm plugin that brings a Phalcon-focused AI assistant directly into the IDE. Ask technical questions about the **Phalcon PHP framework**, optionally attach selected PHP code, and explore answers supported by references to official documentation and framework source code.

This plugin is the IDE client for the [Phalcon RAG backend](https://github.com/fedachkaa/phalcon-rag), a retrieval-augmented generation system built with Python and FastAPI.

> **Project status:** Prototype / portfolio project. The plugin requires a separately running Phalcon RAG API; it is not a standalone or hosted AI service.

## Features

- **Ask from PhpStorm:** Send Phalcon-related questions through a dedicated tool window.
- **Attach selected code:** Select PHP code in the active editor and click **Add Selection** to include it as context for your next question.
- **Grounded answers:** The backend retrieves relevant passages from Phalcon documentation and Zephir source code before generating a response.
- **Source references:** View supporting source files, methods, and documentation sections, with clickable links when supported.
- **Markdown display:** Read formatted answers directly in the IDE.
- **Configurable API endpoint:** Point the plugin to a locally running API or another reachable deployment.

## Demo

See Phalcon RAG Assistant in action:

https://github.com/user-attachments/assets/6614303c-7cce-4686-b5a4-38b705e1b134

## How It Works

```text
PhpStorm editor
   │
   ├── Question
   └── Optional selected PHP code (Add Selection)
                │
                ▼
        Phalcon RAG tool window
                │
                ▼
        POST /api/v1/ask
                │
                ▼
        Phalcon RAG backend
        (retrieval → reranking → generation)
                │
                ▼
        Answer + source metadata
                │
                ▼
        Formatted answer and source links in PhpStorm
```

The plugin sends the question and optional selected-code context to the backend. Retrieval, reranking, and LLM generation happen on the server, not inside PhpStorm.

## Requirements

- **PhpStorm** compatible with the plugin build. The project currently targets **PhpStorm 2026.2.3** in `build.gradle.kts`; compatibility with other versions has not been verified here.
- A running [Phalcon RAG backend](https://github.com/fedachkaa/phalcon-rag) reachable from your computer.
- The backend's required knowledge-base artifacts and API configuration, including an OpenAI API key, as documented in the backend repository.

## Build and Run Locally

Clone this repository:

```bash
git clone https://github.com/fedachkaa/phalcon-rag-plugin.git
cd phalcon-rag-plugin
```

Run a development IDE instance with the plugin:

**Windows (PowerShell):**

```powershell
.\gradlew.bat runIde
```

**Linux / macOS:**

```bash
./gradlew runIde
```

To build a distributable plugin archive:

**Windows (PowerShell):**

```powershell
.\gradlew.bat buildPlugin
```

**Linux / macOS:**

```bash
./gradlew buildPlugin
```

The generated archive is typically placed under `build/distributions/`. You can install a built ZIP in PhpStorm through **Settings → Plugins → ⚙ → Install Plugin from Disk**. This repository does not currently document a JetBrains Marketplace release.

## Configure the Backend URL

1. Start the [Phalcon RAG API](https://github.com/fedachkaa/phalcon-rag) locally or make a deployment reachable from your computer.
2. In PhpStorm, open **Settings → Tools → Phalcon RAG**.
3. Set **API Base URL** to the address of the running backend, for example:

   ```text
   http://127.0.0.1:8000
   ```

4. Apply the settings.

The plugin appends `/api/v1/ask` automatically. **Do not include `/api/v1/ask` in the configured base URL.**

If you use the backend on AWS through an SSH tunnel, keep the tunnel open and use `http://127.0.0.1:8000` as the API base URL. The backend does not need to be publicly exposed.

## Usage

### Ask a Question

1. Open the **Phalcon RAG** tool window in PhpStorm (the current registered tool-window name).
2. Type a question, for example: **How does Phalcon's `getShared()` differ from `get()`?**
3. Click **Ask**.
4. Read the generated answer and inspect its sources.

### Ask About Selected PHP Code

1. Open a PHP file and select the code you want to discuss.
2. In the plugin tool window, click **Add Selection**. A message confirms the number of attached lines.
3. Enter a question, such as **Could this loop cause N+1 queries?**
4. Click **Ask**.

The selection is captured when **Add Selection** is clicked. If you change the selected code, click **Add Selection** again to update the attached context. The current implementation does not provide a dedicated **Clear Selection** action.

### Example API Request

The plugin sends a JSON request to the backend:

```http
POST /api/v1/ask
Content-Type: application/json
```

```json
{
  "question": "Could this code cause an N+1 query problem?",
  "context": "foreach ($users as $user) { /* load user orders */ }"
}
```

The API responds with an `answer` and a `sources` array. The plugin renders the answer and builds source links from returned metadata. The example above is illustrative, not a recorded API response.

## Technology Stack

- **Kotlin** — plugin implementation
- **IntelliJ Platform SDK** — PhpStorm tool window and settings integration
- **Gradle** — build and development IDE tasks
- **Java HTTP Client + Gson** — communication with the RAG API
- **CommonMark** — Markdown rendering
- **Phalcon RAG backend** — FastAPI, hybrid retrieval, reranking, and answer generation

## Repository Structure

```text
src/main/
├── kotlin/
│   ├── MyToolWindowFactory.kt
│   ├── api/
│   │   ├── Models.kt
│   │   ├── PhalconRagApiClient.kt
│   │   └── SourceUrlBuilder.kt
│   ├── config/
│   ├── settings/
│   │   ├── PhalconRagSettings.kt
│   │   └── PhalconRagSettingsConfigurable.kt
│   └── ui/
│       └── MarkdownRenderer.kt
└── resources/
    └── META-INF/
        └── plugin.xml
```

## Current Limitations

- The backend must be started and configured separately.
- There is no chat history or streaming response UI.
- Selected code remains attached until replaced; there is no dedicated clear button.
- Source URL construction targets Phalcon **5.20** repositories; support for other framework versions is not implemented.
- The current API client does not explicitly check HTTP status codes before parsing the response.

## Related Project

**[Phalcon RAG Backend](https://github.com/fedachkaa/phalcon-rag)** — ingestion of Phalcon documentation and source code, hybrid dense/BM25 retrieval, cross-encoder reranking, answer generation, evaluation, and FastAPI deployment.
