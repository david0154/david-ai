# Gesture Control Guide

**Control your device using hand gestures!**

---

## 📋 Table of Contents

1. [What is Gesture Control?](#what-is-gesture-control)
2. [Supported Gestures](#supported-gestures)
3. [Setup](#setup)
4. [Using Gestures](#using-gestures)
5. [Customization](#customization)
6. [Troubleshooting](#troubleshooting)

---

## What is Gesture Control?

**Gesture Control lets you control your device without touching the screen!**

### How It Works

1. **Camera** captures your hand
2. **AI Model** (MediaPipe) detects 21 hand landmarks
3. **Gesture Recognizer** identifies your gesture
4. **Overlay Pointer** moves based on hand position
5. **Action** executes when gesture is recognized

### Benefits

✅ **Hands-free operation**  
✅ **Accessibility for users with motor disabilities**  
✅ **Hygienic (no screen touch)**  
✅ **Fun and futuristic!**  
✅ **Works offline**  

---

## Supported Gestures

### 1. ✋ Open Palm

**Action:** Show pointer

**How to do it:**
- Open your hand fully
- All fingers extended
- Palm facing camera

**Use case:** Activate gesture mode

---

### 2. ✊ Closed Fist

**Action:** Hide pointer

**How to do it:**
- Close all fingers
- Make a fist

**Use case:** Deactivate gesture mode

---

### 3. ☝️ Pointing Up

**Action:** Move pointer

**How to do it:**
- Extend index finger
- Other fingers closed
- Point upward

**Use case:** Move cursor around screen

---

### 4. ✌️ Victory Sign

**Action:** Click/Tap

**How to do it:**
- Extend index and middle fingers
- Form a "V" shape
- Other fingers closed

**Use case:** Click buttons, select items

---

### 5. 👍 Thumbs Up

**Action:** Confirm/OK

**How to do it:**
- Extend thumb upward
- Other fingers closed

**Use case:** Confirm actions, approve

---

### 6. 👎 Thumbs Down

**Action:** Cancel/Back

**How to do it:**
- Extend thumb downward
- Other fingers closed

**Use case:** Cancel, go back

---

## Setup

### Requirements

✅ **Front or rear camera**  
✅ **Good lighting** (not too dark)  
✅ **Clear background** (for better detection)  
✅ **1-2 feet distance** from camera  

### Enable Gesture Control

**Step 1: Grant Camera Permission**

1. Open D.A.V.I.D AI
2. Go to **Settings**
3. Tap **Gesture Control**
4. Allow camera access

**Step 2: Enable Feature**

1. In Settings, toggle **Enable Gesture Control**
2. Select camera (front/rear)
3. Adjust sensitivity (if needed)

**Step 3: Start Using**

1. Show ✋ **Open Palm** to camera
2. Pointer appears on screen
3. Start gesturing!

---

## Using Gestures

### Basic Workflow

```
1. Show ✋ Open Palm → Pointer appears
2. Use ☝️ Pointing to move pointer
3. Use ✌️ Victory to click
4. Show ✊ Fist to hide pointer
```

### Example: Opening Settings

**Step-by-step:**

1. **Activate:** Show ✋ Open Palm
   - Pointer appears

2. **Navigate:** Use ☝️ Pointing finger
   - Move hand to move pointer
   - Position over Settings icon

3. **Click:** Show ✌️ Victory sign
   - Settings opens

4. **Deactivate:** Show ✊ Closed Fist
   - Pointer disappears

### Example: Scrolling

1. Activate pointer (✋ Open Palm)
2. Move hand up/down (☝️ Pointing)
3. Scroll automatically follows

### Example: Typing

1. Open keyboard
2. Activate pointer
3. Point at letter (☝️)
4. Click letter (✌️ Victory)
5. Repeat for each letter

---

## Customization

### Settings Options

**Go to: Settings → Gesture Control**

#### Sensitivity

**Adjust how easily gestures are detected:**

- **Low:** Requires precise gestures
- **Medium:** Balanced (recommended)
- **High:** Detects easily (may have false positives)

#### Pointer Speed

**Control pointer movement speed:**

- **Slow:** Precise but slow
- **Medium:** Balanced
- **Fast:** Quick but less precise

#### Pointer Size

**Change pointer visual size:**

- Small (20px)
- Medium (40px) - default
- Large (60px)

#### Camera Selection

**Choose which camera to use:**

- **Front Camera:** Best for personal use
- **Rear Camera:** Best for presentations

#### Gesture Confirmation

**Toggle visual/audio feedback:**

- ✅ **Visual:** Show animation on gesture
- ✅ **Audio:** Play sound on gesture
- ✅ **Haptic:** Vibrate on gesture

---

## Advanced Features

### Gesture Macros

**Create custom gesture sequences:**

**Example: "Go Home"**
```
1. Thumbs Up
2. Victory Sign
3. Opens home screen
```

**To create:**
1. Settings → Gesture Control → Macros
2. Tap "Add Macro"
3. Record gesture sequence
4. Assign action

### Multi-Hand Gestures

**Use two hands for advanced controls:**

- **Both palms open:** Zoom in
- **Both fists:** Zoom out
- **One up, one down:** Rotate

---

## Tips & Best Practices

### 💡 For Best Accuracy

1. **Good Lighting**
   - Natural light is best
   - Avoid backlighting
   - No shadows on hand

2. **Clear Background**
   - Solid color backgrounds
   - No clutter
   - Contrasting with hand

3. **Proper Distance**
   - 1-2 feet from camera
   - Full hand visible
   - Not too close or far

4. **Steady Hand**
   - Keep hand stable
   - Slow, deliberate movements
   - Pause briefly on gestures

5. **Clear Gestures**
   - Make distinct shapes
   - Fully extend/close fingers
   - Face palm to camera

### ⚠️ Common Mistakes

**❌ Don't:**
- Move hand too fast
- Use partial gestures
- Block camera view
- Use in dark lighting
- Wear gloves

**✅ Do:**
- Move smoothly
- Make full gestures
- Keep camera clear
- Use good lighting
- Bare hands work best

---

## Troubleshooting

### Gesture Not Detected

**Problem:** Gestures not recognized

**Solutions:**
1. Improve lighting
2. Clear background
3. Increase sensitivity
4. Make clearer gestures
5. Check camera not blocked

### Pointer Jumpy/Unstable

**Problem:** Pointer moves erratically

**Solutions:**
1. Reduce sensitivity
2. Steady your hand
3. Improve lighting
4. Move hand slower
5. Enable smoothing (Settings)

### Pointer Not Appearing

**Problem:** Pointer doesn't show

**Solutions:**
1. Check overlay permission granted
2. Restart gesture service
3. Re-enable gesture control
4. Check camera permission

### Wrong Gesture Detected

**Problem:** Different gesture recognized

**Solutions:**
1. Make gestures more distinct
2. Hold gesture longer
3. Reduce sensitivity
4. Retrain gestures (Settings)

### Camera Not Working

**Problem:** Camera feed not showing

**Solutions:**
1. Grant camera permission
2. Close other camera apps
3. Restart D.A.V.I.D AI
4. Check camera not physically blocked
5. Try other camera (front/rear)

---

## Accessibility

### For Users with Disabilities

**Gesture Control is designed to help users with:**

✅ **Motor disabilities** - No screen touch needed  
✅ **Limited mobility** - Control from distance  
✅ **Hand tremors** - Smoothing reduces shaking  
✅ **Visual impairments** - Large pointer option  

### Custom Accessibility Settings

**Settings → Accessibility → Gesture:**

- **Large Pointer:** 80px size
- **High Contrast:** Bright colors
- **Audio Feedback:** Speak actions
- **Dwell Click:** Auto-click after hover
- **Slow Mode:** Reduced sensitivity

---

## Privacy & Security

### 🔒 Your Privacy

**D.A.V.I.D AI Gesture Control:**

✅ **All processing happens on your device**  
✅ **Camera feed never sent to internet**  
✅ **No recording or storage of video**  
✅ **Only hand landmarks extracted**  
✅ **Landmarks processed in real-time**  
✅ **No data collection**  

### How It Works Privately

1. Camera captures frame
2. MediaPipe detects hand **locally**
3. 21 landmark points extracted
4. Gesture recognized **on-device**
5. Action executed
6. **Frame discarded** (not saved)

**Your camera feed NEVER leaves your device!**

---

## FAQ

### Does it work in the dark?

**Partially.** Some light is needed for camera. Use screen brightness or room light.

### Can I use it while wearing gloves?

**Not recommended.** Bare hands work best. Thin gloves might work.

### Does it drain battery?

**Moderate use.** Camera + AI processing uses battery. Optimize:
- Use only when needed
- Lower camera resolution
- Reduce sensitivity

### Can others control my device?

**Only if they're in front of camera.** Dismiss overlay when not in use.

### Works with screen off?

**No.** Screen must be on for overlay and camera.

---

## Future Features

**Coming soon:**

- 🤝 Custom gesture training
- 🎮 Game control mode
- 📱 Multi-device gestures
- 🌎 AR gestures
- 🎵 Media gesture controls

---

## Feedback

**Help us improve Gesture Control!**

- 📧 Email: [david@nexuzy.in](mailto:david@nexuzy.in)
- 🐛 [Report Issue](https://github.com/david0154/david-ai/issues/new?template=bug_report.md)
- ✨ [Request Feature](https://github.com/david0154/david-ai/issues/new?template=feature_request.md)

---

**© 2026 Nexuzy Tech Ltd.**
