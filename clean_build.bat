@echo off
echo ============================================
echo DAVID AI - Complete Build Clean
echo ============================================
echo.
echo This will delete ALL build artifacts and caches
echo Press Ctrl+C to cancel, or
pause

echo.
echo Step 1: Stopping Gradle Daemon...
call gradlew --stop

echo.
echo Step 2: Cleaning project...
call gradlew clean

echo.
echo Step 3: Deleting .gradle directory...
if exist .gradle rmdir /s /q .gradle

echo.
echo Step 4: Deleting build directories...
if exist build rmdir /s /q build
if exist app\build rmdir /s /q app\build

echo.
echo Step 5: Deleting Gradle cache...
if exist %USERPROFILE%\.gradle\caches rmdir /s /q %USERPROFILE%\.gradle\caches

echo.
echo ============================================
echo Clean complete! Now rebuilding...
echo ============================================
echo.

echo Step 6: Building debug APK...
call gradlew assembleDebug

echo.
echo ============================================
if %ERRORLEVEL% EQU 0 (
    echo BUILD SUCCESSFUL!
    echo APK location: app\build\outputs\apk\debug\app-debug.apk
) else (
    echo BUILD FAILED!
    echo Check the error messages above.
)
echo ============================================
pause
