# Contributing to D.A.V.I.D AI

Thank you for considering contributing to D.A.V.I.D AI! We welcome contributions from everyone. 🎉

---

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How Can I Contribute?](#how-can-i-contribute)
  - [Reporting Bugs](#reporting-bugs)
  - [Suggesting Features](#suggesting-features)
  - [Contributing Code](#contributing-code)
  - [Improving Documentation](#improving-documentation)
  - [Adding Translations](#adding-translations)
- [Development Setup](#development-setup)
- [Code Style Guidelines](#code-style-guidelines)
- [Commit Message Guidelines](#commit-message-guidelines)
- [Pull Request Process](#pull-request-process)

---

## 📜 Code of Conduct

This project adheres to a [Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code. Please report unacceptable behavior to [david@nexuzy.in](mailto:david@nexuzy.in).

---

## 🤝 How Can I Contribute?

### 🐛 Reporting Bugs

Before reporting a bug:

1. **Check existing issues**: Search [GitHub Issues](https://github.com/david0154/david-ai/issues) to avoid duplicates
2. **Update to latest version**: Ensure you're using the latest release
3. **Test on clean install**: Verify bug persists after fresh install

**Good bug reports include:**

```markdown
**Device Information:**
- Device Model: [e.g., Samsung Galaxy S21]
- Android Version: [e.g., Android 13]
- App Version: [e.g., 1.0.0]
- RAM: [e.g., 8GB]

**Steps to Reproduce:**
1. Open app
2. Say "Hey David"
3. Say "Turn on WiFi"
4. See error

**Expected Behavior:**
WiFi should turn on

**Actual Behavior:**
App crashes

**Logs:**
```
[Paste LogCat output here]
```

**Screenshots:**
[Attach screenshots if applicable]
```

### 💡 Suggesting Features

Before suggesting a feature:

1. **Check existing requests**: Search [GitHub Discussions](https://github.com/david0154/david-ai/discussions)
2. **Explain the use case**: Why is this feature needed?
3. **Consider privacy**: Does it align with our privacy-first approach?

**Good feature requests include:**

```markdown
**Feature Description:**
Add support for custom wake words

**Use Case:**
Users want to use their own name as wake word instead of "Hey David"

**Proposed Implementation:**
- Add wake word customization in Settings
- Use speech recognition to train custom wake word
- Store trained model locally

**Mockups:**
[Attach mockups if available]

**Privacy Impact:**
No privacy concerns - all training done locally
```

### 💻 Contributing Code

#### Types of Contributions We Need:

- 🐛 Bug fixes
- ✨ New features
- 🎨 UI improvements
- ⚡ Performance optimizations
- 🔒 Security enhancements
- 🌍 Translation improvements
- 📚 Documentation updates
- ✅ Test coverage

#### Before You Start:

1. **Create an issue**: Discuss your idea first
2. **Get feedback**: Wait for maintainer approval
3. **Fork the repo**: Create your own fork
4. **Create a branch**: Use descriptive names

### 📚 Improving Documentation

Documentation improvements are always welcome:

- Fix typos
- Clarify instructions
- Add examples
- Update outdated information
- Improve README
- Add code comments

### 🌍 Adding Translations

We support 15 languages! Help us improve:

1. Check `LanguageManager.kt` for supported languages
2. Add/improve translations in string resources
3. Test voice commands in your language
4. Submit pull request

**Current Languages:**
- English, Hindi, Tamil, Telugu, Bengali
- Marathi, Gujarati, Kannada, Malayalam, Punjabi
- Odia, Urdu, Sanskrit, Kashmiri, Assamese

---

## 🛠️ Development Setup

### Prerequisites:

- **Android Studio**: Latest stable version (Hedgehog or newer)
- **JDK**: Version 17 or higher
- **Android SDK**: API 31 (Android 12) or higher
- **Git**: For version control
- **Device/Emulator**: Android 12+ device or emulator with 4GB+ RAM

### Setup Steps:

```bash
# 1. Fork the repository on GitHub

# 2. Clone your fork
git clone https://github.com/YOUR_USERNAME/david-ai.git
cd david-ai

# 3. Add upstream remote
git remote add upstream https://github.com/david0154/david-ai.git

# 4. Create a branch
git checkout -b feature/your-feature-name

# 5. Open in Android Studio
# File → Open → Select david-ai folder

# 6. Sync Gradle
# Wait for Gradle sync to complete

# 7. Run the app
# Click Run button or press Shift+F10
```

### Building:

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

---

## 🎨 Code Style Guidelines

### Kotlin Style:

Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html):

```kotlin
// ✅ GOOD
class VoiceController(
    private val context: Context,
    private val deviceController: DeviceController
) {
    private var isListening = false
    
    fun startListening() {
        if (isListening) return
        // Implementation
    }
}

// ❌ BAD
class VoiceController(context:Context,deviceController:DeviceController){
var isListening=false
fun startListening(){if(isListening)return
//Implementation
}
}
```

### Naming Conventions:

- **Classes**: PascalCase (`VoiceController`, `DeviceManager`)
- **Functions**: camelCase (`startListening`, `processCommand`)
- **Variables**: camelCase (`isListening`, `currentLanguage`)
- **Constants**: UPPER_SNAKE_CASE (`MAX_RETRIES`, `DEFAULT_TIMEOUT`)
- **Packages**: lowercase (`com.davidstudioz.david.voice`)

### Documentation:

```kotlin
/**
 * Process voice commands for device control
 * Routes unknown commands to ChatManager for AI responses
 * 
 * @param command The voice command to process
 * @return True if command was handled, false otherwise
 */
fun processVoiceCommand(command: String): Boolean {
    // Implementation
}
```

### Code Organization:

- Keep functions small (< 50 lines)
- One class per file
- Group related functions
- Use meaningful variable names
- Avoid magic numbers
- Handle errors gracefully

---

## 📝 Commit Message Guidelines

Follow [Conventional Commits](https://www.conventionalcommits.org/):

### Format:

```
type(scope): short description

Longer description if needed

Breaking changes or additional notes
```

### Types:

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code formatting (no logic change)
- `refactor`: Code restructuring (no behavior change)
- `perf`: Performance improvement
- `test`: Adding/updating tests
- `chore`: Maintenance tasks

### Examples:

```bash
# Good commits
feat(voice): add support for custom wake words
fix(gesture): resolve pointer position calculation bug
docs(readme): update installation instructions
perf(ai): optimize model loading time by 40%

# Bad commits
update stuff
fixed bug
changes
```

---

## 🔄 Pull Request Process

### Before Submitting:

1. ✅ **Update from upstream**:
   ```bash
   git fetch upstream
   git rebase upstream/main
   ```

2. ✅ **Test your changes**:
   - Build succeeds
   - No new warnings
   - Feature works as expected
   - No regressions

3. ✅ **Update documentation**:
   - Update README if needed
   - Add code comments
   - Update CHANGELOG

4. ✅ **Follow code style**:
   - Run code formatter
   - Fix lint warnings
   - Check naming conventions

### Submitting PR:

1. **Push to your fork**:
   ```bash
   git push origin feature/your-feature-name
   ```

2. **Create Pull Request** on GitHub

3. **Fill out PR template**:

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Tested on device
- [ ] Tested on emulator
- [ ] Added unit tests
- [ ] No new warnings

## Screenshots
[If applicable]

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-reviewed code
- [ ] Commented complex code
- [ ] Updated documentation
- [ ] No new warnings
- [ ] Tested thoroughly
```

### Review Process:

1. **Automated checks** run (build, lint, tests)
2. **Maintainer review** (usually within 48 hours)
3. **Address feedback** if requested
4. **Approval** from maintainer
5. **Merge** into main branch

### After Merge:

- Your contribution will be in next release
- You'll be added to contributors list
- Thank you! 🎉

---

## 🏆 Recognition

Contributors are recognized:

- Listed in [CONTRIBUTORS.md](CONTRIBUTORS.md)
- Mentioned in release notes
- GitHub contributors page
- Special thanks in README

---

## 📧 Questions?

Need help? Contact us:

- **Email**: [david@nexuzy.in](mailto:david@nexuzy.in)
- **Discussions**: [GitHub Discussions](https://github.com/david0154/david-ai/discussions)
- **Issues**: [GitHub Issues](https://github.com/david0154/david-ai/issues)

---

## 📄 License

By contributing, you agree that your contributions will be licensed under the same license as the project.

---

**Thank you for making D.A.V.I.D AI better! 🚀**

---

*Last Updated: January 12, 2026*
