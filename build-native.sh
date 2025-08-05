#!/bin/bash

# Build hstpd Native Image
# This script builds a native executable using GraalVM Native Image

set -e

echo "🏗️  Building hstpd Native Image..."

# Check if GraalVM is installed
if ! command -v native-image &> /dev/null; then
    echo "❌ GraalVM Native Image not found!"
    echo "Please install GraalVM with Native Image support:"
    echo "https://www.graalvm.org/docs/getting-started/"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt "17" ]; then
    echo "❌ Java 17 or later is required. Current version: $JAVA_VERSION"
    exit 1
fi

echo "✅ Java version: $(java -version 2>&1 | head -n 1)"

# Clean previous builds
echo "🧹 Cleaning previous builds..."
./gradlew clean

# Build the project
echo "🔨 Building project..."
./gradlew build

# Build native image
echo "🚀 Building native image..."
./gradlew :node:nativeCompile

# Check if build was successful
if [ -f "node/build/native/nativeCompile/hstpd" ]; then
    echo "✅ Native image built successfully!"
    echo "📁 Location: node/build/native/nativeCompile/hstpd"
    
    # Show file size
    FILE_SIZE=$(du -h node/build/native/nativeCompile/hstpd | cut -f1)
    echo "📏 File size: $FILE_SIZE"
    
    # Test the binary
    echo "🧪 Testing binary..."
    ./node/build/native/nativeCompile/hstpd --version
    
    echo "🎉 hstpd native image is ready!"
    echo "💡 Run with: ./node/build/native/nativeCompile/hstpd"
    
else
    echo "❌ Native image build failed!"
    exit 1
fi 