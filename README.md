# UMind - Digital Wellness & App Blocking Tool 🎯

[![License: CC BY-NC 4.0](https://img.shields.io/badge/License-CC%20BY--NC%204.0-lightgrey.svg)](https://creativecommons.org/licenses/by-nc/4.0/)
[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org)

## 🌟 About

UMind is an open-source Android application designed to help you reclaim your time and maintain digital wellness by intelligently blocking distracting apps during your focused work periods. Born out of a personal need to reduce social media dependency and return to a more mindful lifestyle, UMind prioritizes your privacy and security through its transparent, open-source approach with no backend dependencies.

### Why UMind?

- **🔒 Privacy First**: No data collection, no backend servers, everything stays on your device
- **🔍 Transparent**: Open source means you can verify exactly what the app does
- **🎯 Effective**: Smart blocking system that respects your time and productivity goals
- **🛡️ Secure**: No black-box software concerns - you can see and audit every line of code

## ✨ Features

### Current Features
- **📱 App Selection**: Choose specific apps to block during focus sessions
- **⏰ Time-Based Restrictions**: Set specific time windows when apps should be blocked
- **🔄 Focus Strategies**: Create multiple focus profiles for different scenarios (work, study, sleep)
- **🚫 Real-time Blocking**: Immediate app blocking with accessibility service integration
- **📊 Usage Insights**: Track your focus patterns and app blocking statistics

### Planned Features (Roadmap)
- **🔢 Usage Limits**: Set daily usage time limits (e.g., max 5 minutes per day)
- **📈 Frequency Control**: Limit number of app opens per day (e.g., maximum 3 times)
- **⏳ Temporary Access**: Emergency override with reason tracking and time limits
- **📋 Detailed Analytics**: Comprehensive usage reports, app open logs, and override reasons
- **🔔 Smart Notifications**: Gentle reminders and progress tracking

## 🚀 Getting Started

### Prerequisites
- Android 7.0 (API level 24) or higher
- Android device with accessibility service support

### Installation

#### Option 1: Build from Source (Recommended)
1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/umind.git
   cd umind
   ```

2. Open the project in Android Studio

3. Build and install:
   ```bash
   ./gradlew assembleDebug
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

#### Option 2: Download APK
Download the latest APK from the [Releases](https://github.com/yourusername/umind/releases) page.

### Setup & Configuration

1. **Install the App**: Follow installation steps above

2. **Enable Accessibility Service**:
   - Open UMind app
   - Go to "Me" tab → Tap "Enable Accessibility Service"
   - Find "UMind" in the accessibility services list
   - Toggle it ON and confirm

3. **Grant Overlay Permission**:
   - In UMind app, tap "Enable Overlay Permission"
   - Allow the permission for blocking notifications

4. **Create Your First Focus Strategy**:
   - Tap the "+" button on the Focus tab
   - Name your strategy (e.g., "Work Focus", "Study Time")
   - Set time window (e.g., 9:00 AM - 6:00 PM)
   - Select apps to block during this period
   - Save and activate your strategy

5. **Test the Setup**:
   - Try opening a blocked app during the configured time
   - You should see a blocking dialog and be redirected to home screen

## 🛠️ Technical Architecture

### Core Components
- **Accessibility Service**: Monitors app launches and enforces blocking
- **DataStore**: Local storage for focus strategies and settings
- **Jetpack Compose**: Modern Android UI framework
- **Repository Pattern**: Clean architecture for data management

### Key Files
- `BlockAccessibilityService.kt`: Core blocking logic
- `FocusRepository.kt`: Data management and strategy handling
- `MainActivity.kt`: Main UI and navigation
- `AndroidManifest.xml`: Permissions and service declarations

### Permissions Required
- `BIND_ACCESSIBILITY_SERVICE`: Monitor and block app launches
- `SYSTEM_ALERT_WINDOW`: Show blocking dialogs
- `PACKAGE_USAGE_STATS`: App usage statistics (optional)

## 🤝 Contributing

We welcome contributions from the community! Whether you're fixing bugs, adding features, or improving documentation, your help makes UMind better for everyone.

### How to Contribute

1. **Fork the Repository**
   ```bash
   git fork https://github.com/yourusername/umind.git
   ```

2. **Create a Feature Branch**
   ```bash
   git checkout -b feature/amazing-feature
   ```

3. **Make Your Changes**
   - Follow the existing code style
   - Add comments for complex logic
   - Test your changes thoroughly

4. **Commit Your Changes**
   ```bash
   git commit -m "Add amazing feature"
   ```

5. **Push to Your Fork**
   ```bash
   git push origin feature/amazing-feature
   ```

6. **Create a Pull Request**
   - Describe your changes clearly
   - Include screenshots for UI changes
   - Reference any related issues

### Development Guidelines

- **Code Style**: Follow Kotlin coding conventions
- **Architecture**: Maintain clean architecture patterns
- **Testing**: Add unit tests for new functionality
- **Documentation**: Update README and code comments
- **Privacy**: Ensure no data leaves the device

### Areas We Need Help With

- 🐛 **Bug Fixes**: Help identify and fix issues
- 🎨 **UI/UX**: Improve the user interface and experience  
- 📱 **Device Compatibility**: Test on various Android devices
- 🌍 **Internationalization**: Add support for more languages
- 📚 **Documentation**: Improve guides and help content
- 🔍 **Testing**: Expand test coverage
- ♿ **Accessibility**: Improve app accessibility features

## 📋 Roadmap

### Phase 1: Core Functionality ✅
- [x] Basic app blocking
- [x] Time-based restrictions
- [x] Multiple focus strategies
- [x] Accessibility service integration

### Phase 2: Enhanced Control 🚧
- [ ] Usage time limits
- [ ] Daily frequency limits  
- [ ] Temporary access with reason tracking
- [ ] Advanced scheduling options

### Phase 3: Analytics & Insights 📊
- [ ] Detailed usage statistics
- [ ] Focus session analytics
- [ ] Progress tracking and goals
- [ ] Export data functionality

### Phase 4: Advanced Features 🔮
- [ ] Smart suggestions based on usage patterns
- [ ] Integration with calendar apps
- [ ] Focus group challenges
- [ ] Mindfulness reminders

## 🐛 Bug Reports & Feature Requests

### Reporting Bugs
1. Check existing [issues](https://github.com/yourusername/umind/issues) first
2. Create a new issue with:
   - Clear description of the problem
   - Steps to reproduce
   - Expected vs actual behavior
   - Device info (Android version, device model)
   - App version and build

### Requesting Features
1. Search existing feature requests
2. Create a new issue with "Feature Request" label
3. Describe the feature and its benefits
4. Include mockups or examples if helpful

## 📄 License

This project is licensed under the **Creative Commons Attribution-NonCommercial 4.0 International License**.

### What this means:
- ✅ **You CAN**: Use, modify, and distribute the code for personal and non-commercial purposes
- ✅ **You CAN**: Fork the project and create your own versions
- ✅ **You CAN**: Contribute back to the original project
- ❌ **You CANNOT**: Use this code for commercial purposes without explicit permission
- ❌ **You CANNOT**: Sell apps based on this code
- ❌ **You CANNOT**: Use this in commercial products or services

See the [LICENSE](LICENSE) file for full details.

### Commercial Use
If you're interested in commercial use, please contact the maintainers to discuss licensing options.

## 🙏 Acknowledgments

- Thanks to the Android development community for excellent documentation
- Inspired by digital wellness advocates and productivity researchers
- Built with love for everyone seeking a healthier relationship with technology

## 📞 Support & Community

- **Issues**: [GitHub Issues](https://github.com/yourusername/umind/issues)
- **Discussions**: [GitHub Discussions](https://github.com/yourusername/umind/discussions)
- **Email**: [kuniseichi@gmail.com](mailto:kuniseichi@gmail.com)

---

**Remember**: The goal isn't to eliminate technology, but to use it more intentionally. UMind helps you create boundaries so you can be present for what matters most. 🌱

*Made with ❤️ by developers who believe in digital wellness and privacy*