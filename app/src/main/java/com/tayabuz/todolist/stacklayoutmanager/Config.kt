package com.tayabuz.todolist.stacklayoutmanager

import androidx.annotation.FloatRange
import androidx.annotation.IntRange

class Config {

    @IntRange(from = 2)
    var space = 60
    var maxStackCount = 3
    var initialStackCount = 0
    @FloatRange(from = 0.0, to = 1.0)
    var secondaryScale: Float = 0.toFloat()
    @FloatRange(from = 0.0, to = 1.0)
    var scaleRatio: Float = 0.toFloat()
    /**
     * the real scroll distance might meet requirement,
     * so we multiply a factor fro parallax
     */
    @FloatRange(from = 1.0, to = 2.0)
    var parallax = 1f
    internal var align: StackLayoutManager.Align? = null
    var isLinearTransparentGradientEnable = false

}
