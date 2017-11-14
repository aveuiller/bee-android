package com.apisense.bee.ui.layout

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

class SensorsLayout : ViewGroup {
    private var lineHeight: Int = 0

    class LayoutParams(width: Int, height: Int) : ViewGroup.LayoutParams(width, height)

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = View.MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight

        val count = childCount

        var xPosition = paddingLeft
        var yPosition = paddingTop

        for (i in 0 until count) {
            val child = getChildAt(i)

            if (child.visibility == View.GONE) {
                continue
            }

            val layoutParams = child.layoutParams as LayoutParams

            lineHeight = Math.max(lineHeight, layoutParams.height)

            if (xPosition + layoutParams.width > width) {
                xPosition = paddingLeft
                yPosition += layoutParams.height
            }

            xPosition = xPosition + layoutParams.width
        }

        val height = yPosition + lineHeight

        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val count = childCount
        val width = right - left
        var xPosition = paddingLeft
        var yPosition = paddingTop


        for (i in 0 until count) {
            val child = getChildAt(i)

            if (child.visibility == View.GONE) {
                continue
            }

            val layoutParams = child.layoutParams as LayoutParams

            if (xPosition + layoutParams.width > width) {
                xPosition = paddingLeft
                yPosition = yPosition + lineHeight
            }

            child.layout(xPosition, yPosition, xPosition + layoutParams.width, yPosition + layoutParams.height)

            xPosition = xPosition + layoutParams.width
        }
    }
}
