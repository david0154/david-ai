#!/bin/bash

echo "Cleaning DAVID AI Project..."
echo ""

echo "[1/4] Stopping Gradle daemon..."
./gradlew --stop

echo "[2/4] Deleting build directories..."
rm -rf .gradle
rm -rf build
rm -rf app/build

echo "[3/4] Cleaning Gradle cache..."
rm -rf ~/.gradle/caches/transforms-3
rm -rf ~/.gradle/caches/modules-2

echo "[4/4] Running clean build..."
./gradlew clean

echo ""
echo "========================================"
echo "Clean completed successfully!"
echo "Now run: ./gradlew assembleDebug"
echo "========================================"
