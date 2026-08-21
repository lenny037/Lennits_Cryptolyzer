# Canonical Repository Reconstruction

The repository is being rebuilt around an executable Android-first source tree. Legacy archives remain historical inputs and are not runtime dependencies.

```text
android/
  app/
    src/main/java/com/lennit/cryptolyzer/
      domain/events/        # domain event contracts
      persistence/          # local durable state adapters
      runtime/              # process lifecycle
      blockchain/           # M08 observation boundary
      intelligence/         # M00/M14/M15 signal fabric
      prediction/           # M05 prediction boundary
    src/test/               # JVM tests
  build.gradle.kts
  settings.gradle.kts
  gradle.properties
```

## Reconstruction rule

A directory is canonical only when its source is executable, tested where applicable, and represented by the Android build. Documentation and archive artifacts are retained as references but do not define runtime behavior.

## Current implementation boundary

The Android project now has a real Gradle application root, application module, manifest, entry activity, lifecycle component, local SQLite database foundation, event contracts, blockchain observation contract, intelligence signal contract, and prediction contract.

Remaining reconstruction is driven by build validation and phase requirements rather than by recreating historical file names verbatim.
