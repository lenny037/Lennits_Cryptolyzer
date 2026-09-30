# Phone Build Baseline

- Android Gradle Plugin: 9.3.1
- Gradle: 9.5.0
- Kotlin: 2.3.21
- Compose BOM: 2026.06.00
- Room: 2.8.4 baseline pending dependency lock validation
- WorkManager: 2.11.2
- JDK: 17
- NDK: 29.x for the Rust/native layer; pin exact revision during native build validation
- Minimum Android API: 29 for the first supported device class
- Target/compile API: 36 stable baseline

The production app is Android-first and local-first. Cloud services are adapters, not prerequisites for core operation.
