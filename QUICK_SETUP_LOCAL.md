# ⚡ DAVID AI - 5 Minute Local Setup

## 🎯 What You Get

✅ **Google Login** (No backend needed)  
✅ **Local Chat Storage** (120-day auto-cleanup)  
✅ **AI Models** (Auto-download from Hugging Face)  
✅ **Device Only** (Zero cloud sync)  
✅ **Open Source** (No backend code needed)  

---

## 🚀 Setup Steps

### Step 1: Get Google Client ID (3 min)
```
1. Go to https://firebase.google.com/console
2. Create new project "DAVID-AI"
3. Add Android app
4. Get Web Client ID from Settings
```

### Step 2: Update Code (1 min)
```kotlin
// File: GoogleAuthManager.kt (Line 22)
.requestIdToken("YOUR_WEB_CLIENT_ID") // ← Paste here
```

### Step 3: Build & Run (1 min)
```bash
./gradlew installDebug
```

### Step 4: First Launch (auto)
- App downloads models
- Google Sign-In works
- Chat stored locally
- **Done!**

---

## 📊 Storage Breakdown

```
Chat History:  ~1-10 MB (auto-deletes after 120 days)
Models:        ~1.5-3 GB (based on device RAM)
App Size:      ~50 MB

Total: 1.5-3.5 GB
```

---

## 🔧 What Changed from Backend Version

| Feature | Backend | Local |
|---------|---------|-------|
| Storage | Cloud | Device |
| Chat | Cloud DB | SQLite |
| Models | Server | Hugging Face |
| Auth | Custom | Google |
| Sync | API | None |
| Cost | $$/month | $0 |
| Privacy | Server | All Local |

---

## ✨ Core Files

✅ `auth/GoogleAuthManager.kt` - Google login  
✅ `storage/ChatHistoryManager.kt` - Local chat storage  
✅ `models/ModelManager.kt` - Model management  
✅ `config/ModelConfig.kt` - Model URLs  

---

## 🎓 Usage Examples

### Save Chat
```kotlin
chatHistoryManager.saveChatMessage(
    userId = "user123",
    userMessage = "Hello",
    assistantResponse = "Hi!"
)
```

### Get History
```kotlin
val history = chatHistoryManager.getRecentHistory(userId)
```

### Download Models
```kotlin
val models = modelManager.getRecommendedModels()
modelManager.downloadModel(model) { progress ->
    println("$progress%")
}
```

---

## ✅ Checklist

- [ ] Firebase project created
- [ ] Web Client ID obtained
- [ ] Code updated with ID
- [ ] Build successful
- [ ] APK installs
- [ ] Google login works
- [ ] Models download
- [ ] Chat saves locally

---

**Zero Backend. 100% Local. Fully Private.**
