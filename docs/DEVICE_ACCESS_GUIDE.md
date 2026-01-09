# 📱 Device Access & Permissions Guide

## Overview

David AI requires 40+ permissions to provide full functionality. This guide explains:

- Permission groups and requirements
- Runtime permission handling (Android 6.0+)
- Device access management
- Security best practices

---

## Permission Groups

### Core Permissions (Required)

| Permission | Purpose | Feature |
|------------|---------|----------|
| `RECORD_AUDIO` | Voice input | Voice recognition, wake word |
| `CAMERA` | Hand gesture detection | Gesture control, hand tracking |
| `INTERNET` | Network access | Weather API, cloud sync, AI models |
| `ACCESS_NETWORK_STATE` | Network status | Connection detection |
| `ACCESS_WIFI_STATE` | WiFi status | WiFi control detection |

### Location Permissions

| Permission | Purpose | Feature |
|------------|---------|----------|
| `ACCESS_FINE_LOCATION` | GPS coordinates | Weather location, maps |
| `ACCESS_COARSE_LOCATION` | WiFi/network location | Fallback location source |

### Device Control

| Permission | Purpose | Feature |
|------------|---------|----------|
| `CALL_PHONE` | Make calls | Voice dialing |
| `SEND_SMS` | Send messages | Voice SMS |
| `READ_SMS` | Read messages | Message history |
| `READ_PHONE_STATE` | Phone state | Call detection |
| `READ_CONTACTS` | Contact access | SMS/call contacts |
| `WRITE_CONTACTS` | Write contacts | Add new contacts |
| `READ_CALL_LOG` | Call history | View call logs |
| `WRITE_CALL_LOG` | Write call logs | Log calls |

### Connectivity

| Permission | Purpose | Feature |
|------------|---------|----------|
| `CHANGE_WIFI_STATE` | WiFi toggle | WiFi on/off commands |
| `CHANGE_NETWORK_STATE` | Network control | Connection management |
| `BLUETOOTH` | Bluetooth access | Bluetooth device list |
| `BLUETOOTH_ADMIN` | Pair devices | Connect/disconnect devices |
| `BLUETOOTH_CONNECT` | Connect to paired | Audio output |
| `BLUETOOTH_SCAN` | Discover devices | Find Bluetooth devices |

### Storage

| Permission | Purpose | Feature |
|------------|---------|----------|
| `READ_EXTERNAL_STORAGE` | Read files | AI model loading |
| `WRITE_EXTERNAL_STORAGE` | Write files | Cache, downloads |
| `MANAGE_EXTERNAL_STORAGE` | All files | Media access |
| `READ_MEDIA_*` | Media access | Photos, video, audio |

### Security

| Permission | Purpose | Feature |
|------------|---------|----------|
| `SYSTEM_ALERT_WINDOW` | Overlay windows | Pointer cursor display |
| `USE_BIOMETRIC` | Fingerprint/face | Biometric authentication |
| `USE_FINGERPRINT` | Fingerprint | Older biometric API |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Battery | Background services |

### Device Admin (Special Permission)

| Permission | Purpose | Feature |
|------------|---------|----------|
| Device Admin | Force lock | Voice lock device |

---

## Runtime Permission Handling

### Check Permission

```kotlin
if (ContextCompat.checkSelfPermission(
    context,
    Manifest.permission.CAMERA
) == PackageManager.PERMISSION_GRANTED) {
    // Permission granted
    startGestureRecognition()
} else {
    // Request permission
    requestPermissionsLauncher.launch(
        arrayOf(Manifest.permission.CAMERA)
    )
}
```

### Request Permission

```kotlin
val requestPermissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            // Permission granted
            startFeature()
        } else {
            // Permission denied
            showMessage("Permission required")
        }
    }

requestPermissionLauncher.launch(Manifest.permission.CAMERA)
```

### Request Multiple Permissions

```kotlin
val requestMultiplePermissionsLauncher =
    registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        permissions.forEach { (permission, isGranted) ->
            Log.d("Permission", "$permission: $isGranted")
        }
    }

requestMultiplePermissionsLauncher.launch(
    arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
)
```

---

## Code Implementation

### Using DeviceAccessManager

