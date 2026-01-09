#!/bin/bash

echo "============================================"
echo "DAVID AI - Complete Build Clean"
echo "============================================"
echo ""
echo "This will delete ALL build artifacts and caches"
read -p "Press Enter to continue or Ctrl+C to cancel..."

echo ""
echo "Step 1: Stopping Gradle Daemon..."
./gradlew --stop

echo ""
echo "Step 2: Cleaning project..."
./gradlew clean

echo ""
echo "Step 3: Deleting .gradle directory..."
rm -rf .gradle

echo ""
echo "Step 4: Deleting build directories..."
rm -rf build app/build

echo ""
echo "Step 5: Deleting Gradle cache..."
rm -rf ~/.gradle/caches

echo ""
echo "============================================"
echo "Clean complete! Now rebuilding..."
echo "============================================"
echo ""

echo "Step 6: Building debug APK..."
./gradlew assembleDebug

echo ""
echo "============================================"
if [ $? -eq 0 ]; then
    echo "BUILD SUCCESSFUL!"
    echo "APK location: app/build/outputs/apk/debug/app-debug.apk"
else
    echo "BUILD FAILED!"
    echo "Check the error messages above."
fi
echo "============================================"
