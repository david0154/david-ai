# GGUF Model Support (Optional)

## Current Status

⚠️ **GGUF support is currently DISABLED** because the `java-llama.cpp` library for Android doesn't exist as a Maven dependency.

✅ **Your app works perfectly without it!**

All features are functional:
- 100+ Smart Responses
- News API (Indian headlines)
- Weather API (500+ cities)
- Web Search
- Device Control
- TensorFlow Lite models
- Gesture Recognition
- Voice Control

---

## How to Enable GGUF Support (Future)

If you want to add GGUF model support, you have 3 options:

### **Option 1: Use Official llama.cpp (Recommended)**

1. Add llama.cpp as a git submodule:
```bash
cd android-app-root
git submodule add https://github.com/ggml-org/llama.cpp libs/llama.cpp
```

2. Follow the official Android build guide:
https://github.com/ggml-org/llama.cpp/blob/master/docs/android.md

3. Build the native libraries using CMake

4. Update `LlamaCppEngine.kt` to use the JNI bindings

---

### **Option 2: Use Community Wrapper**

1. Add the community wrapper as submodule:
```bash
git submodule add https://github.com/kherud/java-llama.cpp
```

2. Follow their Android integration guide:
https://github.com/kherud/java-llama.cpp#importing-in-android

3. Update `build.gradle.kts` to include the submodule

---

### **Option 3: Build Your Own JNI Wrapper**

1. Clone llama.cpp
2. Create Android JNI bindings
3. Compile native libraries for Android (arm64-v8a, armeabi-v7a)
4. Create Kotlin wrapper classes

---

## Why GGUF Support Was Disabled

**Problem:** The dependency doesn't exist:
```kotlin
implementation("io.github.kherud:java-llama.cpp:3.1.1-android") // ❌ Not found
```

**Solution:** Removed the dependency to fix build errors

**Impact:** None! App works perfectly with:
- Smart responses (100+ patterns)
- News/Weather APIs
- TensorFlow Lite models
- All other features

---

## Current Architecture

```
User Message
    ↓
ChatManager
    ↓
LlamaCppEngine.isReady() → false ⚠️
    ↓
Falls back to:
├─ NewsService (for news queries)
├─ WeatherService (for weather)
├─ WebSearchEngine (for web queries)
├─ LLMInferenceEngine (TFLite models)
└─ Smart Responses (100+ patterns)
```

---

## Files That Reference GGUF

1. `LlamaCppEngine.kt` - Disabled, returns false
2. `ChatManager.kt` - Checks `llamaCppEngine.isReady()`, falls back
3. `LLMEngine.kt` - Checks `llamaCppEngine.isReady()`, falls back

**All gracefully handle absence of GGUF support!**

---

## Want to Add GGUF Later?

When you're ready to add GGUF support:

1. Choose one of the 3 options above
2. Uncomment code in `LlamaCppEngine.kt`
3. Implement the JNI bindings
4. Test with a small GGUF model (e.g., TinyLlama Q4)

Your existing features will continue working alongside GGUF!

---

## Recommended Models (When GGUF is Enabled)

| Model | Size | Speed | Quality |
|-------|------|-------|--------|
| TinyLlama Q4 | 600MB | Fast | Good |
| Phi-2 Q4_K_M | 1.6GB | Medium | Excellent |
| Qwen-0.5B Q4 | 300MB | Very Fast | Fair |

---

**For now, enjoy your fully functional app without GGUF! 🚀**
