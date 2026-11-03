---
name: setupDependencies task removed in IntelliJ Platform Gradle Plugin 2.19.0
description: Why IntelliJ Gradle tool window throws "Task 'setupDependencies' not found" and how to fix it
type: gotcha
---

The `setupDependencies` Gradle task no longer exists in IntelliJ Platform Gradle Plugin 2.19.0.
Verified: no `setupDependencies` or `prepareDependencies` class in `intellij-platform-gradle-plugin-2.19.0.jar`,
and `./gradlew tasks --all` lists neither.

**Why** — the plugin resolves IntelliJ Platform / bundled-plugin dependencies lazily, so the 1.x-era
"prepare dependencies" task was dropped. A stale IDE-side reference (`.idea/workspace.xml`) then fails
with `Task 'setupDependencies' not found in root project`, surfaced as "Selection failed" in the Gradle tool window.

**How to apply** — the reference lives in two places in `.idea/workspace.xml`:
`ExternalProjectsManager > ... > activation > after_sync > task name="setupDependencies"`, and a
`"Gradle.<project> [setupDependencies].executor": "Run"` history line. Delete both with the IDE closed
(otherwise the IDE rewrites the file on exit). Do not try to re-add the task; nothing needs to be run
manually — `runIde`, `prepareSandbox`, and `compileKotlin` pull platform artifacts themselves.

**Related** — [[gradle-plugin-version]]
