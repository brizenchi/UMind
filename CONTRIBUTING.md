# Contributing to UMind 🤝

Thank you for your interest in contributing to UMind! This document provides guidelines and information for contributors.

## 🎯 Project Mission

UMind is dedicated to helping people maintain digital wellness and reclaim their time from distracting applications. We believe in:

- **Privacy First**: No data collection or backend dependencies
- **Transparency**: Open source development with clear, auditable code
- **Effectiveness**: Simple yet powerful tools for digital wellness
- **Community**: Collaborative development that benefits everyone

## 🚀 Getting Started

### Development Environment Setup

1. **Prerequisites**:
   - Android Studio Arctic Fox or later
   - Android SDK (API 24+)
   - Kotlin plugin
   - Git

2. **Fork and Clone**:
   ```bash
   git fork https://github.com/yourusername/umind.git
   git clone https://github.com/yourusername/umind.git
   cd umind
   ```

3. **Open in Android Studio**:
   - Import the project
   - Sync Gradle files
   - Ensure all dependencies are resolved

4. **Run the App**:
   - Connect an Android device or start an emulator
   - Build and run the debug version

### Project Structure

```
umind/
├── app/
│   ├── src/main/java/com/example/focus/
│   │   ├── BlockAccessibilityService.kt    # Core blocking logic
│   │   ├── FocusRepository.kt              # Data management
│   │   ├── MainActivity.kt                 # Main UI
│   │   ├── data/                           # Data models
│   │   ├── ui/                             # UI components
│   │   └── viewmodel/                      # ViewModels
│   ├── src/main/res/                       # Resources
│   └── AndroidManifest.xml                 # App manifest
├── README.md
├── LICENSE
└── CONTRIBUTING.md
```

## 📋 How to Contribute

### Types of Contributions

We welcome various types of contributions:

- 🐛 **Bug Reports**: Help us identify and fix issues
- ✨ **Feature Requests**: Suggest new functionality
- 💻 **Code Contributions**: Bug fixes, features, optimizations
- 📚 **Documentation**: Improve guides, comments, and help content
- 🎨 **Design**: UI/UX improvements and accessibility enhancements
- 🧪 **Testing**: Write tests and test on different devices
- 🌍 **Translations**: Add support for more languages

### Bug Reports

Before submitting a bug report:

1. **Search existing issues** to avoid duplicates
2. **Test with the latest version**
3. **Reproduce the issue** consistently

When creating a bug report, include:

```markdown
**Describe the bug**
A clear and concise description of what the bug is.

**To Reproduce**
Steps to reproduce the behavior:
1. Go to '...'
2. Click on '....'
3. Scroll down to '....'
4. See error

**Expected behavior**
A clear description of what you expected to happen.

**Screenshots**
If applicable, add screenshots to help explain your problem.

**Device Information:**
 - Device: [e.g. Samsung Galaxy S21]
 - Android Version: [e.g. Android 12]
 - App Version: [e.g. 1.0.0]

**Additional context**
Add any other context about the problem here.
```

### Feature Requests

For feature requests:

1. **Check existing requests** first
2. **Describe the problem** you're trying to solve
3. **Explain your proposed solution**
4. **Consider alternatives** you've thought about
5. **Add mockups or examples** if helpful

### Code Contributions

#### Before You Start

1. **Create an issue** or comment on an existing one
2. **Get feedback** from maintainers before major changes
3. **Fork the repository** and create a feature branch

#### Coding Standards

- **Language**: Kotlin for Android development
- **Architecture**: Follow existing MVVM/Repository patterns
- **Naming**: Use clear, descriptive names
- **Comments**: Add comments for complex logic
- **Formatting**: Use Android Studio's default formatting

#### Code Style Guidelines

```kotlin
// Good: Clear, descriptive naming
class FocusStrategy(
    val id: String,
    val name: String,
    val startHour: Int,
    val startMinute: Int
)

// Good: Proper documentation
/**
 * Checks if an app should be blocked based on active focus strategies
 * @param packageName The package name of the app to check
 * @return true if the app should be blocked, false otherwise
 */
fun isAppBlockedNow(packageName: String): Boolean {
    // Implementation here
}

// Good: Consistent error handling
try {
    // Risky operation
} catch (e: Exception) {
    Log.e("FocusRepository", "Error performing operation", e)
    return false
}
```

#### Pull Request Process

1. **Create a feature branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes**:
   - Follow coding standards
   - Add tests if applicable
   - Update documentation

3. **Test thoroughly**:
   - Test on multiple devices if possible
   - Verify accessibility service functionality
   - Check different Android versions

4. **Commit your changes**:
   ```bash
   git add .
   git commit -m "feat: add amazing new feature"
   ```

5. **Push to your fork**:
   ```bash
   git push origin feature/your-feature-name
   ```

6. **Create a Pull Request**:
   - Use a clear, descriptive title
   - Reference related issues
   - Describe your changes
   - Add screenshots for UI changes

#### Commit Message Format

Use conventional commit format:

- `feat:` New features
- `fix:` Bug fixes
- `docs:` Documentation changes
- `style:` Code style changes
- `refactor:` Code refactoring
- `test:` Test additions or modifications
- `chore:` Maintenance tasks

Examples:
```
feat: add usage time limits for focus strategies
fix: resolve accessibility service crash on Android 12
docs: update installation instructions
style: format code according to Kotlin conventions
```

## 🧪 Testing

### Manual Testing

- Test on various Android versions (minimum API 24)
- Test accessibility service functionality
- Test different device configurations
- Verify permissions work correctly

### Automated Testing

- Add unit tests for new functionality
- Update existing tests when modifying code
- Ensure all tests pass before submitting PR

## 📚 Documentation

### Code Documentation

- Add KDoc comments for public functions
- Explain complex algorithms or business logic
- Update README if adding new features

### User Documentation

- Update help content for new features
- Add screenshots for UI changes
- Consider creating video tutorials

## 🌍 Internationalization

To add support for a new language:

1. Create new `values-{language_code}/strings.xml`
2. Translate all string resources
3. Test UI layout with translated text
4. Update README with supported languages

## 🔒 Security & Privacy

### Privacy Guidelines

- **No data collection**: Don't add analytics or tracking
- **Local storage only**: Keep all data on device
- **Minimal permissions**: Only request necessary permissions
- **Transparent functionality**: All features should be clearly explained

### Security Considerations

- Validate all user inputs
- Handle permissions gracefully
- Avoid storing sensitive information
- Use secure coding practices

## 📞 Getting Help

If you need help or have questions:

1. **Check the documentation** first
2. **Search existing issues** and discussions
3. **Create a new discussion** for general questions
4. **Contact maintainers** for urgent matters

### Communication Channels

- **GitHub Issues**: Bug reports and feature requests
- **GitHub Discussions**: General questions and ideas
- **Email**: [kuniseichi@gmail.com] for private matters

## 🏆 Recognition

Contributors will be recognized in:

- README contributors section
- Release notes for significant contributions
- Special thanks for major features or fixes

## 📄 License

By contributing to UMind, you agree that your contributions will be licensed under the same Creative Commons Attribution-NonCommercial 4.0 International License that covers the project.

## 🎉 Thank You!

Every contribution, no matter how small, helps make UMind better for everyone. Thank you for being part of our mission to promote digital wellness and privacy!

---

*Remember: We're building this together to help people have a healthier relationship with technology. Every line of code, every bug report, and every suggestion makes a difference.* 🌱
