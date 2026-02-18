#!/bin/bash

# 调试脚本 - 帮助诊断应用阻止功能

echo "========================================="
echo "专注模式 - 调试工具"
echo "========================================="
echo ""

# 检查 adb 是否可用
if ! command -v adb &> /dev/null; then
    echo "❌ 错误：未找到 adb 命令"
    echo "请安装 Android SDK Platform Tools"
    exit 1
fi

# 检查设备连接
echo "1. 检查设备连接..."
DEVICE=$(adb devices | grep -w "device" | head -1 | awk '{print $1}')
if [ -z "$DEVICE" ]; then
    echo "❌ 未检测到设备，请连接Android设备并启用USB调试"
    exit 1
fi
echo "✅ 设备已连接: $DEVICE"
echo ""

# 检查无障碍服务状态
echo "2. 检查无障碍服务状态..."
ACCESSIBILITY=$(adb shell settings get secure enabled_accessibility_services)
if echo "$ACCESSIBILITY" | grep -q "com.example.focus"; then
    echo "✅ 无障碍服务已启用"
else
    echo "❌ 无障碍服务未启用"
    echo "   请在设置中启用无障碍服务"
fi
echo ""

# 检查弹窗权限
echo "3. 检查弹窗权限..."
OVERLAY=$(adb shell appops get com.example.focus SYSTEM_ALERT_WINDOW 2>/dev/null)
if echo "$OVERLAY" | grep -q "allow"; then
    echo "✅ 弹窗权限已授予"
else
    echo "❌ 弹窗权限未授予"
    echo "   请在设置中授予弹窗权限"
fi
echo ""

# 检查应用是否安装
echo "4. 检查应用安装状态..."
if adb shell pm list packages | grep -q "com.example.focus"; then
    echo "✅ 应用已安装"
    VERSION=$(adb shell dumpsys package com.example.focus | grep versionName | head -1)
    echo "   $VERSION"
else
    echo "❌ 应用未安装"
fi
echo ""

echo "========================================="
echo "开始监听日志..."
echo "========================================="
echo "提示：现在可以尝试打开被阻止的应用"
echo "按 Ctrl+C 停止监听"
echo ""

# 清空日志并开始监听
adb logcat -c
adb logcat -s BlockAccessibilityService:D FocusRepository:D | while read line; do
    # 高亮重要信息
    if echo "$line" | grep -q "BLOCKING APP"; then
        echo -e "\033[1;31m$line\033[0m"  # 红色加粗
    elif echo "$line" | grep -q "Should block.*true"; then
        echo -e "\033[1;33m$line\033[0m"  # 黄色加粗
    elif echo "$line" | grep -q "Should block.*false"; then
        echo -e "\033[0;36m$line\033[0m"  # 青色
    else
        echo "$line"
    fi
done
