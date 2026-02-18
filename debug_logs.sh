#!/bin/bash

# 调试日志脚本 - 显示应用的关键日志

echo "=== 清除旧日志 ==="
adb logcat -c

echo ""
echo "=== 开始监控日志 ==="
echo "按 Ctrl+C 停止"
echo ""

# 过滤显示关键日志
adb logcat | grep -E "(BlockAccessibilityService|FocusRepository|UsageTracking)" --line-buffered
s