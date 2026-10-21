# DroidCode — Native Autonomous AI Coding Agent for Android

**DroidCode** brings an autonomous AI software engineering agent workflow (inspired by Claude Code) directly to Android smartphones. Built natively with Kotlin and Jetpack Compose (Material Design 3), it allows developers to create and open projects, edit source code, execute development tools, run shell commands, manage Git repositories, and collaborate with an AI coding agent to inspect, modify, and debug code right on their mobile device.

---

## Key Features

### 1. Autonomous Coding Agent Loop
- **Multi-Step Agentic Workflow**: Inspects projects, gathers context, plans changes, executes authorized tools, verifies results, and provides clear summaries.
- **Safety & Authorization**: Sensitive actions (file writes, non-read-only terminal commands) require user approval by default or can be configured for automatic execution.
- **Unified Diff Review**: All agent file changes generate visual line-by-line diffs (`+` additions, `-` deletions, context lines) before being applied or reverted.

### 2. Universal AI Provider Support (BYOK)
- **OpenAI-Compatible Endpoints**: Works with OpenAI (`gpt-4o`, `gpt-4o-mini`), Groq, DeepSeek, Together, OpenRouter, and self-hosted vLLM.
- **Anthropic-Compatible Endpoints**: Direct support for Claude (`claude-3-7-sonnet`, `claude-3-5-sonnet`) with standard header authentication.
- **Local & Ollama Endpoints**: Connect to local models running on your network or emulator (`http://10.0.2.2:11434/v1`).
- **Live Latency & Connection Tester**: Instant ping test to verify endpoint reachability and credentials.

### 3. Native File Manager & Code Editor
- **Multi-Tab Code Editor**: Line numbers gutter, monospaced typography, dirty state tracking, and fast save.
- **Sandboxed File Operations**: Path-traversal protection prevents escaping project root boundaries.
- **Automatic Backups**: Preserves previous versions in `.droidcode_backup/` prior to every write.
- **Project Templates**: Instant generation of Android/Kotlin, Python CLI, Node.js, Shell, and General codebases.

### 4. Interactive Terminal & Command Execution
- **Android Native Shell (`/system/bin/sh`)**: Direct execution of device binaries (ls, pwd, grep, cat, find, date, uname, etc.) with working directory tracking.
- **Mobile Keyboard Accessories**: Quick tap keys for `Ctrl+C` (Process SIGKILL), `Clear`, `Tab`, `ls -la`, `pwd`, and `git status`.
- **Termux & External Toolchain Bridge**: Clear diagnostics and setup instructions for linking external package managers (apt, pip, cargo, gradle).

### 5. Native Git Version Control
- **Repository Management**: Branch switching, status inspection, and working tree tracking.
- **Staging & Commits**: Stage/unstage individual files, author commits with SHA-1 hashes, and review history logs.
- **Git Diffs**: Inspect changes against HEAD or working tree.

### 6. Build & Diagnostic Workflow
- **Syntax Verification**: Detects unmatched braces, Python indentation errors, and shell script syntax.
- **AI Debugger Integration**: One-click action to pass compiler and build diagnostics directly to the coding agent for automatic repair.

---

## Technical Stack
- **OS Target**: Android 7.0+ (API 24 to API 36)
- **UI Framework**: Jetpack Compose with Material 3 Dynamic Theming
- **Language**: Kotlin 2.2 with Coroutines and StateFlow
- **Network & I/O**: OkHttp with SSE streaming, Moshi, DataStore Preferences
- **Testing**: Robolectric JVM unit tests and JUnit4
