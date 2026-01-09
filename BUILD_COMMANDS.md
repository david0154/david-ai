# 🔨 DAVID AI - Complete Build Commands\n\n## Prerequisites\n\n```bash\n# Install Android SDK (if not already installed)\nDownload: https://developer.android.com/studio\n
# Install Java Development Kit (JDK 11+)\nDebian/Ubuntu: sudo apt-get install openjdk-11-jdk\nMacOS: brew install openjdk@11\nWindows: Download from https://adoptopenjdk.net/\n```\n\n---\n\n## Build Commands\n\n### 1. Build the Project\n```bash\n# Debug build\n./gradlew build\n
# Release build\n./gradlew build -Pbuild_type=release\n```\n\n### 2. Build APK\n```bash\n# Debug APK\n./gradlew assembleDebug\n
# Release APK\n./gradlew assembleRelease\n
# All APKs\n./gradlew assemble\n```\n\n### 3. Build App Bundle (For Play Store)\n```bash\n# Debug bundle\n./gradlew bundleDebug\n
# Release bundle\n./gradlew bundleRelease\n```\n\n### 4. Run Tests\n```bash\n# Unit tests\n./gradlew test\n
# UI tests\n./gradlew connectedAndroidTest\n
# All tests\n./gradlew check\n```\n\n### 5. Install on Device\n```bash\n# Install debug APK\n./gradlew installDebug\n
# Install and run\n./gradlew installDebug\nadb shell am start -n com.davidstudioz.david/.MainActivity\n
# Uninstall\n./gradlew uninstallDebug\n```\n\n### 6. View Build Info\n```bash\n# Get signing report (SHA-1 for Firebase)\n./gradlew signingReport\n
# Check dependencies\n./gradlew dependencies\n
# Build info\n./gradlew buildEnvironment\n```\n\n### 7. Clean Build\n```bash\n# Clean previous build\n./gradlew clean\n
# Clean and rebuild\n./gradlew clean build\n```\n\n### 8. Code Quality Checks\n```bash\n# Lint check\n./gradlew lint\n
# Detekt (Kotlin linter)\n./gradlew detekt\n
# Ktlint (Code formatting)\n./gradlew ktlint\n```\n\n### 9. Generate Documentation\n```bash\n# Generate Javadoc/Dokka\n./gradlew dokka\n
# Generate HTML docs\n./gradlew dokkaHtml\n```\n\n### 10. Performance Optimization\n```bash\n# Build with optimizations\n./gradlew build -Dorg.gradle.workers.max=8\n
# Parallel builds\n./gradlew build --parallel\n
# Build cache\n./gradlew build --build-cache\n```\n\n---\n\n## Advanced Build Commands\n\n### Build with Specific Variant\n```bash\n# Build specific flavor\n./gradlew assembleProduction\n./gradlew assembleDevelopment\n
# Build specific variant\n./gradlew assembleProductionRelease\n```\n\n### Build with Custom Properties\n```bash\n# Set custom version\n./gradlew build -Pversion_code=100 -Pversion_name=\"2.0.0\"\n
# Set custom package name\n./gradlew build -Ppackage_name=\"com.example.david\"\n```\n\n### Generate APK Reports\n```bash\n# APK size analysis\n./gradlew analyzeApkSize\n
# Build report\n./gradlew htmlDependencyReport\n```\n\n### Continuous Integration\n```bash\n# CI build (no daemon)\n./gradlew build --no-daemon\n
# Build with logging\n./gradlew build --info\n./gradlew build --debug\n```\n\n---\n\n## Release Build Steps\n\n### 1. Configure Keystore\n```bash\n# Generate keystore (one time)\nkeytool -genkey -v -keystore my.keystore -keyalg RSA -keysize 2048 -validity 10000\n
# Store path in gradle.properties\necho \"KEYSTORE_PATH=./my.keystore\" >> gradle.properties\necho \"KEYSTORE_PASSWORD=your_password\" >> gradle.properties\necho \"KEY_ALIAS=my_key\" >> gradle.properties\necho \"KEY_PASSWORD=your_key_password\" >> gradle.properties\n```\n\n### 2. Build Release APK\n```bash\n./gradlew assembleRelease\n```\n\n### 3. Sign APK\n```bash\njarsigner -verbose -sigalg SHA-256withRSA -digestalg SHA-256 \\\n  -keystore my.keystore app/build/outputs/apk/release/app-release-unsigned.apk my_key\n```\n\n### 4. Align APK\n```bash\nzipalign -v 4 app-release.apk app-release-aligned.apk\n```\n\n### 5. Verify Signature\n```bash\njarsigner -verify -verbose -certs app-release-aligned.apk\n```\n\n---\n\n## ADB Commands (After Build)\n\n### Device Management\n```bash\n# List connected devices\nadb devices\n
# Reboot device\nadb reboot\n
# Access shell\nadb shell\n```\n\n### App Management\n```bash\n# Install APK\nadb install app/build/outputs/apk/debug/app-debug.apk\n
# Uninstall app\nadb uninstall com.davidstudioz.david\n
# Clear app data\nadb shell pm clear com.davidstudioz.david\n
# Get app info\nadb shell dumpsys package com.davidstudioz.david\n```\n\n### Debugging\n```bash\n# View logcat\nadb logcat\n
# Filter logs by tag\nadb logcat | grep DAVID\n
# Save logs to file\nadb logcat > app_logs.txt\n
# Clear logcat\nadb logcat -c\n```\n\n### File Management\n```bash\n# Push file to device\nadb push local_file /sdcard/\n
# Pull file from device\nadb pull /sdcard/file local_file\n
# List files\nadb shell ls -la /data/data/com.davidstudioz.david/\n```\n\n---\n\n## Gradle Properties\n\n### gradle.properties\n```properties\n# SDK Versions\nandroid.useAndroidX=true\nandroid.enableJetifier=true\n
# Build Features\nandroid.useNewApkStructure=true\nandroid.gradle.parallel=true\nandroid.maxProcessCount=8\n
# Memory Settings\norg.gradle.jvmargs=-Xmx4096m -XX:MaxPermSize=512m\n
# Build Optimization\nandroid.enableSeparateCompilation=true\nandroid.enableD8=true\n```\n\n---\n\n## Build Troubleshooting\n\n### Common Issues\n
**1. Build Fails with Out of Memory\\n**Solution:** Increase heap size\n```bash\nexport GRADLE_OPTS=\"-Xmx2g -XX:MaxPermSize=512m\"\n./gradlew build\n```\n
**2. Gradle Daemon Issues**\n**Solution:** Disable daemon\n```bash\n./gradlew build --no-daemon\n```\n
**3. Dependency Issues**\n**Solution:** Refresh dependencies\n```bash\n./gradlew build --refresh-dependencies\n```\n
**4. Build Cache Problems**\n**Solution:** Clean cache\n```bash\n./gradlew cleanBuildCache\n./gradlew build\n```\n\n---\n\n## Final Commands for Production\n\n```bash\n# 1. Clean build\n./gradlew clean\n
# 2. Build and test\n./gradlew build\n
# 3. Generate release bundle\n./gradlew bundleRelease\n
# 4. Check signing report\n./gradlew signingReport\n
# 5. Upload to Play Store\n# Use bundletool\nbundletool build-apks \\\\\n  --bundle=app-release.aab \\\\\n  --output=app.apks \\\\\n  --ks=my.keystore\n
# 6. Install universal APK\nbundletool install-apks --apks=app.apks\n```\n\n---\n\n**DAVID AI v2.0 - Build System**\n*© David Powered by Nexuzy Tech, 2026*\n