# Centralized Build Structure

This document describes the centralized build configuration structure used in the Spatial Web Node project.

## Overview

The project uses a centralized build configuration approach where all module-specific build configurations are stored in the `gradle/build/` directory, and all compiled outputs are organized in the root `build/` directory. This provides several benefits:

- **Centralized Configuration**: All build logic is in one location
- **Centralized Build Outputs**: All compiled code is organized in the root build directory
- **Easy Maintenance**: Changes to build configuration can be made in one place
- **Consistency**: Ensures all modules follow the same build patterns
- **Reduced Duplication**: Eliminates repetitive build configuration code
- **Clean Organization**: Build outputs are organized by module in a structured way

## Directory Structure

```
gradle/
├── libs.versions.toml          # Centralized dependency versions
└── build/                      # Centralized build configurations
    ├── common.gradle          # Common build configuration
    ├── common-utils.gradle    # Common utilities module config
    ├── core.gradle            # Core module config
    ├── identity.gradle        # Identity module config
    ├── node.gradle            # Node application config
    ├── transport.gradle       # Transport parent module config
    ├── transport-http.gradle  # HTTP transport config
    ├── transport-mqtt.gradle  # MQTT transport config
    └── transport-p2p.gradle   # P2P transport config

build/                          # Centralized build outputs
├── modules/                    # Module-specific build outputs
│   ├── common-utils/          # Common utilities build output
│   ├── core/                  # Core module build output
│   ├── identity/              # Identity module build output
│   ├── node/                  # Node application build output
│   ├── transport/             # Transport parent build output
│   ├── http/                  # HTTP transport build output
│   ├── mqtt/                  # MQTT transport build output
│   └── p2p/                   # P2P transport build output
├── libs/                      # Root project libraries
├── reports/                   # Build reports
└── tmp/                       # Temporary files
```

## Module Build Files

Each module's `build.gradle` file now follows a simple pattern:

```gradle
plugins {
    id 'java'
    alias(libs.plugins.kotlin.jvm)
    // ... other plugins
}

apply from: '../gradle/build/module-name.gradle'
```

### Example: Core Module

**`core/build.gradle`:**
```gradle
plugins {
    id 'java'
    alias(libs.plugins.kotlin.jvm)
}

apply from: '../gradle/build/core.gradle'
```

**`gradle/build/core.gradle`:**
```gradle
apply from: "${rootProject.projectDir}/gradle/build/common.gradle"

group = 'com.geoknoesis.spatialweb'
version = '1.0.0'

repositories {
    maven { url 'https://maven.waltid.dev/releases'}
}

dependencies {
    implementation libs.bundles.kotlin.common
    implementation libs.waltid.did
    implementation libs.bundles.logging

    implementation project(':identity')
    implementation project(':common-utils')

    testImplementation libs.bundles.testing
}
```

**`gradle/build/common.gradle`:**
```gradle
// Common build configuration for all modules
// This file centralizes build outputs to the root build directory

// Configure build outputs to be organized in the root build directory
buildDir = file("${rootProject.buildDir}/modules/${project.name}")

// Configure Java source sets
java {
    sourceCompatibility = '17'
    targetCompatibility = '17'
}

// Configure Kotlin to use centralized build (only if Kotlin plugin is applied)
plugins.withId('org.jetbrains.kotlin.jvm') {
    kotlin {
        jvmToolchain(17)
    }
}

// Configure test output
test {
    useJUnitPlatform()
    reports {
        html.required = true
        junitXml.required = true
    }
}

// Configure jar task
jar {
    archiveBaseName = project.name
    archiveVersion = project.version
}

// Configure all modules to use the same build directory structure
tasks.withType(JavaCompile) {
    options.encoding = 'UTF-8'
    options.compilerArgs += ['-Xlint:unchecked', '-Xlint:deprecation']
}

// Configure repositories for all modules
repositories {
    mavenCentral()
    maven { url 'https://maven.waltid.dev/releases' }
    maven { url 'https://jitpack.io' }
}
```

## Available Build Configurations

### `common-utils.gradle`
- **Purpose**: Common utilities module
- **Dependencies**: None
- **Plugins**: Java only

### `core.gradle`
- **Purpose**: Core HSTP engine and operations
- **Dependencies**: Identity, common-utils, Kotlin, logging, DID
- **Plugins**: Java, Kotlin JVM

### `identity.gradle`
- **Purpose**: DID and credential management
- **Dependencies**: Common-utils, Kotlin, serialization, DID, logging
- **Plugins**: Java, Kotlin JVM, Kotlin Serialization

### `node.gradle`
- **Purpose**: Main application node
- **Dependencies**: Core, transport, identity, all bundles
- **Plugins**: Java, Kotlin JVM, Application

### `transport.gradle`
- **Purpose**: Transport parent module
- **Dependencies**: None
- **Plugins**: Java only

### `transport-http.gradle`
- **Purpose**: HTTP transport implementation
- **Dependencies**: Core, identity, Ktor client, logging, Jackson
- **Plugins**: Java, Kotlin JVM

### `transport-mqtt.gradle`
- **Purpose**: MQTT transport implementation
- **Dependencies**: Core, identity, common-utils, MQTT clients, logging, Jackson
- **Plugins**: Java, Kotlin JVM, Kotlin Serialization

