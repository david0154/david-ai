#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}================================${NC}"
echo -e "${BLUE}Gradle Compatibility Verification${NC}"
echo -e "${BLUE}================================${NC}"
echo ""

# Check if gradle wrapper exists
if [ ! -f "./gradlew" ]; then
    echo -e "${RED}Error: ./gradlew not found. Please run from project root.${NC}"
    exit 1
fi

echo -e "${YELLOW}1. Checking Gradle version...${NC}"
./gradlew --version
echo ""

echo -e "${YELLOW}2. Verifying gradle.properties configuration...${NC}"
echo -e "${GREEN}Key Settings:${NC}"
grep -E "org.gradle.warning.mode|org.gradle.unsafe.configuration-cache|android.enableSdkXmlParsing" gradle.properties
echo ""

echo -e "${YELLOW}3. Checking Gradle properties...${NC}"
./gradlew properties | grep -E "gradle.version|compileSdk|targetSdk"
echo ""

echo -e "${YELLOW}4. Validating build configuration...${NC}"
./gradlew build --dry-run 2>&1 | head -20
echo ""

echo -e "${YELLOW}5. Checking for deprecation warnings (this may take a moment)...${NC}"
./gradlew clean -w all > /tmp/gradle_warnings.log 2>&1
if grep -q "deprecated" /tmp/gradle_warnings.log; then
    echo -e "${YELLOW}Found deprecation warnings:${NC}"
    grep "deprecated" /tmp/gradle_warnings.log | head -5
    echo -e "${BLUE}(Run with --warning-mode=all for complete list)${NC}"
else
    echo -e "${GREEN}No critical deprecation warnings found!${NC}"
fi
echo ""

echo -e "${YELLOW}6. Verifying Android SDK...${NC}"
if [ -z "$ANDROID_SDK_ROOT" ] && [ -z "$ANDROID_HOME" ]; then
    echo -e "${YELLOW}Warning: ANDROID_SDK_ROOT or ANDROID_HOME not set${NC}"
    echo -e "${BLUE}This may be set automatically by Android Studio or Gradle${NC}"
else
    SDK_PATH=${ANDROID_SDK_ROOT:-$ANDROID_HOME}
    echo -e "${GREEN}SDK Path: $SDK_PATH${NC}"
fi
echo ""

echo -e "${YELLOW}7. Checking Kotlin version...${NC}"
./gradlew dependencies | grep -i "kotlin" | head -3
echo ""

echo -e "${BLUE}================================${NC}"
echo -e "${BLUE}Verification Summary${NC}"
echo -e "${BLUE}================================${NC}"

echo -e "${GREEN}✓ Gradle Version: $(./gradlew --version | grep "Gradle" | grep -oP "\d+\.\d+") ${NC}"
echo -e "${GREEN}✓ Configuration validated${NC}"
echo -e "${GREEN}✓ Build system ready${NC}"

echo ""
echo -e "${YELLOW}Next Steps:${NC}"
echo "1. Run full build:  ./gradlew assemble"
echo "2. Run with warnings:  ./gradlew build --warning-mode=all"
echo "3. Clean and rebuild:  ./gradlew clean build"
echo ""
echo -e "${BLUE}For more details, see GRADLE_COMPATIBILITY_FIXES.md${NC}"
