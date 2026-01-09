@echo off
echo Cleaning DAVID AI Project...
echo.

echo [1/4] Stopping Gradle daemon...
call gradlew --stop

echo [2/4] Deleting build directories...
if exist .gradle rmdir /s /q .gradle
if exist build rmdir /s /q build
if exist app\build rmdir /s /q app\build

echo [3/4] Cleaning Gradle cache...
if exist %USERPROFILE%\.gradle\caches\transforms-3 rmdir /s /q %USERPROFILE%\.gradle\caches\transforms-3
if exist %USERPROFILE%\.gradle\caches\modules-2 rmdir /s /q %USERPROFILE%\.gradle\caches\modules-2

echo [4/4] Running clean build...
call gradlew clean

echo.
echo ========================================
echo Clean completed successfully!
echo Now run: gradlew assembleDebug
echo ========================================
pause