```kotlin
val deviceAccess = DeviceAccessManager(context)

// Check single permission
if (deviceAccess.hasPermission(DeviceAccessManager.PermissionGroup.CAMERA)) {
    println("Camera available")
} else {
    println("Camera permission not granted")
}

// Get all permissions
val status = deviceAccess.getAccessStatus()
status.forEach { (permission, granted) ->
    println("$permission: $granted")
}

// Get granted permissions
val granted = deviceAccess.getGrantedPermissions()
println("Granted: $granted")

// Get missing permissions
val missing = deviceAccess.getMissingPermissions()
println("Missing: $missing")

// Check running apps
val runningApps = deviceAccess.getRunningApps()
println("Running apps: $runningApps")

// Check if app installed
if (deviceAccess.isAppInstalled("com.whatsapp")) {
    println("WhatsApp installed")
}

// Log access status
deviceAccess.logAccessStatus()
```

### Permission Manager Helper

```kotlin
val permissionManager = PermissionManager(context)

// Check core permissions
if (!permissionManager.areCorePermissionsGranted()) {
    permissionManager.requestCorePermissions(activity) { granted, denied ->
        Log.d("Permissions", "Granted: $granted, Denied: $denied")
    }
}

// Get permission status
val corePermissions = permissionManager.getCorePermissions()
corePermissions.forEach { permission ->
    val status = permissionManager.checkPermission(permission)
    Log.d("Permission", "$permission: $status")
}
```

---

## Permission Manifest Declaration

All permissions already declared in `AndroidManifest.xml`:

```xml
<!-- Voice & Audio -->
<uses-permission android:name="android.permission.RECORD_AUDIO" />

<!-- Camera -->
<uses-permission android:name="android.permission.CAMERA" />

<!-- Network -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />

<!-- Location -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<!-- Device Control -->
<uses-permission android:name="android.permission.CALL_PHONE" />
<uses-permission android:name="android.permission.SEND_SMS" />
<uses-permission android:name="android.permission.READ_SMS" />

<!-- Connectivity -->
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />

<!-- Storage -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" />

<!-- UI -->
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
```

---

## iOS Equivalent Permissions

For reference, if porting to iOS:

| Android Permission | iOS Equivalent |
|-------------------|----------------|
| RECORD_AUDIO | NSMicrophoneUsageDescription |
| CAMERA | NSCameraUsageDescription |
| ACCESS_FINE_LOCATION | NSLocationWhenInUseUsageDescription |
| SEND_SMS | MessageUI (no permission needed) |
| BLUETOOTH | NSBluetoothPeripheralUsageDescription |
| CONTACTS | NSContactsUsageDescription |
| CALENDAR | NSCalendarsUsageDescription |

---

## Permission Scopes by Android Version

### Android 6.0+ (API 23+)

- Runtime permission requests required
- Users can deny individual permissions
- App must handle missing permissions gracefully

### Android 12+ (API 31+)

- Approximate location option
- Bluetooth scan permission split
- Media permissions granularized

### Android 13+ (API 33+)

- Read/write media permissions separate
- Photo and video picker
- Clipboard access notification

### Android 14+ (API 34+)

- Partial media access
- Developer-focused privacy improvements
- Temperature sensors access

---

## Testing Permission Handling

### Simulate Permission Denial

```bash
adb shell pm revoke com.davidstudioz.david android.permission.CAMERA
adb shell pm revoke com.davidstudioz.david android.permission.RECORD_AUDIO
```

### Grant All Permissions

```bash
adb shell pm grant com.davidstudioz.david android.permission.CAMERA
adb shell pm grant com.davidstudioz.david android.permission.RECORD_AUDIO
# ... repeat for all permissions
```

### Check Permissions

```bash
adb shell dumpsys package com.davidstudioz.david | grep PERMISSION
```

---

## Security Best Practices

1. **Minimum Permissions** - Only request what's needed
2. **Runtime Checks** - Always check before accessing feature
3. **Graceful Degradation** - Handle permission denial gracefully
4. **User Control** - Let users enable/disable features
5. **Privacy Policy** - Clear explanation of permission usage
6. **No Background Collection** - Audio/camera only when active
7. **Encryption** - Always encrypt sensitive data
8. **Regular Audits** - Review permission usage periodically

---

## Related Guides

- [Gesture Recognition](GESTURE_RECOGNITION_GUIDE.md) - Camera usage
- [Voice Guide](VOICE_GUIDE.md) - Audio permission
- [Device Control](DEVICE_CONTROL.md) - Call/SMS permissions
- [Security Guide](ENCRYPTION.md) - Data protection

---

**Last Updated:** January 9, 2026  
**Status:** ✅ Complete  
**Tested On:** Android 6.0 - 14.0
