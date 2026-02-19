#!/bin/bash

echo "========================================="
echo "清理旧包名并重新安装"
echo "========================================="
echo ""

# 检查 adb 是否可用
if ! command -v adb &> /dev/null; then
    echo "❌ 错误：未找到 adb 命令"
    exit 1
fi

# 检查设备连接
DEVICE=$(adb devices | grep -w "device" | head -1 | awk '{print $1}')
if [ -z "$DEVICE" ]; then
    echo "❌ 未检测到设备"
    exit 1
fi
echo "✅ 设备已连接: $DEVICE"
echo ""

# 1. 卸载旧包名的应用
echo "1. 卸载旧包名应用 (com.example.focus)..."
adb uninstall com.example.focus 2>/dev/null
if [ $? -eq 0 ]; then
    echo "✅ 已卸载旧应用"
else
    echo "ℹ️  旧应用不存在或已卸载"
fi
echo ""

# 2. 卸载新包名的应用（如果存在）
echo "2. 卸载当前应用 (com.example.umind)..."
adb uninstall com.example.umind 2>/dev/null
if [ $? -eq 0 ]; then
    echo "✅ 已卸载当前应用"
else
    echo "ℹ️  当前应用不存在"
fi
echo ""

# 3. 清理 Android Studio 缓存
echo "3. 清理构建缓存..."
cd "$(dirname "$0")"
./gradlew clean
echo "✅ 构建缓存已清理"
echo ""

# 4. 重新构建并安装
echo "4. 重新构建并安装应用..."
./gradlew installDebug
if [ $? -eq 0 ]; then
    echo "✅ 应用安装成功"
else
    echo "❌ 应用安装失败"
    exit 1
fi
echo ""

# 5. 验证安装
echo "5. 验证安装..."
if adb shell pm list packages | grep -q "com.example.umind"; then
    echo "✅ 应用已正确安装"
    VERSION=$(adb shell dumpsys package com.example.umind | grep versionName | head -1)
    echo "   $VERSION"
else
    echo "❌ 应用未找到"
    exit 1
fi
echo ""

echo "========================================="
echo "✅ 完成！现在可以启动应用了"
echo "========================================="
echo ""
echo "提示：首次启动后需要重新授予以下权限："
echo "  1. 无障碍服务权限"
echo "  2. 弹窗权限"
echo "  3. 电池优化豁免"
