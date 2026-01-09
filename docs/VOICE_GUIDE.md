# 🎤 DAVID AI - Voice Control Guide

## Overview

DAVID AI provides comprehensive voice control capabilities with offline speech recognition and synthesis.

## Features

### Speech-to-Text (STT)
- **Offline Recognition**: Whisper.cpp Tiny (50MB)
- **Latency**: < 500ms per phrase
- **Languages**: 14 supported languages
- **Accuracy**: 85-95% for standard English, 75-85% for accented speech

### Text-to-Speech (TTS)
- **Engine**: Coqui Indic TTS
- **Voices**: Male & Female per language
- **Speed**: < 1 second per sentence
- **Quality**: Natural sounding with prosody

### Voice Biometrics
- **Speaker Recognition**: Identify user by voice
- **Anti-Spoofing**: Detect playback attacks
- **Enrollment**: 3-5 seconds of audio

## Supported Languages

1. English (en)
2. Hindi (hi)
3. Bengali (bn)
4. Tamil (ta)
5. Telugu (te)
6. Marathi (mr)
7. Gujarati (gu)
8. Punjabi (pa)
9. Urdu (ur)
10. Kannada (kn)
11. Malayalam (ml)
12. Odia (or)
13. Assamese (as)
14. Maithili (mai)

## Voice Commands

### Device Control
```
"David, call <contact>"
"David, send message to <contact> – <message>"
"David, turn on WiFi"
"David, turn on Bluetooth"
"David, set volume to 50%"
"David, increase brightness"
"David, take a photo"
"David, lock my phone"
"David, unlock my phone"
```

### AI Assistant
```
"David, what's the weather in <city>?"
"David, explain this photo"
"David, search for <query>"
"David, create an image of <description>"
"David, translate this to <language>"
```

### Smart Home (if connected)
```
"David, turn on the lights"
"David, set temperature to 24°C"
"David, play music"
```

## Voice Authentication

### Setup
1. Open Settings
2. Go to Security
3. Select "Voice Authentication"
4. Click "Enroll"
5. Speak the 3 authentication phrases:
   - "My name is David"
   - "David is my voice assistant"
   - "Unlock my device"
6. Confirm enrollment

### Usage
- Say any of the enrollment phrases
- Device unlocks if match confidence > 95%
- 3 failed attempts lock for 1 hour

## Voice Recording

### Waveform Visualization
- Real-time frequency visualization
- Noise level indicator
- Recording duration timer
- VU meter for audio input

### Audio Processing
- Noise suppression
- Echo cancellation
- Automatic gain control
- Voice activity detection

## Customization

### Language Settings
```kotlin
// Change TTS language
voiceEngine.setLanguage("hi") // Hindi
voiceEngine.setLanguage("ta") // Tamil
```

### Voice Selection
```kotlin
// Choose male or female voice
voiceEngine.setVoice("female", "natural")
voiceEngine.setVoice("male", "professional")
```

### Speech Rate
```kotlin
// Adjust speed (0.5 = slow, 1.0 = normal, 1.5 = fast)
voiceEngine.setSpeechRate(1.2)
```

## API Reference

### VoiceEngine

```kotlin
suspend fun startRecording(): Result<String>
suspend fun stopRecording(): Result<String>
suspend fun transcribeAudio(audioPath: String): Result<String>
suspend fun synthesizeSpeech(text: String, language: String): Result<String>
suspend fun playAudio(filePath: String): Result<Unit>
```

## Troubleshooting

### No Audio Input
- Check microphone permission
- Test microphone with other apps
- Restart device

### Poor Recognition
- Speak clearly and slowly
- Reduce background noise
- Check language setting
- Re-train voice biometrics

### TTS Issues
- Ensure speaker volume is not muted
- Check language pack is downloaded
- Test with different text

## Privacy

- All voice processing is offline
- No audio sent to cloud
- Voice models stored locally
- User voice biometrics encrypted

## Performance

| Device RAM | STT Speed | TTS Speed |
|------------|-----------|----------|
| 1.5 GB     | 800ms     | 1500ms   |
| 2 GB       | 600ms     | 1200ms   |
| 3 GB       | 500ms     | 1000ms   |
| 4+ GB      | 300ms     | 800ms    |

## Storage

- Whisper Model: 50 MB
- TTS Models (14 langs): 150 MB per language
- Total Voice Data: ~2 GB

## See Also

- [Device Control Guide](DEVICE_CONTROL.md)
- [Encryption Guide](ENCRYPTION.md)
- [Backend Integration](BACKEND.md)
