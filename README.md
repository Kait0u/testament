# Testament

The last word on your tests.

Testament is a Gradle plugin that prints a consolidated, cross-module summary of your
test execution at the very end of every build — including the failures and why they failed.

```
==========================================================================
 TESTAMENT
==========================================================================
 app    4 passed, 1 skipped, 0 failed
 core   3 passed, 0 skipped, 0 failed
--------------------------------------------------------------------------
 TOTAL  7 passed, 1 skipped, 0 failed (8 tests)
==========================================================================
```

When tests fail, their reasons are listed right there at the bottom:

```
 FAILURES
--------------------------------------------------------------------------
 [app] dev.kaitou.sample.app.AppServiceTest.occasionallyFails
     reason: expected: <4> but was: <0>
--------------------------------------------------------------------------
```

## Requirements

- Gradle 9.0+
- Java 17+ to run the build

## Usage

```kotlin
plugins {
    id("pl.kaitoudev.testament") version "0.1.0"
}
```

Apply the plugin to the root project of a multi-project build — it wires itself into every
`Test` task in every module. Nothing else to configure.

The summary is printed at the end of the build, whether it succeeded or failed. If no
tests ran, nothing is printed.

## How it works

Testament is built entirely on modern Gradle APIs:

- Test results are recorded by a `TestListener` on every `Test` task — the replacement
  Gradle points to for the removed `testLogging.afterSuite`/`afterTest` closures.
- Results accumulate in a shared build service, scoped to a single build invocation and
  safe for parallel module execution.
- The summary is printed by a [Flow action][flow] (`FlowScope.always`), the replacement
  for the deprecated `buildFinished` hook.

The plugin is compatible with the configuration cache and triggers no deprecation
warnings on Gradle 9.

## Sample project

The [`sample/`](sample) directory contains a small two-module build that consumes the
plugin through a composite build:

```bash
cd sample
./gradlew test                        # all green
./gradlew test -Ptestament.demo.fail=yes   # demo a failure with its reason
```

## Development

```bash
./gradlew build               # unit + TestKit functional tests
./gradlew publishToMavenLocal # try it out in other builds
./gradlew publishPlugins      # publish to the Gradle Plugin Portal
```

## License

[MIT](LICENSE)

[flow]: https://docs.gradle.org/current/userguide/dataflow_actions.html
