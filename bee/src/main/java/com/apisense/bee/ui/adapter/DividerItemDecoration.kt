package com.apisense.bee.ui.adapter

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView

/**
 * RecyclerView does not show any separation between items so we use ItemDecoration
 * @see <a href="http://stackoverflow.com/questions/24618829/how-to-add-dividers-and-spaces-between-items-in-recyclerview">
 *     Stackoverflow: How to add dividers and spaces between items in recycler view</a>
 */
class DividerItemDecoration : RecyclerView.ItemDecoration {
    private val mDivider: Drawable

    /**
     * Default divider will be used
     */
    constructor(context: Context) {
        val styledAttributes = context.obtainStyledAttributes(ATTRS)
        mDivider = styledAttributes.getDrawable(0)
        styledAttributes.recycle()
    }

    /**
     * Custom divider will be used
     */
    constructor(context: Context, resId: Int) {
        mDivider = ContextCompat.getDrawable(context, resId)
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State?) {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight

        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)

            val params = child.layoutParams as RecyclerView.LayoutParams

            val top = child.bottom + params.bottomMargin
            val bottom = top + mDivider.intrinsicHeight

            mDivider.setBounds(left, top, right, bottom)
            mDivider.draw(c)
        }
    }

    companion object {
        private val ATTRS = intArrayOf(android.R.attr.listDivider)
    }
}
