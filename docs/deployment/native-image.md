# Native Image Deployment

hstpd can be built as a native executable using GraalVM Native Image, providing fast startup times, low memory usage, and a single-file deployment.

## Benefits of Native Image

- ⚡ **Fast Startup** - Starts in milliseconds instead of seconds
- 💾 **Low Memory Usage** - Reduced memory footprint
- 📦 **Single File** - No JVM dependencies required
- 🔒 **Security** - Reduced attack surface
- 🐳 **Container Ready** - Perfect for containerized deployments

## Prerequisites

### Install GraalVM

#### Using SDKMAN! (Recommended)
```bash
# Install SDKMAN!
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Install GraalVM
sdk install java 23.0.2-graal
sdk use java 23.0.2-graal

# Install Native Image
gu install native-image
```

#### Manual Installation
1. Download GraalVM from [graalvm.org](https://www.graalvm.org/downloads/)
2. Extract and add to your PATH
3. Install Native Image: `gu install native-image`

### Verify Installation
```bash
# Check Java version
java -version

# Check Native Image
native-image --version
```

## Building Native Image

### Using Build Scripts

#### Linux/macOS
```bash
# Make script executable
chmod +x build-native.sh

# Build native image
./build-native.sh
```

#### Windows
```cmd
# Build native image
build-native.bat
```

### Using Gradle Directly
```bash
# Build the project first
./gradlew build

# Build native image
./gradlew :node:nativeCompile
```

### Build Output
The native executable will be created at:
- **Linux/macOS**: `node/build/native/nativeCompile/hstpd`
- **Windows**: `node/build/native/nativeCompile/hstpd.exe`

## Running the Native Image

### Basic Usage
```bash
# Run with default configuration
./hstpd

# Run with custom configuration
./hstpd --config /path/to/config.yml

# Show help
./hstpd --help

# Show version
./hstpd --version
```

### Performance Comparison

| Metric | JVM | Native Image |
|--------|-----|--------------|
| Startup Time | 2-5 seconds | 50-100ms |
| Memory Usage | 100-200MB | 20-50MB |
| File Size | 40MB JAR | 15-25MB |
| Peak Performance | Same | Same |

## Docker Native Image

### Build Native Docker Image
```bash
# Build using native Dockerfile
docker build -f Dockerfile.native -t hstpd:native .

# Run native container
docker run -p 8080:8080 -p 1883:1883 -p 4001:4001 hstpd:native
```

### Multi-Architecture Builds
```bash
# Build for multiple architectures
docker buildx build --platform linux/amd64,linux/arm64 -f Dockerfile.native -t hstpd:native .
```

## Configuration

### Native Image Configuration Files

The native image build uses several configuration files:

#### Reflection Configuration
`node/src/main/resources/META-INF/native-image/reflect-config.json`
```json
[
  {
    "name": "com.geoknoesis.spatialweb.node.NodeApplication",
    "allDeclaredConstructors": true,
    "allPublicConstructors": true,
    "allDeclaredMethods": true,
    "allPublicMethods": true
  }
]
```

#### Resource Configuration
`node/src/main/resources/META-INF/native-image/resource-config.json`
```json
{
  "resources": {
    "includes": [
      {
        "pattern": "\\QMETA-INF/services/\\E.*"
      },
      {
        "pattern": "\\Qlogback.xml\\E"
      },
      {
        "pattern": "\\Qconfig/\\E.*"
      }
    ]
  }
}
```

### Build Arguments

The native image build includes several optimizations:

```gradle
nativeCompile {
    imageName = "hstpd"
    buildArgs.add("--no-fallback")
    buildArgs.add("--initialize-at-build-time=org.slf4j.LoggerFactory")
    buildArgs.add("--enable-http")
    buildArgs.add("--enable-https")
    buildArgs.add("--enable-all-security-services")
}
```

## Troubleshooting

### Common Issues

#### 1. Missing Native Image
```bash
# Error: native-image command not found
# Solution: Install Native Image
gu install native-image
```

#### 2. Reflection Errors
```bash
# Error: Class not found at runtime
# Solution: Add to reflect-config.json
{
  "name": "com.example.MyClass",
  "allDeclaredConstructors": true,
  "allPublicConstructors": true
}
```

#### 3. Resource Loading Errors
```bash
# Error: Resource not found
# Solution: Add to resource-config.json
{
  "pattern": "\\Qmy-resource.txt\\E"
}
```

#### 4. Build Time Issues
```bash
# Error: Build takes too long
# Solution: Use build cache
./gradlew :node:nativeCompile --build-cache
```

### Debugging Native Image

#### Enable Debug Output
```bash
# Build with debug information
./gradlew :node:nativeCompile -Dnative-image.debug=true
```

#### Analyze Build Report
```bash
# Generate build report
./gradlew :node:nativeCompile -Dnative-image.report=true
```

## Production Deployment

### System Requirements
- **Memory**: 50MB minimum, 100MB recommended
- **CPU**: Any modern x86_64 or ARM64 processor
- **OS**: Linux, macOS, Windows

### Security Considerations
- Native images have a reduced attack surface
- No JVM vulnerabilities
- Smaller memory footprint reduces exposure
- Static linking provides additional security

### Monitoring
```bash
# Monitor memory usage
ps aux | grep hstpd

# Monitor file descriptors
lsof -p $(pgrep hstpd)

# Monitor network connections
netstat -tulpn | grep hstpd
```

## Performance Tuning

### Memory Configuration
```bash
# Set memory limits
export GRAALVM_NATIVE_MEMORY_LIMIT=1g

# Build with memory optimization
./gradlew :node:nativeCompile -Dnative-image.memory-limit=1g
```

### Build Optimization
```bash
# Parallel build
./gradlew :node:nativeCompile --parallel

# Use build cache
./gradlew :node:nativeCompile --build-cache

# Optimize for size
./gradlew :node:nativeCompile -Dnative-image.optimize-for-size=true
```

## CI/CD Integration

### GitHub Actions Example
```yaml
name: Build Native Image

on: [push, pull_request]

jobs:
  build-native:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup GraalVM
        uses: graalvm/setup-graalvm@v1
        with:
          java-version: '23.0.2'
          distribution: 'graalvm'
          native-image: true
      
      - name: Build Native Image
        run: ./build-native.sh
      
      - name: Upload Artifact
        uses: actions/upload-artifact@v3
        with:
          name: hstpd-native
          path: node/build/native/nativeCompile/hstpd
```

## Best Practices

1. **Test Thoroughly** - Native images behave differently than JVM
2. **Monitor Performance** - Use profiling tools to optimize
3. **Update Regularly** - Keep GraalVM and native image updated
4. **Security Scanning** - Scan native binaries for vulnerabilities
5. **Documentation** - Keep build and deployment docs updated

## Support

For native image issues:
- [GraalVM Documentation](https://www.graalvm.org/docs/)
- [Native Image Reference](https://www.graalvm.org/reference-manual/native-image/)
- [GitHub Issues](https://github.com/your-org/spatialwebnode/issues) 