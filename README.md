# MavenQuickBuild

[![JetBrains Plugin](https://img.shields.io/jetbrains/plugin/v/31045-mavenquickbuild)](https://plugins.jetbrains.com/plugin/31045-mavenquickbuild)

[**Install from JetBrains Marketplace**](https://plugins.jetbrains.com/plugin/31045-mavenquickbuild)

<!-- Plugin description -->
An IntelliJ IDEA plugin that adds Maven shortcut buttons to the toolbar for running `mvn clean install` with or without tests.

## Features

Two toolbar buttons are added to the main toolbar and nav bar toolbar:

| Button | Command |
|--------|---------|
| Maven Clean Install (skip tests) | `mvn clean install -Dmaven.test.skip=true` |
| Maven Clean Install | `mvn clean install` |

Both buttons are only enabled when a Maven project is open.

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

## Manual Installation

1. Run `./gradlew buildPlugin`
2. Open IntelliJ IDEA → **Settings → Plugins → Install Plugin from Disk**
3. Select the `.zip` file from `build/distributions/`

## Project Structure

```
src/main/java/it/consciousdreams/
├── MavenCleanInstallAction.java          # mvn clean install -Dmaven.test.skip=true
└── MavenCleanInstallWithTestsAction.java # mvn clean install

src/main/resources/
├── META-INF/plugin.xml                   # Plugin registration & action declarations
└── icons/                                # Toolbar button SVG icons
```

## How It Works

Each action follows this flow:

1. `update()` — enables/shows the button only when a project is open
2. `actionPerformed()` — verifies the project is a Maven project, then builds `MavenRunnerParameters` (goals + working directory) and `MavenRunnerSettings`, and delegates to `MavenRunner.getInstance(project).run()`

The skip-tests variant clones the current `MavenRunnerSettings` and adds `maven.test.skip=true` to a copy of the Maven properties — global settings are never mutated.
<!-- Plugin description end -->