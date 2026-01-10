# Contributing to D.A.V.I.D AI

**Welcome! We're excited that you want to contribute to D.A.V.I.D AI!**

---

## 🎯 Table of Contents

1. [Code of Conduct](#code-of-conduct)
2. [How Can I Contribute?](#how-can-i-contribute)
3. [Getting Started](#getting-started)
4. [Development Workflow](#development-workflow)
5. [Coding Standards](#coding-standards)
6. [Submitting Changes](#submitting-changes)
7. [Review Process](#review-process)

---

## Code of Conduct

### Our Pledge

We are committed to providing a welcoming and inspiring community for all. We pledge to:

- ✅ Be respectful and inclusive
- ✅ Welcome newcomers warmly
- ✅ Accept constructive criticism
- ✅ Focus on what's best for the community
- ✅ Show empathy towards others

### Unacceptable Behavior

- ❌ Harassment or discriminatory language
- ❌ Trolling or insulting comments
- ❌ Personal or political attacks
- ❌ Publishing others' private information
- ❌ Unprofessional conduct

**Enforcement:** Violations may result in temporary or permanent ban.

---

## How Can I Contribute?

### 🐛 Reporting Bugs

**Found a bug?** Help us fix it!

1. Check [existing issues](https://github.com/david0154/david-ai/issues)
2. If not found, [create new bug report](https://github.com/david0154/david-ai/issues/new?template=bug_report.md)
3. Include:
   - Device model & Android version
   - RAM size
   - Steps to reproduce
   - Expected vs actual behavior
   - LogCat output (if possible)
   - Screenshots

### ✨ Suggesting Features

**Have an idea?** We'd love to hear it!

1. Check [existing feature requests](https://github.com/david0154/david-ai/issues?q=is%3Aissue+label%3Aenhancement)
2. [Create feature request](https://github.com/david0154/david-ai/issues/new?template=feature_request.md)
3. Describe:
   - The feature
   - Why it's useful
   - How it should work
   - Examples/mockups

### 🌍 Translating

**Speak another language?** Help us reach more users!

**Current Languages (15):**
English, Hindi, Tamil, Telugu, Bengali, Marathi, Gujarati, Kannada, Malayalam, Punjabi, Odia, Urdu, Sanskrit, Kashmiri, Assamese

**Add New Language:**

1. Create `app/src/main/res/values-{lang}/strings.xml`
2. Translate all strings from `values/strings.xml`
3. Test on device
4. Submit pull request

### 📝 Improving Documentation

**Good at explaining?** Help others understand!

**Areas to improve:**
- README clarity
- Wiki pages
- Code comments
- API documentation
- Tutorial videos

### 💻 Writing Code

**Developer?** Jump right in!

**Good First Issues:**
- Look for [`good first issue`](https://github.com/david0154/david-ai/labels/good%20first%20issue) label
- Simple bug fixes
- UI improvements
- Adding tests

---

## Getting Started

### Prerequisites

- Java JDK 17+
- Android Studio Hedgehog+
- Git
- GitHub account

### Fork & Clone

```bash
# 1. Fork the repository on GitHub
# Click "Fork" button at top right

# 2. Clone YOUR fork
git clone https://github.com/YOUR_USERNAME/david-ai.git
cd david-ai

# 3. Add upstream remote
git remote add upstream https://github.com/david0154/david-ai.git

# 4. Verify remotes
git remote -v
# Should show:
# origin    https://github.com/YOUR_USERNAME/david-ai.git (fetch)
# origin    https://github.com/YOUR_USERNAME/david-ai.git (push)
# upstream  https://github.com/david0154/david-ai.git (fetch)
# upstream  https://github.com/david0154/david-ai.git (push)
```

### Set Up Development Environment

1. Open project in Android Studio
2. Wait for Gradle sync (5-10 minutes)
3. Build the app:
   ```bash
   ./gradlew assembleDebug
   ```
4. Run tests:
   ```bash
   ./gradlew test
   ```

---

## Development Workflow

### Create Feature Branch

```bash
# Update main branch
git checkout main
git pull upstream main

# Create feature branch
git checkout -b feature/my-awesome-feature

# Or for bug fix
git checkout -b fix/bug-description
```

**Branch Naming:**
- `feature/` - New features
- `fix/` - Bug fixes
- `docs/` - Documentation
- `refactor/` - Code refactoring
- `test/` - Adding tests

### Make Changes

1. **Write code** following [coding standards](#coding-standards)
2. **Add tests** for new features
3. **Update documentation** if needed
4. **Test thoroughly**:
   ```bash
   ./gradlew test
   ./gradlew connectedAndroidTest
   ```

### Commit Changes

**Commit Message Format:**

```
<type>: <subject>

<body>

<footer>
```

**Types:**
- `feat` - New feature
- `fix` - Bug fix
- `docs` - Documentation
- `style` - Formatting
- `refactor` - Code restructuring
- `test` - Adding tests
- `chore` - Maintenance

**Example:**

```bash
git add .
git commit -m "feat: Add dark mode support

Implemented dark mode theme switching in settings.
Added day/night theme detection.
Updated all UI components.

Closes #123"
```

### Push Changes

```bash
# Push to your fork
git push origin feature/my-awesome-feature
```

---

## Coding Standards

### Kotlin Style Guide

**Follow official [Kotlin Style Guide](https://kotlinlang.org/docs/coding-conventions.html)**

**Key Points:**

```kotlin
// 1. Class names: PascalCase
class VoiceController

// 2. Function names: camelCase
fun processVoiceCommand()

// 3. Constants: UPPER_SNAKE_CASE
const val MAX_RETRY_COUNT = 3

// 4. Properties: camelCase
val userName: String

// 5. Use explicit types for public API
fun getUserName(): String { ... }

// 6. Prefer val over var
val immutableValue = "constant"

// 7. Use named arguments for clarity
fun createUser(
    name: String,
    age: Int,
    email: String
)

// 8. Single expression functions
fun double(x: Int): Int = x * 2

// 9. Use when instead of if-else chains
when (value) {
    0 -> "zero"
    1 -> "one"
    else -> "many"
}

// 10. Null safety
val length: Int = text?.length ?: 0
```

### Code Organization

**Package Structure:**

```
com.nexuzy.david/
├── ai/               # AI models & processing
│   ├── voice/
│   ├── chat/
│   ├── vision/
│   └── gesture/
├── ui/               # UI components
│   ├── screens/
│   ├── components/
│   └── theme/
├── data/             # Data layer
│   ├── repository/
│   ├── local/
│   └── model/
├── domain/           # Business logic
│   ├── usecase/
│   └── model/
├── service/          # Background services
├── util/             # Utilities
└── MainActivity.kt
```

### Documentation

**Document public APIs:**

```kotlin
/**
 * Processes voice command and executes corresponding action.
 *
 * @param audioData Raw audio data from microphone
 * @param language Target language for recognition (default: English)
 * @return CommandResult containing transcription and execution status
 * @throws ModelNotLoadedException if Whisper model is not initialized
 */
fun processVoiceCommand(
    audioData: ByteArray,
    language: String = "en"
): CommandResult
```

### Testing

**Write tests for:**
- All public functions
- Edge cases
- Error conditions

**Example:**

```kotlin
@Test
fun testVoiceCommandProcessing() {
    // Given
    val controller = VoiceController(context)
    val audioData = loadTestAudio("test_command.wav")
    
    // When
    val result = controller.processVoiceCommand(audioData)
    
    // Then
    assertTrue(result.isSuccess)
    assertEquals("open settings", result.transcription)
}
```

---

## Submitting Changes

### Create Pull Request

1. **Push to your fork:**
   ```bash
   git push origin feature/my-awesome-feature
   ```

2. **Go to GitHub** and click "Compare & pull request"

3. **Fill PR template:**
   - Clear title
   - Description of changes
   - Related issues (e.g., "Closes #123")
   - Screenshots (for UI changes)
   - Testing done

4. **Ensure CI passes:**
   - All tests pass
   - No lint errors
   - Build succeeds

### Pull Request Checklist

**Before submitting:**

- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Comments added for complex code
- [ ] Documentation updated
- [ ] Tests added/updated
- [ ] All tests pass locally
- [ ] No new warnings
- [ ] Commit messages are clear

---

## Review Process

### What to Expect

1. **Automated Checks** (< 5 minutes)
   - Builds successfully
   - Tests pass
   - Lint checks pass

2. **Code Review** (1-3 days)
   - Maintainers review code
   - May request changes
   - Discussion on approach

3. **Revisions** (if needed)
   - Make requested changes
   - Push to same branch
   - PR updates automatically

4. **Approval & Merge**
   - At least one approval required
   - Squash and merge to main
   - Branch deleted

### Responding to Feedback

**Good practices:**

✅ Respond to all comments  
✅ Ask questions if unclear  
✅ Be open to suggestions  
✅ Make requested changes  
✅ Mark resolved conversations  

**Make changes:**

```bash
# Make changes based on feedback
git add .
git commit -m "fix: Address PR feedback"
git push origin feature/my-awesome-feature
```

---

## Development Tips

### Sync with Upstream

**Keep your fork updated:**

```bash
# Fetch upstream changes
git fetch upstream

# Switch to main
git checkout main

# Merge upstream changes
git merge upstream/main

# Push to your fork
git push origin main
```

### Rebase Feature Branch

**Keep feature branch up-to-date:**

```bash
# Update main first
git checkout main
git pull upstream main

# Switch to feature branch
git checkout feature/my-awesome-feature

# Rebase on main
git rebase main

# Force push (if already pushed)
git push origin feature/my-awesome-feature --force-with-lease
```

### Debug Build Issues

```bash
# Clean build
./gradlew clean

# Build with stacktrace
./gradlew assembleDebug --stacktrace

# Run with info logging
./gradlew assembleDebug --info
```

---

## Recognition

### Contributor Credits

All contributors are:
- ✅ Listed in README
- ✅ Shown in contributor graph
- ✅ Credited in release notes
- ✅ Thanked in announcements

### Special Recognition

**Outstanding contributors may receive:**
- 🏆 Contributor badge
- 📢 Feature in blog post
- 🎁 Swag (future)
- 🤝 Invitation to core team

---

## Community

### Communication Channels

- 💬 [GitHub Discussions](https://github.com/david0154/david-ai/discussions)
- 📧 Email: [david@nexuzy.in](mailto:david@nexuzy.in)
- 🐛 [Issue Tracker](https://github.com/david0154/david-ai/issues)

### Getting Help

**Stuck? We're here to help!**

1. Check [FAQ](FAQ)
2. Search [existing issues](https://github.com/david0154/david-ai/issues)
3. Ask in [Discussions](https://github.com/david0154/david-ai/discussions)
4. Email us: [david@nexuzy.in](mailto:david@nexuzy.in)

---

## Resources

- 📖 [Building from Source](Building-from-Source)
- 🏗️ [Architecture](Architecture)
- 📚 [API Reference](API-Reference)
- ❓ [FAQ](FAQ)
- 🐛 [Troubleshooting](Troubleshooting)

---

## Thank You!

**Every contribution makes D.A.V.I.D AI better!**

Whether you:
- 🐛 Report a bug
- 💡 Suggest a feature
- 💻 Write code
- 📝 Improve docs
- 🌍 Translate
- ⭐ Star the repo
- 📢 Spread the word

**You're awesome! Thank you for contributing!** 🙏

---

**© 2026 Nexuzy Tech Ltd.**  
**Questions?** [david@nexuzy.in](mailto:david@nexuzy.in)
