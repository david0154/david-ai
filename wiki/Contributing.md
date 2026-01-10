# 🤝 Contributing to D.A.V.I.D AI

**Thank you for considering contributing to D.A.V.I.D AI!**

We welcome contributions from everyone, whether you're fixing bugs, adding features, improving documentation, or translating the app.

---

## 📋 Table of Contents

1. [Code of Conduct](#code-of-conduct)
2. [How Can I Contribute?](#how-can-i-contribute)
3. [Development Setup](#development-setup)
4. [Coding Standards](#coding-standards)
5. [Commit Guidelines](#commit-guidelines)
6. [Pull Request Process](#pull-request-process)
7. [Translation Guidelines](#translation-guidelines)
8. [Bug Reporting](#bug-reporting)
9. [Feature Requests](#feature-requests)

---

## 📜 Code of Conduct

### Our Pledge

We are committed to providing a welcoming and inclusive experience for everyone. We pledge to:

- Be respectful and considerate
- Welcome diverse perspectives
- Accept constructive criticism gracefully
- Focus on what's best for the community
- Show empathy towards other contributors

### Unacceptable Behavior

- Harassment or discriminatory language
- Personal attacks or trolling
- Publishing others' private information
- Any conduct that could be considered inappropriate

**Report violations:** [david@nexuzy.in](mailto:david@nexuzy.in)

---

## 🛠️ How Can I Contribute?

### 1. 🐛 Report Bugs

[Create a bug report](https://github.com/david0154/david-ai/issues/new?template=bug_report.md)

**Good bug reports include:**
- Clear, descriptive title
- Steps to reproduce
- Expected vs actual behavior
- Screenshots (if applicable)
- Device and Android version
- Logcat output (if possible)

---

### 2. ✨ Suggest Features

[Request a feature](https://github.com/david0154/david-ai/issues/new?template=feature_request.md)

**Good feature requests include:**
- Clear description of the feature
- Why it would be useful
- How it should work
- Examples or mockups

---

### 3. 💻 Submit Code

**Areas to contribute:**
- Bug fixes
- New features
- Performance improvements
- Code refactoring
- Test coverage

---

### 4. 📝 Improve Documentation

**Documentation contributions:**
- Fix typos or unclear wording
- Add examples or tutorials
- Improve README
- Update wiki pages
- Add code comments

---

### 5. 🌍 Translate

**Translation contributions:**
- Add new languages
- Improve existing translations
- Fix translation errors
- Add regional variations

[See Translation Guidelines](#translation-guidelines)

---

### 6. 🎨 Design

**Design contributions:**
- UI/UX improvements
- Icon designs
- Logo variations
- App screenshots
- Marketing materials

---

## 🚀 Development Setup

### Prerequisites

- Android Studio Hedgehog or later
- JDK 17+
- Android SDK 26-34
- Git

### Setup Steps

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
open -a "Android Studio" .  # macOS

# 6. Wait for Gradle sync

# 7. Make your changes

# 8. Test your changes
./gradlew test
./gradlew connectedAndroidTest

# 9. Commit and push
git add .
git commit -m "feat: add your feature"
git push origin feature/your-feature-name

# 10. Create Pull Request on GitHub
```

**Detailed instructions:** [Building from Source](Building-from-Source)

---

## 📏 Coding Standards

### Kotlin Style Guide

We follow the [official Kotlin style guide](https://kotlinlang.org/docs/coding-conventions.html).

**Key points:**

**1. Naming:**
```kotlin
// Classes: PascalCase
class VoiceController

// Functions: camelCase
fun processVoiceCommand()

// Constants: SCREAMING_SNAKE_CASE
const val MAX_RETRIES = 3

// Variables: camelCase
val userName = "David"
```

**2. Formatting:**
```kotlin
// Indentation: 4 spaces
fun example() {
    if (condition) {
        doSomething()
    }
}

// Line length: 120 characters max

// Blank lines: One blank line between members
class Example {
    fun function1() { }
    
    fun function2() { }
}
```

**3. Documentation:**
```kotlin
/**
 * Processes voice commands and executes corresponding actions.
 *
 * @param command The voice command to process
 * @param context Android context for system access
 * @return Result of command execution
 */
fun processCommand(command: String, context: Context): CommandResult {
    // Implementation
}
```

---

### Android Best Practices

**1. Architecture:**
- Follow MVVM pattern
- Use ViewModels for UI logic
- Use Repositories for data access
- Keep Activities/Fragments lightweight

**2. Resources:**
- Extract strings to `strings.xml`
- Extract dimensions to `dimens.xml`
- Extract colors to `colors.xml`
- Use vector drawables when possible

**3. Performance:**
- Avoid blocking UI thread
- Use coroutines for async operations
- Release resources when done
- Optimize battery usage

---

### Testing

**Write tests for:**
- New features
- Bug fixes
- Critical functionality

**Test structure:**
```kotlin
@Test
fun testVoiceCommandProcessing() {
    // Given
    val command = "turn on wifi"
    val controller = VoiceController(context)
    
    // When
    val result = controller.processCommand(command)
    
    // Then
    assertTrue(result.isSuccessful)
    assertEquals("wifi_on", result.action)
}
```

---

## 💬 Commit Guidelines

### Commit Message Format

We use [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- **feat**: New feature
- **fix**: Bug fix
- **docs**: Documentation changes
- **style**: Code style changes (formatting, etc.)
- **refactor**: Code refactoring
- **perf**: Performance improvements
- **test**: Adding or updating tests
- **build**: Build system changes
- **ci**: CI/CD changes
- **chore**: Other changes

### Examples

```bash
# Feature
git commit -m "feat(voice): add support for custom wake words"

# Bug fix
git commit -m "fix(gesture): resolve pointer not showing issue"

# Documentation
git commit -m "docs(readme): update installation instructions"

# Multi-line commit
git commit -m "feat(language): add Assamese language support

- Add Assamese translations
- Add Assamese voice model
- Update language selection UI

Closes #123"
```

---

## 🔄 Pull Request Process

### Before Submitting

**1. Sync with upstream:**
```bash
git fetch upstream
git rebase upstream/main
```

**2. Test your changes:**
```bash
./gradlew test
./gradlew connectedAndroidTest
./gradlew lint
```

**3. Update documentation:**
- Update README if needed
- Add/update code comments
- Update wiki pages if needed

---

### Creating Pull Request

**1. Title:**
- Follow commit message format
- Be clear and descriptive
- Examples:
  - `feat: add Punjabi language support`
  - `fix: resolve gesture control crash on Android 12`
  - `docs: improve voice commands documentation`

**2. Description:**

Use this template:

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
Describe tests performed

## Screenshots
(if applicable)

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Comments added for complex code
- [ ] Documentation updated
- [ ] Tests added/updated
- [ ] All tests passing
- [ ] No new warnings
```

**3. Link Issues:**
- Reference related issues
- Use `Closes #123` to auto-close issues

---

### Review Process

**1. Automated Checks:**
- CI/CD builds APK
- Runs tests
- Checks code style
- Must pass to merge

**2. Code Review:**
- Maintainer reviews code
- May request changes
- Address feedback
- Push updates

**3. Approval & Merge:**
- After approval, maintainer merges
- PR branch can be deleted

---

## 🌍 Translation Guidelines

### Adding New Language

**1. Check if already supported:**
- See [Languages](Languages) page
- 15 languages currently supported

**2. Request language addition:**
- Create feature request
- Include language details
- Volunteer to translate

**3. Translation process:**

```bash
# 1. Create strings resource file
app/src/main/res/values-[lang]/strings.xml

# Example for French (fr):
app/src/main/res/values-fr/strings.xml
```

**4. Translate all strings:**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">D.A.V.I.D AI</string>
    <string name="voice_control">Contrôle vocal</string>
    <!-- ... more translations ... -->
</resources>
```

**5. Test translations:**
- Change device language
- Open app
- Verify all text is translated
- Check for UI layout issues

---

### Translation Best Practices

**1. Context matters:**
- Understand the context of each string
- Ask if unclear

**2. Length considerations:**
- Some languages are longer/shorter
- Ensure text fits in UI
- Test on device

**3. Cultural sensitivity:**
- Respect cultural differences
- Adapt examples when needed
- Consult native speakers

**4. Consistency:**
- Use same terms throughout
- Follow existing style
- Maintain tone

---

## 🐛 Bug Reporting

### Before Reporting

1. **Search existing issues:** Maybe already reported
2. **Update app:** Bug might be fixed
3. **Reproduce:** Ensure it's reproducible

### Bug Report Template

**Use:** [Bug Report Template](https://github.com/david0154/david-ai/issues/new?template=bug_report.md)

**Include:**
- Clear title
- Steps to reproduce
- Expected behavior
- Actual behavior
- Screenshots/video
- Device info (model, Android version)
- App version
- Logcat output

---

## ✨ Feature Requests

### Before Requesting

1. **Search existing requests:** Maybe already requested
2. **Check roadmap:** Maybe already planned
3. **Consider scope:** Is it feasible?

### Feature Request Template

**Use:** [Feature Request Template](https://github.com/david0154/david-ai/issues/new?template=feature_request.md)

**Include:**
- Clear description
- Use case
- Why it's useful
- How it should work
- Examples or mockups

---

## 📞 Contact

### Get Help

- 📧 **Email:** [david@nexuzy.in](mailto:david@nexuzy.in)
- 💬 **Discussions:** [GitHub Discussions](https://github.com/david0154/david-ai/discussions)
- 🐛 **Issues:** [GitHub Issues](https://github.com/david0154/david-ai/issues)

### Community

- Follow updates on GitHub
- Star the repository
- Share with others
- Join discussions

---

## 🏆 Recognition

**All contributors are recognized:**
- Listed in README contributors section
- Shown on [contributors page](https://github.com/david0154/david-ai/graphs/contributors)
- Mentioned in release notes (for significant contributions)

**Thank you for contributing to D.A.V.I.D AI!** 🎉

---

**© 2026 Nexuzy Tech Ltd.**  
*Open Source • MIT License • Community Driven*
