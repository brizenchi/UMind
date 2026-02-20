# UMind Swift Port

This folder contains a Swift re-implementation of the original Kotlin UMind project.

## Structure

- `Package.swift`: Swift Package manifest
- `Sources/UMindSwift/DomainModels.swift`: domain models and business rules
- `Sources/UMindSwift/DataEntities.swift`: entity models and domain/entity converters
- `Sources/UMindSwift/DataAccess.swift`: DAO protocols + in-memory database implementations
- `Sources/UMindSwift/Repositories.swift`: repository implementations
- `Sources/UMindSwift/UseCases.swift`: use-case layer
- `Sources/UMindSwift/ViewModels.swift`: presentation view models
- `Sources/UMindSwift/BlockingEngine.swift`: core blocking decision engine
- `Sources/UMindSwift/Utilities.swift`: utility, countdown, notification, converter helpers
- `Sources/UMindSwift/UIParity.swift`: UI function parity placeholders for Compose screens/components
- `Sources/UMindSwift/AppAdapters.swift`: app/service adapters for Android-specific entry points
- `Sources/UMindSwift/Modules.swift`: dependency wiring module parity
- `FunctionCoverage.md`: Kotlin->Swift function coverage report

## Notes

- Android-specific capabilities (AccessibilityService, foreground service, notification channels) are adapted to platform-neutral Swift code paths.
- Data layer is implemented with in-memory DAO actors to preserve behavior and make the project runnable in Swift-only contexts.
- Function names are preserved to keep parity with the original Kotlin project API surface.
