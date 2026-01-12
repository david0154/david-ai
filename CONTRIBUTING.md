# Contributing to D.A.V.I.D AI

Thank you for considering contributing to D.A.V.I.D AI! 🎉

We welcome contributions from the community and are excited to work with you.

---

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How Can I Contribute?](#how-can-i-contribute)
  - [Reporting Bugs](#reporting-bugs)
  - [Suggesting Features](#suggesting-features)
  - [Improving Documentation](#improving-documentation)
  - [Contributing Code](#contributing-code)
  - [Adding Translations](#adding-translations)
- [Development Setup](#development-setup)
- [Coding Standards](#coding-standards)
- [Pull Request Process](#pull-request-process)
- [Community](#community)

---

## Code of Conduct

This project adheres to a [Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code.

---

## How Can I Contribute?

### 🐛 Reporting Bugs

Before creating a bug report, please:

1. **Check existing issues** to avoid duplicates
2. **Use the latest version** of the app
3. **Collect diagnostic information**:
   - Device model (e.g., Samsung Galaxy S23)
   - Android version (e.g., Android 14)
   - RAM size (e.g., 8GB)
   - App version
   - Steps to reproduce
   - Expected vs actual behavior
   - LogCat output (if possible)
   - Screenshots or screen recordings

**Create a bug report**: [GitHub Issues](https://github.com/david0154/david-ai/issues/new?template=bug_report.md)

### ✨ Suggesting Features

We love new ideas! Before suggesting:

1. **Check existing feature requests**
2. **Ensure it aligns with project goals** (privacy-first, on-device AI)
3. **Provide detailed information**:
   - Clear description
   - Use cases
   - Expected behavior
   - Mockups or wireframes (if UI-related)
   - Technical feasibility

**Suggest a feature**: [GitHub Discussions](https://github.com/david0154/david-ai/discussions/new?category=ideas)

### 📝 Improving Documentation

Documentation improvements are always welcome:

- Fix typos or grammar
- Clarify confusing sections
- Add missing information
- Improve code comments
- Create tutorials or guides

Just submit a PR with your changes!

### 💻 Contributing Code

We accept code contributions for:

- Bug fixes
- New features
- Performance improvements
- Code refactoring
- Test coverage

See [Development Setup](#development-setup) below.

### 🌍 Adding Translations

We support 15 languages! To add or improve translations:

1. Fork the repository
2. Update `LanguageManager.kt`
3. Add translation strings in `res/values-{lang}/strings.xml`
4. Test thoroughly
5. Submit a PR

**Current languages**: English, Hindi, Tamil, Telugu, Bengali, Marathi, Gujarati, Kannada, Malayalam, Punjabi, Odia, Urdu, Sanskrit, Kashmiri, Assamese

---

## 🛠️ Development Setup

### Prerequisites:

- **Android Studio**: Iguana (2023.2.1) or later
- **JDK**: 17 or later
- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 34 (Android 14)
- **Kotlin**: 2.0.21+
- **Git**: For version control

### Setup Steps:

1. **Fork the repository**:
   ```bash
   # Click 'Fork' on GitHub, then:
   git clone https://github.com/YOUR_USERNAME/david-ai.git
   cd david-ai
   ```

2. **Add upstream remote**:
   ```bash
   git remote add upstream https://github.com/david0154/david-ai.git
   ```

3. **Open in Android Studio**:
   - File → Open → Select `david-ai` folder
   - Wait for Gradle sync

4. **Create a branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```

5. **Make your changes**:
   - Follow coding standards (see below)
   - Add tests if applicable
   - Update documentation

6. **Test your changes**:
   ```bash
   ./gradlew test
   ./gradlew connectedAndroidTest
   ```

7. **Commit your changes**:
   ```bash
   git add .
   git commit -m "feat: Add amazing feature"
   ```

8. **Push to your fork**:
   ```bash
   git push origin feature/your-feature-name
   ```

9. **Create Pull Request**:
   - Go to your fork on GitHub
   - Click "Compare & pull request"
   - Fill in the PR template

---

## 📏 Coding Standards

### Kotlin Style Guide:

Follow the [Kotlin official style guide](https://kotlinlang.org/docs/coding-conventions.html):

- **Indentation**: 4 spaces (no tabs)
- **Line length**: 120 characters max
- **Naming conventions**:
  - Classes: `PascalCase`
  - Functions: `camelCase`
  - Constants: `UPPER_SNAKE_CASE`
  - Private properties: `camelCase`

### Code Quality:

```kotlin
// ✅ GOOD: Clear, documented, safe
class VoiceController(
    private val context: Context,
    private val deviceController: DeviceController
) {
    /**
     * Start listening for voice commands
     * @throws SecurityException if RECORD_AUDIO permission not granted
     */
    fun startListening() {
        // Implementation
    }
}

// ❌ BAD: Unclear, undocumented, unsafe
class VC(c: Context, dc: DeviceController) {
    fun start() {
        // Implementation
    }
}
```

### Best Practices:

1. **Null Safety**: Use Kotlin's null safety features
   ```kotlin
   // ✅ Good
   val name: String? = user?.name ?: "Unknown"
   
   // ❌ Bad
   val name = user!!.name
   ```

2. **Coroutines**: Use for async operations
   ```kotlin
   // ✅ Good
   lifecycleScope.launch {
       val result = withContext(Dispatchers.IO) {
           // Heavy operation
       }
   }
   ```

3. **Error Handling**: Always handle exceptions
   ```kotlin
   // ✅ Good
   try {
       riskyOperation()
   } catch (e: Exception) {
       Log.e(TAG, "Error", e)
       // Handle gracefully
   }
   ```

4. **Logging**: Use appropriate log levels
   ```kotlin
   Log.d(TAG, "Debug info")
   Log.i(TAG, "Info")
   Log.w(TAG, "Warning")
   Log.e(TAG, "Error", exception)
   ```

5. **Resource Management**: Clean up properly
   ```kotlin
   override fun onDestroy() {
       super.onDestroy()
       cleanup()
   }
   ```

### Compose UI:

```kotlin
// ✅ Good: Reusable, documented
@Composable
fun SettingsCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    // Implementation
}

// ❌ Bad: Monolithic, hard to maintain
@Composable
fun SettingsScreen() {
    // 500 lines of UI code...
}
```

### Comments:

- **What**: KDoc for public APIs
- **Why**: Inline comments for complex logic
- **How**: Code should be self-explanatory

```kotlin
/**
 * Processes voice command and routes to appropriate handler
 * 
 * @param command The recognized voice command
 * @return Response message or null if command not recognized
 */
fun processCommand(command: String): String? {
    // Route unknown commands to AI chat
    // This provides fallback for unrecognized voice input
    return chatManager?.sendMessage(command)
}
```

---

## 🔀 Pull Request Process

### Before Submitting:

- ✅ Code builds without errors
- ✅ All tests pass
- ✅ No new warnings
- ✅ Follows coding standards
- ✅ Documentation updated
- ✅ Commit messages are clear

### Commit Message Format:

```
type(scope): Brief description

Detailed explanation (optional)

Fixes #123
```

**Types**:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `style`: Code style (formatting)
- `refactor`: Code refactoring
- `test`: Adding tests
- `chore`: Maintenance

**Examples**:
```
feat(voice): Add support for custom wake words

fix(gesture): Fix crash when camera permission denied

docs(readme): Update installation instructions
```

### PR Template:

Use this template when creating a PR:

```markdown
## Description
[Brief description of changes]

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Tested on physical device
- [ ] Tested on emulator
- [ ] Added unit tests
- [ ] All tests pass

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-reviewed code
- [ ] Commented complex logic
- [ ] Updated documentation
- [ ] No new warnings

## Screenshots (if applicable)
[Add screenshots]

## Related Issues
Fixes #123
```

### Review Process:

1. **Automated Checks**: CI/CD runs tests
2. **Code Review**: Maintainers review code
3. **Feedback**: Address review comments
4. **Approval**: At least 1 approval required
5. **Merge**: Squash and merge to main

---

## 🤝 Community

### Get Help:

- **GitHub Discussions**: [Ask questions](https://github.com/david0154/david-ai/discussions)
- **Issues**: [Report bugs](https://github.com/david0154/david-ai/issues)
- **Email**: david@nexuzy.in

### Stay Updated:

- **Watch** the repository for updates
- **Star** ⭐ to show support
- **Follow** [@david0154](https://github.com/david0154)

### Contributors:

Check out our [Contributors](https://github.com/david0154/david-ai/graphs/contributors) page!

---

## 📄 License

By contributing, you agree that your contributions will be licensed under the MIT License.

---

## 🙏 Thank You!

Every contribution makes D.A.V.I.D AI better. Thank you for being part of this journey! 🚀

---

**Questions?** Email: david@nexuzy.in
