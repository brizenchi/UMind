// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "UMindSwift",
    platforms: [
        .iOS(.v15),
        .macOS(.v13)
    ],
    products: [
        .library(name: "UMindSwift", targets: ["UMindSwift"])
    ],
    targets: [
        .target(name: "UMindSwift", path: "Sources/UMindSwift")
    ]
)
