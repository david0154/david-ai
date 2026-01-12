# Contributing to D.A.V.I.D AI

Thank you for considering contributing to D.A.V.I.D AI! 🎉

We welcome contributions from everyone. This document provides guidelines for contributing to the project.

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How Can I Contribute?](#how-can-i-contribute)
- [Reporting Bugs](#reporting-bugs)
- [Suggesting Features](#suggesting-features)
- [Code Contributions](#code-contributions)
- [Translation Contributions](#translation-contributions)
- [Documentation](#documentation)
- [Style Guidelines](#style-guidelines)
- [Commit Messages](#commit-messages)
- [Pull Request Process](#pull-request-process)

## 📜 Code of Conduct

This project adheres to a Code of Conduct that all contributors are expected to follow. Please read [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) before contributing.

## 🤝 How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check existing issues to avoid duplicates.

**When reporting a bug, include**:

1. **Device Information**:
   - Device model (e.g., Samsung Galaxy S23)
   - Android version (e.g., Android 14)
   - RAM size (e.g., 8GB)
   - Storage available

2. **App Information**:
   - App version (from Settings → About)
   - Build number
   - Which AI models are downloaded

3. **Bug Description**:
   - Clear, concise title
   - Steps to reproduce
   - Expected behavior
   - Actual behavior
   - Screenshots/screen recordings
   - LogCat output (if available)

4. **Environment**:
   - Which language is selected
   - Which features were being used
   - Internet connection status

**Bug Report Template**:

```markdown
### Bug Description
A clear description of the bug.

### Steps to Reproduce
1. Open app
2. Say "Hey David"
3. ...

### Expected Behavior
What should happen

### Actual Behavior
What actually happens

### Device Information
- Device: Samsung Galaxy S23
- Android: 14
- RAM: 8GB
- App Version: 1.0.0

### Screenshots
[Attach screenshots]

### LogCat Output
```
[Paste relevant logs]
```
```

### Suggesting Features

We love feature suggestions! Before creating a feature request:

1. Check if the feature already exists
2. Search existing feature requests
3. Consider if it fits D.A.V.I.D's goals (privacy, local AI, voice control)

**When suggesting a feature, include**:

1. **Feature Title**: Clear, descriptive title
2. **Problem**: What problem does this solve?
3. **Proposed Solution**: How should it work?
4. **Alternatives**: Other ways to solve the problem
5. **Use Case**: Example scenarios
6. **Mockups**: UI mockups if applicable

**Feature Request Template**:

```markdown
### Feature Title
Add [feature name]

### Problem
Describe the problem this feature solves

### Proposed Solution
How should this feature work?

### Alternatives
Other approaches considered

### Use Case
Example: "When I say 'Set timer for 5 minutes', D.A.V.I.D should..."

### Mockups
[Attach mockups if applicable]
```

## 💻 Code Contributions

### Getting Started

1. **Fork the repository**
   ```bash
   git clone https://github.com/YOUR-USERNAME/david-ai.git
   cd david-ai
   ```

2. **Create a branch**
   ```bash
   git checkout -b feature/amazing-feature
   ```
   
   Branch naming:
   - `feature/` for new features
   - `bugfix/` for bug fixes
   - `docs/` for documentation
   - `refactor/` for refactoring

3. **Make your changes**
   - Follow code style guidelines
   - Add tests if applicable
   - Update documentation

4. **Test thoroughly**
   - Test on real device
   - Test voice commands
   - Test gestures
   - Check for memory leaks

5. **Commit your changes**
   ```bash
   git commit -m 'Add: Amazing feature that does X'
   ```

6. **Push to your fork**
   ```bash
   git push origin feature/amazing-feature
   ```

7. **Create Pull Request**
   - Use PR template
   - Link related issues
   - Add screenshots/videos

### Development Setup

**Requirements**:
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or later
- Android SDK 35
- Minimum 16GB RAM (for AI model testing)
- Physical Android device (recommended for testing)

**Build the app**:
```bash
./gradlew assembleDebug
```

**Run tests**:
```bash
./gradlew test
./gradlew connectedAndroidTest
```

### Code Areas

**Easy for beginners**:
- UI improvements
- Documentation
- Translations
- Bug fixes in settings

**Medium difficulty**:
- Voice command additions
- Gesture improvements
- Device control features

**Advanced**:
- AI model integration
- Performance optimization
- Background processing

## 🌍 Translation Contributions

We support 15 languages! Help us improve translations:

### Current Languages

- English (en) ✅
- Hindi (hi) ✅
- Tamil (ta) ✅
- Telugu (te) ✅
- Bengali (bn) ✅
- Marathi (mr) ✅
- Gujarati (gu) ✅
- Kannada (kn) ✅
- Malayalam (ml) ✅
- Punjabi (pa) ✅
- Odia (or) ✅
- Urdu (ur) ✅
- Sanskrit (sa) ✅
- Kashmiri (ks) ✅
- Assamese (as) ✅

### How to Contribute Translations

1. Fork the repository
2. Edit `strings.xml` files in `res/values-XX/`
3. Test the translations in the app
4. Create a Pull Request

**Translation files**:
- `app/src/main/res/values-hi/strings.xml` (Hindi)
- `app/src/main/res/values-ta/strings.xml` (Tamil)
- etc.

## 📚 Documentation

Documentation improvements are always welcome:

- README improvements
- Code comments
- Wiki pages
- Tutorial videos
- Blog posts

## 🎨 Style Guidelines

### Kotlin Code Style

Follow [Kotlin official style guide](https://kotlinlang.org/docs/coding-conventions.html):

```kotlin
// Good
fun processVoiceCommand(command: String) {
    val lowerCommand = command.lowercase()
    when {
        "wifi on" in lowerCommand -> enableWiFi()
        else -> handleUnknownCommand(command)
    }
}

// Bad
fun ProcessVoiceCommand(Command:String){
val Lower_Command=Command.lowercase()
if(Lower_Command.contains("wifi on")){EnableWiFi()}
}
```

**Key points**:
- Use `camelCase` for variables and functions
- Use `PascalCase` for classes
- Indent with 4 spaces
- Max line length: 120 characters
- Use meaningful names
- Add KDoc comments for public APIs

### XML Style

```xml
<!-- Good -->
<Button
    android:id="@+id/btnSettings"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="@string/settings" />

<!-- Bad -->
<Button android:id="@+id/button1" android:layout_width="match_parent" android:layout_height="wrap_content" android:text="Settings"/>
```

### Compose Style

```kotlin
@Composable
fun SettingsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Settings")
    }
}
```

## 📝 Commit Messages

Use clear, descriptive commit messages:

### Format

```
Type: Brief description (50 chars max)

Detailed description if needed (wrap at 72 chars).
Explain what and why, not how.

Fixes #123
```

### Types

- `Add:` New feature
- `Fix:` Bug fix
- `Update:` Update existing feature
- `Refactor:` Code refactoring
- `Docs:` Documentation changes
- `Style:` Formatting changes
- `Test:` Add or update tests
- `Chore:` Maintenance tasks

### Examples

```
Add: Voice command for setting alarms

Implemented voice recognition for alarm setting.
Users can now say "Set alarm for 7 AM".

Fixes #45
```

```
Fix: Crash when camera permission denied

Added null check before accessing camera.
Show error message to user.

Fixes #78
```

## 🔄 Pull Request Process

1. **Before creating PR**:
   - Update README if needed
   - Add tests
   - Update documentation
   - Test on real device
   - Check for merge conflicts

2. **PR Title**:
   - Clear and descriptive
   - Start with type prefix
   - Example: `Add: Settings screen with language selector`

3. **PR Description**:
   ```markdown
   ## Changes
   - Added SettingsActivity
   - Implemented language selector
   - Added about page
   
   ## Testing
   - Tested on Samsung Galaxy S23 (Android 14)
   - All 15 languages load correctly
   - Settings persist after app restart
   
   ## Screenshots
   [Attach screenshots]
   
   ## Related Issues
   Fixes #123
   Relates to #456
   ```

4. **Review Process**:
   - Address reviewer feedback
   - Keep PR focused (one feature per PR)
   - Be responsive to comments

5. **After Merge**:
   - Delete your branch
   - Pull latest changes
   - Thank reviewers! 🙏

## 🧪 Testing

### Before Submitting PR

**Manual Testing**:
- [ ] App builds successfully
- [ ] No compilation errors
- [ ] App runs on device
- [ ] Feature works as expected
- [ ] No crashes
- [ ] UI looks good
- [ ] Performance is acceptable
- [ ] Battery usage is reasonable

**Voice Testing**:
- [ ] Voice commands recognized
- [ ] TTS speaks correctly
- [ ] Multiple languages work
- [ ] Background listening works

**Gesture Testing**:
- [ ] Gestures detected correctly
- [ ] Pointer moves smoothly
- [ ] Click actions work
- [ ] No false positives

## 🏆 Recognition

Contributors will be:

- Listed in README acknowledgments
- Mentioned in release notes
- Credited in About page
- Given contributor badge (if applicable)

## 📧 Questions?

Need help? Have questions?

- **Email**: [david@nexuzy.in](mailto:david@nexuzy.in)
- **Issues**: [GitHub Issues](https://github.com/david0154/david-ai/issues)
- **Discussions**: [GitHub Discussions](https://github.com/david0154/david-ai/discussions)

## 📄 License

By contributing, you agree that your contributions will be licensed under the same license as the project (Apache 2.0).

---

Thank you for contributing to D.A.V.I.D AI! 🚀

**© 2026 Nexuzy Tech Ltd.**
