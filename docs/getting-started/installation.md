# Installation Guide

This guide covers all the different ways to install and run hstpd on your system.

## Prerequisites

### System Requirements

- **Operating System**: Linux, macOS, or Windows
- **Architecture**: x86_64 or ARM64
- **Memory**: 512MB minimum, 2GB recommended
- **Storage**: 100MB available space
- **Network**: Internet connection for dependencies

### Required Software

#### Java Runtime (JVM Installation)
- **Java 17 or later** (OpenJDK or Oracle JDK)
- **Gradle 8.0 or later** (for building from source)

#### Optional Software
- **Docker** (for containerized deployment)
- **Kubernetes** (for orchestrated deployment)
- **GraalVM** (for native image builds)

## Installation Methods

### Method 1: Build from Source (Recommended)

#### Step 1: Clone the Repository
```bash
# Clone the repository
git clone https://github.com/geoknoesis/spatialwebnode.git
cd spatialwebnode

# Verify the clone
ls -la
```

#### Step 2: Verify Prerequisites
```bash
# Check Java version
java -version

# Check Gradle version
./gradlew --version
```

#### Step 3: Build the Project
```bash
# Build all modules
./gradlew build

# Run tests (optional but recommended)
./gradlew test
```

#### Step 4: Run hstpd
```bash
# Run with default configuration
./gradlew :node:run

# Run with custom configuration
./gradlew :node:run --args="--config config/custom.yml"
```

### Method 2: Native Image Installation

#### Prerequisites for Native Image
```bash
# Install GraalVM (using SDKMAN!)
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 23.0.2-graal
sdk use java 23.0.2-graal

# Install Native Image
gu install native-image
```

#### Build Native Image
```bash
# Using build script
chmod +x build-native.sh
./build-native.sh

# Or using Gradle directly
./gradlew :node:nativeCompile
```

#### Run Native Binary
```bash
# Run the native binary
./node/build/native/nativeCompile/hstpd

# Run with custom configuration
./node/build/native/nativeCompile/hstpd --config config/custom.yml
```

### Method 3: Docker Installation

#### Pull from Docker Hub
```bash
# Pull the latest image
docker pull geoknoesis/hstpd:latest

# Run the container
docker run -p 8080:8080 -p 1883:1883 -p 4001:4001 geoknoesis/hstpd:latest
```

#### Build from Dockerfile
```bash
# Build the image
docker build -t hstpd:latest .

# Run the container
docker run -p 8080:8080 -p 1883:1883 -p 4001:4001 hstpd:latest
```

#### Build Native Docker Image
```bash
# Build native image
docker build -f Dockerfile.native -t hstpd:native .

# Run native container
docker run -p 8080:8080 -p 1883:1883 -p 4001:4001 hstpd:native
```

### Method 4: Package Manager Installation

#### Using Homebrew (macOS)
```bash
# Add the tap (when available)
brew tap geoknoesis/hstpd

# Install hstpd
brew install hstpd

# Run hstpd
hstpd
```

#### Using apt (Ubuntu/Debian)
```bash
# Add repository (when available)
curl -fsSL https://packages.geoknoesis.com/gpg | sudo apt-key add -
echo "deb https://packages.geoknoesis.com/ubuntu focal main" | sudo tee /etc/apt/sources.list.d/geoknoesis.list

# Install hstpd
sudo apt update
sudo apt install hstpd

# Start the service
sudo systemctl start hstpd
sudo systemctl enable hstpd
```

## Platform-Specific Instructions

### Linux Installation

#### Ubuntu/Debian
```bash
# Install dependencies
sudo apt update
sudo apt install openjdk-17-jdk gradle

# Clone and build
git clone https://github.com/geoknoesis/spatialwebnode.git
cd spatialwebnode
./gradlew build
```

#### CentOS/RHEL/Fedora
```bash
# Install dependencies
sudo dnf install java-17-openjdk-devel gradle

# Clone and build
git clone https://github.com/geoknoesis/spatialwebnode.git
cd spatialwebnode
./gradlew build
```

