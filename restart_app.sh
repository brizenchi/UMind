#!/bin/bash
# 重启应用但不强制停止(保留无障碍权限)
adb shell am start -S com.example.umind/.MainActivity
