package com.example.umind.ui.theme

import androidx.compose.ui.unit.dp

/**
 * UMind Design System v1.0 - Spacing System
 * 使用 4dp 网格系统
 */
object Spacing {
    val space2 = 2.dp   // 微小间距
    val space4 = 4.dp   // 最小间距
    val space8 = 8.dp   // 小间距
    val space12 = 12.dp // 中小间距
    val space16 = 16.dp // 标准间距 ⭐ 最常用
    val space20 = 20.dp // 中等间距
    val space24 = 24.dp // 大间距
    val space32 = 32.dp // 超大间距
    val space48 = 48.dp // 特大间距
}

/**
 * 组件特定间距
 */
object ComponentSpacing {
    val cardPadding = 20.dp        // 卡片内边距
    val pagePadding = 16.dp        // 页面边距
    val componentSpacing = 16.dp   // 组件间距
    val smallSpacing = 8.dp        // 小组件间距
    val textLineSpacing = 8.dp     // 文本行间距
    val sectionSpacing = 24.dp     // Section 间距
}

/**
 * 圆角半径
 */
object CornerRadius {
    val small = 8.dp
    val medium = 12.dp
    val large = 16.dp
    val extraLarge = 24.dp
}

/**
 * 组件尺寸
 */
object ComponentSize {
    val buttonHeight = 56.dp
    val smallButtonHeight = 48.dp
    val iconSizeSmall = 16.dp
    val iconSizeNormal = 24.dp
    val iconSizeLarge = 32.dp
    val iconSizeXLarge = 48.dp
}
