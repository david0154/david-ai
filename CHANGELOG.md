# Changelog - D.A.V.I.D AI

All notable changes to this project will be documented in this file.

## [Unreleased] - 2026-01-13

### Added
- **WeatherService**: New weather integration with Open-Meteo API
  - Real-time weather data with actual temperature, conditions, humidity, and wind speed
  - Location-based weather queries (e.g., "weather in New York")
  - Natural language weather descriptions with contextual advice
  - No API key required - uses free Open-Meteo API
  - Geocoding support for city names
  - Default location fallback (Kolkata)

- **Male Voice Support**: Added David (male) voice option
  - Voice selection between David (male) and Dayana (female)
  - Automatic pitch adjustment for male/female voices
  - Gender-specific voice detection from system TTS
  - Voice preferences saved in settings

- **Code Filtering**: Comprehensive filtering system
  - Removes code blocks, JSON, arrays, and technical syntax from responses
  - Filters debug logs, status emojis, and internal tags
  - Prevents TTS from speaking code or technical terms
  - Cleans up chat responses for natural conversation

### Fixed

#### Chat System
- **Weather Integration**: Chat now returns actual weather data instead of web URLs
  - Integrated WeatherService for real-time weather information
  - Removed web search for weather queries
  - Weather responses include temperature in both Celsius and Fahrenheit
  - Contextual weather advice (umbrella reminders, heat warnings, etc.)

- **Response Filtering**: Fixed chat responses containing internal code
  - All responses now filtered before sending to user
  - Removed code patterns, debug logs, and technical syntax
  - Cleaner, more natural conversation flow

- **Model Loading**: Improved LLM model detection and loading
  - Better error handling for model initialization
  - Fallback to smart responses when model not available
  - Status indicators show model availability

#### Gesture Recognition
- **Model Loading**: Fixed gesture models not loading from ModelManager
  - Properly checks ModelManager's `david_models` directory
  - Detects downloaded `.task` and `.tflite` gesture model files
  - Validates model file size (minimum 1MB)
  - Clear error messages when models not found
  - Status shows whether models are loaded or need downloading

- **Initialization**: Improved MediaPipe initialization
  - Better error logging with stack traces
  - Separate initialization for hand detection and gesture recognition
  - Fallback mode when models not available
  - Status indicators: "✅ Models loaded" vs "❌ Models not loaded"

- **Model Detection**: Enhanced model file detection
  - Looks for files containing "hand", "gesture", "palm"
  - Supports both `.task` (MediaPipe) and `.tflite` (TensorFlow Lite) formats
  - Logs available files for debugging
  - Validates model integrity before loading

#### Voice System
- **Male Voice**: Added support for male voice (David)
  - `getAvailableVoices()` now returns both David (Male) and Dayana (Female)
  - Voice selection properly saved in SharedPreferences
  - Automatic pitch adjustment (0.8 for male, 1.2 for female)
  - Searches system TTS for gender-specific voices
  - Falls back to pitch adjustment if specific voice not found

- **Code Speech**: Fixed TTS speaking internal code
  - `filterInternalCode()` function filters text before speech
  - Removes code blocks, JSON, arrays, HTML/XML tags
  - Strips debug logs, status emojis, technical syntax
  - Prevents speaking of variable declarations, imports, packages
  - Cleans emoji characters that shouldn't be vocalized

- **Voice Management**: Improved voice selection
  - `changeVoice()` properly updates preferences
  - `getCurrentVoice()` retrieves saved selection
  - Better logging of available system voices
  - Locale-aware voice selection (English preference)

### Improved

#### Model Manager
- Already functioning correctly for model downloads
- Models properly saved to `app/files/david_models/`
- Download progress tracking with pause/resume support
- Model validation and integrity checks
- Essential models detected based on device RAM

#### Chat Engine
- Enhanced fallback responses with Nexuzy Tech branding
- Better handling of greetings, questions, and commands
- Math calculation support
- Time and date queries
- General knowledge responses
- Jokes and personality

#### Error Handling
- Better error messages throughout the app
- Detailed logging for debugging
- User-friendly error descriptions
- Graceful fallbacks when features unavailable

