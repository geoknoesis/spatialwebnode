@echo off
setlocal enabledelayedexpansion

echo 🏗️  Building hstpd Native Image...

REM Check if GraalVM is installed
where native-image >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ GraalVM Native Image not found!
    echo Please install GraalVM with Native Image support:
    echo https://www.graalvm.org/docs/getting-started/
    exit /b 1
)

REM Check Java version
for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VERSION=%%g
    set JAVA_VERSION=!JAVA_VERSION:"=!
    for /f "tokens=1 delims=." %%v in ("!JAVA_VERSION!") do set JAVA_MAJOR=%%v
)

if %JAVA_MAJOR% lss 17 (
    echo ❌ Java 17 or later is required. Current version: %JAVA_VERSION%
    exit /b 1
)

echo ✅ Java version: %JAVA_VERSION%

REM Clean previous builds
echo 🧹 Cleaning previous builds...
call gradlew clean

REM Build the project
echo 🔨 Building project...
call gradlew build

REM Build native image
echo 🚀 Building native image...
call gradlew :node:nativeCompile

REM Check if build was successful
if exist "node\build\native\nativeCompile\hstpd.exe" (
    echo ✅ Native image built successfully!
    echo 📁 Location: node\build\native\nativeCompile\hstpd.exe
    
    REM Show file size
    for %%A in ("node\build\native\nativeCompile\hstpd.exe") do (
        echo 📏 File size: %%~zA bytes
    )
    
    REM Test the binary
    echo 🧪 Testing binary...
    call "node\build\native\nativeCompile\hstpd.exe" --version
    
    echo 🎉 hstpd native image is ready!
    echo 💡 Run with: node\build\native\nativeCompile\hstpd.exe
    
) else (
    echo ❌ Native image build failed!
    exit /b 1
) 