#### Arch Linux
```bash
# Install dependencies
sudo pacman -S jdk-openjdk gradle

# Clone and build
git clone https://github.com/geoknoesis/spatialwebnode.git
cd spatialwebnode
./gradlew build
```

### macOS Installation

#### Using Homebrew
```bash
# Install Java and Gradle
brew install openjdk@17 gradle

# Set JAVA_HOME
echo 'export JAVA_HOME=/opt/homebrew/opt/openjdk@17' >> ~/.zshrc
source ~/.zshrc

# Clone and build
git clone https://github.com/geoknoesis/spatialwebnode.git
cd spatialwebnode
./gradlew build
```

#### Using SDKMAN!
```bash
# Install SDKMAN!
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Install Java and Gradle
sdk install java 17.0.2-open
sdk install gradle 8.5

# Clone and build
git clone https://github.com/geoknoesis/spatialwebnode.git
cd spatialwebnode
./gradlew build
```

### Windows Installation

#### Using Chocolatey
```powershell
# Install Java and Gradle
choco install openjdk17 gradle

# Clone and build
git clone https://github.com/geoknoesis/spatialwebnode.git
cd spatialwebnode
./gradlew build
```

#### Using Scoop
```powershell
# Install Java and Gradle
scoop install openjdk17 gradle

# Clone and build
git clone https://github.com/geoknoesis/spatialwebnode.git
cd spatialwebnode
./gradlew build
```

#### Manual Installation
1. Download OpenJDK 17 from [Adoptium](https://adoptium.net/)
2. Download Gradle from [gradle.org](https://gradle.org/releases/)
3. Set environment variables:
   ```cmd
   set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.2.8-hotspot
   set PATH=%JAVA_HOME%\bin;%PATH%
   ```
4. Clone and build:
   ```cmd
   git clone https://github.com/geoknoesis/spatialwebnode.git
   cd spatialwebnode
   gradlew build
   ```

## Verification

### Check Installation
```bash
# Check version
./gradlew :node:run --args="--version"

# Check help
./gradlew :node:run --args="--help"

# Test basic functionality
./gradlew :node:run --args="--config config/test.yml"
```

### Expected Output
```
hstpd v1.0.0
Developed by Geoknoesis LLC (https://www.geoknoesis.com)
Main Developer: Stephane Fellah (stephanef@geoknoesis.com)
Built with Kotlin 2.1.20
JVM 17.0.2
```

## Troubleshooting

### Common Issues

#### Java Version Issues
```bash
# Error: Java version not found
# Solution: Install Java 17 or later
java -version

# Error: JAVA_HOME not set
# Solution: Set JAVA_HOME environment variable
export JAVA_HOME=/path/to/java
```

#### Gradle Issues
```bash
# Error: Gradle not found
# Solution: Install Gradle or use wrapper
./gradlew --version

# Error: Permission denied
# Solution: Make gradlew executable
chmod +x gradlew
```

#### Build Issues
```bash
# Error: Build fails
# Solution: Clean and rebuild
./gradlew clean build

# Error: Tests fail
# Solution: Run tests individually
./gradlew :core:test
```

#### Network Issues
```bash
# Error: Cannot download dependencies
# Solution: Check internet connection and proxy settings
./gradlew build --info
```

### Getting Help

- **Documentation**: [https://geoknoesis.github.io/spatialwebnode/](https://geoknoesis.github.io/spatialwebnode/)
- **Issues**: [GitHub Issues](https://github.com/geoknoesis/spatialwebnode/issues)
- **Email**: stephanef@geoknoesis.com

## Next Steps

After successful installation:

1. **Configure hstpd**: See [Configuration Guide](configuration.md)
2. **Run First Steps**: See [First Steps Guide](first-steps.md)
3. **Deploy to Production**: See [Deployment Guide](../deployment/README.md)
4. **Join the Community**: [GitHub Discussions](https://github.com/geoknoesis/spatialwebnode/discussions) 