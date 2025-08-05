# Dependency Version Management

This document describes the centralized dependency version management system used in the Spatial Web Node project.

## Overview

The project uses Gradle's **Version Catalogs** feature to centralize all dependency versions in a single location. This provides several benefits:

- **Single Source of Truth**: All versions are defined in one place
- **Easy Updates**: Update a version once and it applies everywhere
- **Consistency**: Ensures all modules use the same versions
- **Maintainability**: Reduces duplication and makes maintenance easier

## Version Catalog File

The centralized version management is defined in `gradle/libs.versions.toml`. This file contains:

### Versions Section
```toml
[versions]
kotlin = "2.1.20"
kotlinx-coroutines = "1.10.2"
junit-jupiter = "5.10.2"
# ... more versions
```

### Libraries Section
```toml
[libraries]
kotlin-stdlib-jdk8 = { module = "org.jetbrains.kotlin:kotlin-stdlib-jdk8" }
kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "kotlinx-coroutines" }
# ... more libraries
```

### Bundles Section
```toml
[bundles]
kotlin-common = [
    "kotlin-stdlib-jdk8",
    "kotlinx-coroutines-core",
    "kotlinx-serialization-json"
]
# ... more bundles
```

### Plugins Section
```toml
[plugins]
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

## Usage in Build Files

### Using Individual Libraries
```gradle
dependencies {
    implementation libs.kotlinx.coroutines.core
    implementation libs.jackson.databind
    testImplementation libs.junit.jupiter
}
```

### Using Bundles (Recommended)
```gradle
dependencies {
    implementation libs.bundles.kotlin.common
    implementation libs.bundles.logging
    implementation libs.bundles.testing
}
```

### Using Plugins
```gradle
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}
```

## Available Bundles

### `kotlin-common`
Common Kotlin dependencies used across modules:
- `kotlin-stdlib-jdk8`
- `kotlinx-coroutines-core`
- `kotlinx-serialization-json`

### `logging`
Logging framework dependencies:
- `slf4j-api`
- `logback-classic`

### `testing`
Testing framework dependencies:
- `junit-jupiter`
- `kotlin-test-junit5`
- `mockito-core`
- `mockito-kotlin`

### `jackson`
Jackson JSON/YAML processing:
- `jackson-databind`
- `jackson-dataformat-yaml`
- `jackson-module-kotlin`

### `ktor-client`
Ktor HTTP client dependencies:
- `ktor-client-cio`
- `ktor-client-websockets`
- `ktor-client-content-negotiation`
- `ktor-serialization-kotlinx-json`

## Current Dependencies

### Core Dependencies
| Library | Version | Purpose |
|---------|---------|---------|
| Kotlin | 2.1.20 | Programming language |
| KotlinX Coroutines | 1.10.2 | Asynchronous programming |
| KotlinX Serialization | 1.6.3 | JSON serialization |
| SLF4J | 2.0.17 | Logging facade |
| Logback | 1.5.18 | Logging implementation |
| Jackson | 2.15.2 | JSON/YAML processing |
| SnakeYAML | 2.0 | YAML parsing |
| Walt-ID DID | 0.15.1 | DID/VC support |
| Koin | 4.1.0 | Dependency injection |

### HTTP Transport
| Library | Version | Purpose |
|---------|---------|---------|
| Ktor Client | 3.2.2 | HTTP client |
| Ktor WebSockets | 3.2.2 | WebSocket support |

### MQTT Transport
| Library | Version | Purpose |
|---------|---------|---------|
| Eclipse Paho | 1.2.5 | MQTT client |
| HiveMQ Client | 1.3.0 | Alternative MQTT client |

### Testing Dependencies
| Library | Version | Purpose |
|---------|---------|---------|
| JUnit Jupiter | 5.10.2 | Testing framework |
| Mockito | 5.8.0 | Mocking framework |
| Mockito Kotlin | 5.2.1 | Kotlin extensions for Mockito |
| Kotlin Test | 2.1.20 | Kotlin testing utilities |

## Adding New Dependencies

### 1. Add Version
Add the version to the `[versions]` section:
```toml
[versions]
new-library = "1.2.3"
```

### 2. Add Library
Add the library to the `[libraries]` section:
```toml
[libraries]
new-library = { module = "com.example:new-library", version.ref = "new-library" }
```

### 3. Add to Bundle (Optional)
If the library is commonly used, add it to an existing bundle or create a new one:
```toml
[bundles]
existing-bundle = [
    "existing-library",
    "new-library"
]
```

### 4. Use in Build File
```gradle
dependencies {
    implementation libs.new.library
    // or
    implementation libs.bundles.existing.bundle
}
```

## Updating Dependencies

### Single Dependency
1. Update the version in `gradle/libs.versions.toml`
2. Run `./gradlew build` to test
3. Check for breaking changes

### Multiple Dependencies
1. Update versions in `gradle/libs.versions.toml`
2. Run `./gradlew build` to test
3. Review changelogs for breaking changes
4. Update code if necessary

## Best Practices

### 1. Use Bundles When Possible
Instead of individual dependencies, use bundles for common groups:
```gradle
// Good
implementation libs.bundles.kotlin.common

// Avoid
implementation libs.kotlin.stdlib.jdk8
implementation libs.kotlinx.coroutines.core
implementation libs.kotlinx.serialization.json
```

### 2. Keep Versions Consistent
Use the same version across related libraries:
```toml
[versions]
jackson = "2.15.2"  # All Jackson libraries use this version

[libraries]
jackson-databind = { module = "com.fasterxml.jackson.core:jackson-databind", version.ref = "jackson" }
jackson-dataformat-yaml = { module = "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml", version.ref = "jackson" }
```

### 3. Document Breaking Changes
When updating major versions, document any breaking changes and required code updates.

### 4. Test After Updates
Always run the full test suite after updating dependencies:
```bash
./gradlew clean build test
```

## Migration from Hardcoded Versions

If you find hardcoded versions in build files, migrate them to the version catalog:

### Before
```gradle
dependencies {
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2"
    implementation "org.slf4j:slf4j-api:2.0.17"
}
```

### After
```gradle
dependencies {
    implementation libs.kotlinx.coroutines.core
    implementation libs.slf4j.api
}
```

## Troubleshooting

### Version Conflicts
If you encounter version conflicts:
1. Check for duplicate dependencies
2. Use `./gradlew dependencies` to see the dependency tree
3. Consider using `resolutionStrategy` in the root build.gradle

### Missing Dependencies
If a dependency is not found:
1. Check if it's defined in `gradle/libs.versions.toml`
2. Verify the module name and version
3. Check if the repository is configured

### Build Failures
If the build fails after updating versions:
1. Check the library's changelog for breaking changes
2. Update code to match the new API
3. Consider using an older version if the new one has issues

## References

- [Gradle Version Catalogs Documentation](https://docs.gradle.org/current/userguide/platforms.html#sec:version-catalog)
- [Gradle Dependency Management](https://docs.gradle.org/current/userguide/dependency_management.html)
- [Kotlin Version Catalog Example](https://kotlinlang.org/docs/gradle.html#gradle-version-catalogs) 