# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Run plugin in a sandboxed IDE instance
./gradlew runIde

# Build distributable plugin zip (output: build/distributions/)
./gradlew buildPlugin

# Compile only
./gradlew compileJava
```

To install manually: **Settings → Plugins → Install Plugin from Disk**, select the `.zip` from `build/distributions/`.

## Architecture

This is an IntelliJ IDEA plugin that adds two Maven shortcut buttons to the toolbar.

### How actions work

Each toolbar button is an `AnAction` subclass registered in `plugin.xml` under `<actions>`. Both actions are added to `MainToolBar` and `NavBarToolBar`.

Execution flow:
1. `update()` — controls button visibility (enabled only when a project is open)
2. `actionPerformed()` — validates the project is a Maven project via `MavenProjectsManager`, then builds `MavenRunnerParameters` (goals list + pomFile) and `MavenRunnerSettings` (properties like `-D` flags), and delegates to `MavenRunner.getInstance(project).run()`

**Important:** The `MavenRunnerParameters` constructor is overloaded. Always cast the pomFile argument explicitly as `(String) null` to avoid ambiguous call compilation errors.

### The two actions

| Class | Icon | Command |
|---|---|---|
| `MavenCleanInstallAction` | `!m` | `mvn clean install -Dmaven.test.skip=true` |
| `MavenCleanInstallWithTestsAction` | `m` | `mvn clean install` |

The skip-tests variant adds `maven.test.skip=true` to a cloned copy of `MavenRunnerSettings.getMavenProperties()` — it never mutates global settings.

### Compatibility

- `sinceBuild` / `untilBuild` in `build.gradle.kts` must be kept in sync with the IDE version in use. Current range: `241` – `261.*`.
- The plugin depends on `org.jetbrains.idea.maven` — only works in IDE distributions that bundle the Maven plugin (IntelliJ IDEA, not all IDEs).
