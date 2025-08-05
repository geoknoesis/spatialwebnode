# Getting Started with hstpd

Welcome to hstpd! This guide will help you get up and running with the HSTP (Hypermedia Spatial Transport Protocol) daemon.

## Prerequisites

Before you begin, ensure you have the following installed:

- **Java 17** or later
- **Gradle 8.0** or later (or use the included Gradle wrapper)
- **Git** for cloning the repository

### Installing Java 17

#### Ubuntu/Debian
```bash
sudo apt update
sudo apt install openjdk-17-jdk
```

#### macOS
```bash
brew install openjdk@17
```

#### Windows
Download from [Oracle](https://www.oracle.com/java/technologies/downloads/#java17) or use [AdoptOpenJDK](https://adoptopenjdk.net/).

### Installing Gradle

#### Using SDKMAN! (Linux/macOS)
```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install gradle 8.13
```

#### Manual Installation
Download from [Gradle releases](https://gradle.org/releases/) and add to your PATH.

## Quick Installation

### 1. Clone the Repository

```bash
git clone https://github.com/your-org/spatialwebnode.git
cd spatialwebnode
```

### 2. Build the Project

```bash
# Using the included Gradle wrapper
./gradlew build

# Or using system Gradle
gradle build
```

### 3. Run hstpd

```bash
# Run with default configuration
./gradlew :node:run

# Run with custom configuration
./gradlew :node:run --args="--config config/custom.yml"
```

## What's Next?

- **[Installation Guide](installation.md)** - Detailed installation instructions
- **[Configuration Guide](configuration.md)** - Setting up your first configuration
- **[First Steps](first-steps.md)** - Your first HSTP operations

## Troubleshooting

If you encounter issues during installation:

1. **Java Version**: Ensure you're using Java 17 or later
   ```bash
   java -version
   ```

2. **Gradle Version**: Check your Gradle version
   ```bash
   ./gradlew --version
   ```

3. **Build Issues**: Clean and rebuild
   ```bash
   ./gradlew clean build
   ```

4. **Permission Issues**: Make the Gradle wrapper executable
   ```bash
   chmod +x gradlew
   ```

For more help, see the [Troubleshooting Guide](../deployment/troubleshooting.md) or [open an issue](https://github.com/your-org/spatialwebnode/issues). 