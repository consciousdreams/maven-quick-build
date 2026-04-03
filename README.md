# MavenQuickBuild

[![JetBrains Plugin](https://img.shields.io/jetbrains/plugin/v/31045-mavenquickbuild)](https://plugins.jetbrains.com/plugin/31045-mavenquickbuild)

[**Install from JetBrains Marketplace**](https://plugins.jetbrains.com/plugin/31045-mavenquickbuild)

<!-- Plugin description -->
An IntelliJ IDEA plugin that adds fully configurable Maven shortcut buttons to the toolbar.

## Features

- **Add, edit, or remove** toolbar buttons from **Settings → Tools → Maven Quick Build**
- Set any **Maven goals** per button (e.g. `clean package -Pproduction`)
- Assign **custom keyboard shortcuts** per button directly in the settings panel
- Choose a **built-in icon** or pick any **custom SVG** from your filesystem per button
- Buttons appear in the **MainToolBar** and **NavBarToolBar** — always one click away
- Uses the native **MavenRunner** API — output goes directly to the IDE run console
- Maven properties (e.g. `-Dmaven.test.skip=true`) never mutate global settings

## Default Buttons

| Button | Shortcut (Mac) | Shortcut (Win/Linux) | Command |
|--------|---------------|----------------------|---------|
| Maven Clean Install (skip tests) | `Cmd+Option+S` | `Ctrl+Alt+S` | `mvn clean install -Dmaven.test.skip=true` |
| Maven Clean Install | `Cmd+Option+M` | `Ctrl+Alt+M` | `mvn clean install` |

## Configuration

Open **Settings → Tools → Maven Quick Build** to manage your buttons:

- **Add** — set a label, Maven goals string, icon, and optional keyboard shortcut
- **Edit** — update any field of an existing button
- **Remove** — delete a button from the toolbar
- **Custom SVG** — browse your filesystem to use any SVG file as a button icon
- **Keyboard shortcut** — click the shortcut field and press any key combination; shortcuts are registered with the IDE's Keymap system and can also be changed via **Settings → Keymap**

## Requirements

- IntelliJ IDEA (Community or Ultimate) — builds `241` through `261.*`
- Java 17+
- The project must be a Maven project (recognized by `MavenProjectsManager`)

## Build & Run

```bash
# Run plugin in a sandboxed IDE instance
./gradlew runIde

# Build distributable plugin zip (output: build/distributions/)
./gradlew buildPlugin

# Compile only
./gradlew compileJava
```

## Release & Publish

The project uses GitHub Actions for CI/CD.

### Automatic flow

1. Push to `main` → builds, verifies the plugin, and creates a **draft GitHub Release** with changelog notes
2. Review the draft on GitHub → publish it → the plugin is automatically **signed and published to JetBrains Marketplace**

### Required secrets (GitHub → Settings → Secrets)

| Secret | Description |
|---|---|
| `PUBLISH_TOKEN` | JetBrains Marketplace token (your profile → Tokens) |
| `CERTIFICATE_CHAIN` | Plugin signing certificate chain |
| `PRIVATE_KEY` | Plugin signing private key |
| `PRIVATE_KEY_PASSWORD` | Password for the private key |

> To generate signing keys: `./gradlew signPlugin` (first time only, follow the prompts).

## Manual Installation

1. Run `./gradlew buildPlugin`
2. Open IntelliJ IDEA → **Settings → Plugins → Install Plugin from Disk**
3. Select the `.zip` file from `build/distributions/`

## Project Structure

```
src/main/java/it/consciousdreams/
├── MavenActionConfig.java              # Data model for a toolbar button
├── MavenQuickBuildSettings.java        # Persistent app-level settings service
├── MavenActionsRegistrar.java          # Registers dynamic actions with ActionManager on startup
├── MavenQuickBuildActionGroup.java     # Dynamic toolbar group (reads from settings)
├── DynamicMavenAction.java             # AnAction that runs a configured Maven command
├── MavenQuickBuildConfigurable.java    # Settings UI (Settings → Tools → Maven Quick Build)
└── MavenActionEditDialog.java          # Add/Edit dialog for a single button

src/main/resources/
├── META-INF/plugin.xml                 # Plugin registration
├── META-INF/pluginIcon.svg             # Marketplace / Settings → Plugins logo
└── icons/                              # Built-in toolbar button SVG icons
```

## How It Works

Each toolbar button is a `DynamicMavenAction` instance registered with `ActionManager` under a stable UUID-based ID. On every IDE startup, `MavenActionsRegistrar` syncs the registered actions with the persisted settings and applies keyboard shortcuts to the active keymap.

The `MavenQuickBuildActionGroup` is registered in `plugin.xml` and its `getChildren()` looks up the registered action instances from `ActionManager`, ensuring stable references (important for tooltip display).

Goal strings like `clean install -Dmaven.test.skip=true` are parsed at execution time: tokens starting with `-D` become Maven properties, the rest become the goals list passed to `MavenRunnerParameters`.
<!-- Plugin description end -->