### Technical Details

#### New Files
- `app/src/main/kotlin/com/davidstudioz/david/weather/WeatherService.kt`
  - Complete weather service implementation
  - Open-Meteo API integration
  - Geocoding and location services
  - Weather condition mapping (WMO codes)

#### Modified Files
- `app/src/main/kotlin/com/davidstudioz/david/chat/ChatEngine.kt`
  - Added WeatherService integration
  - Added code filtering for responses
  - Improved weather query detection
  - Enhanced model status reporting

- `app/src/main/kotlin/com/davidstudioz/david/gesture/GestureController.kt`
  - Fixed model directory path (uses ModelManager's directory)
  - Enhanced model file detection logic
  - Better initialization error handling
  - Improved status reporting
  - Added model validation checks

- `app/src/main/kotlin/com/davidstudioz/david/voice/TextToSpeechEngine.kt`
  - Added male voice support (David)
  - Added code filtering before speech
  - Added `getAvailableVoices()` method
  - Improved voice selection logic
  - Added gender-based pitch adjustment
  - Added `isSpeaking()` status check

### Dependencies
- No new dependencies added
- Uses existing libraries:
  - MediaPipe for gesture recognition
  - Android TTS for speech
  - OkHttp for network requests
  - Gson for JSON parsing

### Breaking Changes
- None - all changes are backward compatible

### Migration Notes
- Users may need to re-download gesture models if previously downloaded incorrectly
- Voice preferences will be migrated automatically
- Weather data will now show actual information instead of web links

### Known Issues
- Gesture recognition requires camera permission
- Male voice quality depends on system TTS voices available
- Weather service requires internet connection
- Location permission needed for automatic location detection

### Credits
- Developed by Nexuzy Tech Ltd.
- Lead Developer: David
- Website: https://nexuzy.tech

---

## Testing Checklist

### Weather Service
- [ ] Test "what's the weather" query
- [ ] Test "weather in [city]" query
- [ ] Test weather with no internet
- [ ] Verify temperature in Celsius and Fahrenheit
- [ ] Check contextual advice (rain, snow, heat, cold)

### Chat System
- [ ] Verify weather returns actual data, not URLs
- [ ] Check responses don't contain code
- [ ] Test with LLM model loaded
- [ ] Test without LLM model (fallback)
- [ ] Verify model status display

### Gesture Recognition
- [ ] Download gesture models from settings
- [ ] Check model status shows "loaded" or "not loaded"
- [ ] Start gesture detection
- [ ] Verify no "model not loaded" error when models exist
- [ ] Test hand tracking
- [ ] Test gesture recognition

### Voice System
- [ ] Check voice settings show both David (Male) and Dayana (Female)
- [ ] Select David voice and verify lower pitch
- [ ] Select Dayana voice and verify higher pitch
- [ ] Test TTS doesn't speak code or emojis
- [ ] Verify voice preference saves correctly

### Integration
- [ ] Test full conversation flow
- [ ] Ask for weather via voice
- [ ] Verify gesture models load on app start
- [ ] Check all features work together

---

## Future Improvements

### Planned Features
- [ ] Weather forecast (next 7 days)
- [ ] Weather alerts and notifications
- [ ] More gesture types (swipe, pinch, rotate)
- [ ] Custom voice training
- [ ] Offline weather caching
- [ ] Weather widget
- [ ] Voice emotion detection
- [ ] Gesture customization

### Performance Optimizations
- [ ] Lazy loading for models
- [ ] Background model preloading
- [ ] Weather data caching
- [ ] TTS response queue optimization
- [ ] Gesture processing on GPU

### UI/UX Enhancements
- [ ] Voice preview in settings
- [ ] Gesture training mode
- [ ] Weather visualization
- [ ] Model download progress in status bar
- [ ] Interactive gesture tutorial

---

## Support

For issues, feature requests, or questions:
- GitHub Issues: https://github.com/david0154/david-ai/issues
- Website: https://nexuzy.tech
- Email: support@nexuzy.tech

## License

Copyright © 2026 Nexuzy Tech Ltd. All rights reserved.
