# 🔧 Build Error Fix Guide

## Problem: Redeclaration Errors (Phantom Errors)

If you see errors like:
```
Redeclaration: class VoiceDownloadManager
class VoiceDownloadManager : Any (VoiceDownloadManager.kt:19)
class VoiceDownloadManager : Any (VoiceEngine.kt:19)
```

But `VoiceEngine.kt:19` does NOT actually contain `VoiceDownloadManager`, this is a **stale build cache issue**.

---

## ✅ Solution 1: Deep Clean (Recommended)

```bash
# Run deep clean task
./gradlew deepClean

# Then build
./gradlew assembleDebug
```

This removes:
- All `build/` directories
- Gradle cache (`.gradle/`)
- Kotlin compile caches
- Incremental build state

---

## ✅ Solution 2: Manual Clean

If Solution 1 doesn't work:

### Windows:
```cmd
rd /s /q build
rd /s /q .gradle
rd /s /q app\build
rd /s /q app\.gradle
gradlew clean
gradlew assembleDebug
```

### Linux/Mac:
```bash
rm -rf build .gradle app/build app/.gradle
./gradlew clean
./gradlew assembleDebug
```

---

## ✅ Solution 3: Android Studio IDE Cache

1. In Android Studio: `File > Invalidate Caches...`
2. Check "Invalidate and Restart"
3. Click "Invalidate and Restart"
4. After restart, run: `./gradlew clean build`

---

## ✅ Solution 4: Delete Kotlin Daemon

```bash
# Kill Kotlin daemon
gradlew --stop

# On Windows, also kill manually:
taskkill /F /IM kotlin-compiler-daemon.exe
taskkill /F /IM java.exe

# Then rebuild
gradlew clean assembleDebug
```

---

## Why This Happens

The error occurs because:

1. **Renamed Classes**: `VoiceEngine` data class → `VoiceEngineInfo`
2. **Stale Cache**: Old `.class` files still reference old names
3. **Kotlin Compiler**: Sees both old (cached) and new (source) declarations
4. **Result**: "Redeclaration" error even though source code is correct

---

## Verification

After cleaning, verify no duplicates exist:

```bash
# Search for VoiceDownloadManager class declarations
grep -r "class VoiceDownloadManager" app/src/

# Should show only ONE file:
# app/src/main/kotlin/com/davidstudioz/david/voice/VoiceDownloadManager.kt
```

---

## All Features Preserved

✅ This fix does NOT remove any features!
✅ Only cleans build artifacts
✅ All source code unchanged
✅ All functionality intact

The errors are **phantom errors** from cached builds, not real code issues.

---

## If Still Failing

Check these files have NO duplicate class names:

1. `VoiceEngine.kt` → Should contain ONLY `class VoiceEngine`
2. `VoiceDownloadManager.kt` → Should contain ONLY `class VoiceDownloadManager` and `data class VoiceEngineInfo`

If you see `class VoiceEngine` contains `VoiceDownloadManager` at line 19, that file is corrupted and needs to be re-committed from the repository.