### `transport-p2p.gradle`
- **Purpose**: P2P transport implementation
- **Dependencies**: Core, identity, common-utils, logging, Jackson
- **Plugins**: Java, Kotlin JVM, Kotlin Serialization

## Benefits of This Approach

### 1. **Centralized Configuration**
All build logic is stored in one location, making it easy to find and modify.

### 2. **Centralized Build Outputs**
All compiled code is organized in the root `build/` directory, making it easy to locate and manage build artifacts.

### 3. **Reduced Duplication**
Common configurations (like repositories, Java version, group/version) are defined once and reused.

### 4. **Consistent Patterns**
All modules follow the same build patterns, ensuring consistency across the project.

### 5. **Easy Maintenance**
Changes to build configuration can be made in one place and automatically apply to all modules.

### 6. **Clear Separation**
Plugin declarations are kept in module build files, while configuration is centralized.

### 7. **Organized Build Outputs**
Each module's build output is organized in its own subdirectory under `build/modules/`, making it easy to find specific artifacts.

### 8. **Clean Project Structure**
No build directories scattered throughout the project - everything is centralized in the root build directory.

## Adding New Modules

### 1. Create Centralized Config
Create a new file in `gradle/build/`:

```gradle
// gradle/build/new-module.gradle
group = 'com.geoknoesis.spatialweb'
version = '1.0.0'

java {
    sourceCompatibility = '17'
}

repositories {
    mavenCentral()
}

dependencies {
    implementation libs.bundles.kotlin.common
    implementation libs.bundles.logging
    
    // Module-specific dependencies
    implementation project(':core')
    
    testImplementation libs.bundles.testing
}
```

### 2. Create Module Build File
Create the module's `build.gradle`:

```gradle
// new-module/build.gradle
plugins {
    id 'java'
    alias(libs.plugins.kotlin.jvm)
}

apply from: '../gradle/build/new-module.gradle'
```

### 3. Update Settings
Add the module to `settings.gradle`:

```gradle
include ':new-module'
```

## Modifying Existing Configurations

### Adding Dependencies
To add a dependency to all modules of a certain type:

1. **Update the centralized config file** in `gradle/build/`
2. **Rebuild the project** to apply changes

### Changing Versions
To update dependency versions:

1. **Update `gradle/libs.versions.toml`**
2. **Rebuild the project** to apply changes

### Adding New Build Logic
To add new build logic (like custom tasks):

1. **Add to the appropriate centralized config file**
2. **Rebuild the project** to apply changes

## Best Practices

### 1. **Keep Plugin Declarations in Module Files**
Plugin declarations should remain in the module's `build.gradle` file for clarity.

### 2. **Use Descriptive File Names**
Centralized config files should have descriptive names that clearly indicate their purpose.

### 3. **Group Related Configurations**
Related configurations should be grouped together in the same centralized file.

### 4. **Document Changes**
When modifying centralized configurations, document the changes and their impact.

### 5. **Test After Changes**
Always test the build after making changes to centralized configurations.

## Troubleshooting

### Build Failures
If the build fails after modifying centralized configurations:

1. **Check syntax** in the centralized config file
2. **Verify file paths** in `apply from:` statements
3. **Ensure plugins are declared** in module build files
4. **Check for circular dependencies**

### Missing Dependencies
If dependencies are not found:

1. **Verify the dependency is defined** in `gradle/libs.versions.toml`
2. **Check the centralized config file** includes the dependency
3. **Ensure repositories are configured** correctly

### Plugin Issues
If plugins are not working:

1. **Verify plugin declaration** in module build file
2. **Check plugin version** in `gradle/libs.versions.toml`
3. **Ensure plugin is applied** correctly

## Migration Guide

### From Traditional Build Files
To migrate from traditional build files to centralized configuration:

1. **Create centralized config file** in `gradle/build/`
2. **Move configuration logic** to centralized file
3. **Update module build file** to use `apply from:`
4. **Test the build** to ensure everything works

### Example Migration

**Before:**
```gradle
// core/build.gradle
plugins {
    id 'java'
    id 'org.jetbrains.kotlin.jvm' version '2.1.20'
}

group = 'com.geoknoesis.spatialweb'
version = '1.0.0'

dependencies {
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk8"
    implementation "org.slf4j:slf4j-api:2.0.17"
    // ... more dependencies
}
```

**After:**
```gradle
// core/build.gradle
plugins {
    id 'java'
    alias(libs.plugins.kotlin.jvm)
}

apply from: '../gradle/build/core.gradle'
```

```gradle
// gradle/build/core.gradle
group = 'com.geoknoesis.spatialweb'
version = '1.0.0'

dependencies {
    implementation libs.bundles.kotlin.common
    implementation libs.bundles.logging
    // ... more dependencies
}
```

## References

- [Gradle Build Scripts](https://docs.gradle.org/current/userguide/writing_build_scripts.html)
- [Gradle Version Catalogs](https://docs.gradle.org/current/userguide/platforms.html#sec:version-catalog)
- [Gradle Apply Plugin](https://docs.gradle.org/current/userguide/plugins.html#sec:applying_plugins) 