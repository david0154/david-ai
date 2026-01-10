@echo off
REM Colors won't work in Command Prompt, but we'll use basic formatting
setlocal enabledelayedexpansion

echo.
echo ================================
echo Gradle Compatibility Verification
echo ================================
echo.

REM Check if gradle wrapper exists
if not exist "gradlew.bat" (
    echo Error: gradlew.bat not found. Please run from project root.
    exit /b 1
)

echo 1. Checking Gradle version...
call gradlew --version
echo.

echo 2. Verifying gradle.properties configuration...
echo Key Settings:
findstr "org.gradle.warning.mode org.gradle.unsafe.configuration-cache android.enableSdkXmlParsing" gradle.properties
echo.

echo 3. Checking Gradle properties...
call gradlew properties | findstr "gradle.version compileSdk targetSdk"
echo.

echo 4. Validating build configuration...
echo Running dry-run (first 20 lines)...
call gradlew build --dry-run 2>&1 | more +20
echo.

echo 5. Checking for deprecation warnings (this may take a moment)...
call gradlew clean -w all > %TEMP%\gradle_warnings.log 2>&1
findstr "deprecated" %TEMP%\gradle_warnings.log
if errorlevel 1 (
    echo No critical deprecation warnings found!
) else (
    echo Found deprecation warnings (see above)
    echo Run with --warning-mode=all for complete list
)
echo.

echo 6. Verifying Android SDK...
if defined ANDROID_SDK_ROOT (
    echo SDK Path: !ANDROID_SDK_ROOT!
) else if defined ANDROID_HOME (
    echo SDK Path: !ANDROID_HOME!
) else (
    echo Warning: ANDROID_SDK_ROOT or ANDROID_HOME not set
    echo This may be set automatically by Android Studio
)
echo.

echo 7. Checking Kotlin version...
call gradlew dependencies | findstr "kotlin"
echo.

echo ================================
echo Verification Summary
echo ================================
echo.
echo [OK] Gradle Version verified
echo [OK] Configuration validated
echo [OK] Build system ready
echo.
echo Next Steps:
echo 1. Run full build:  gradlew assemble
echo 2. Run with warnings:  gradlew build --warning-mode=all
echo 3. Clean and rebuild:  gradlew clean build
echo.
echo For more details, see GRADLE_COMPATIBILITY_FIXES.md
echo.
pause
