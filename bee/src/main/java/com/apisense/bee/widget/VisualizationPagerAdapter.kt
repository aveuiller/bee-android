package com.apisense.bee.widget

import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup

import io.apisense.sting.visualization.widget.VisualizationView

class VisualizationPagerAdapter(private val visualizations: List<View>) : PagerAdapter() {

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val ret = visualizations[position]
        collection.addView(ret)
        return ret
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun getCount(): Int {
        return visualizations.size
    }

    override fun getPageTitle(position: Int): CharSequence {
        return ""
    }

    fun invalidateView(position: Int) {
        val ret = visualizations[position]
        if (ret is VisualizationView) {
            ret.notifyDataChanged()
        } else {
            ret.invalidate()
        }
    }
}
