package com.apisense.bee.utils

import android.content.Context
import android.content.res.Resources
import android.support.v4.content.ContextCompat
import android.util.DisplayMetrics
import android.view.ViewGroup
import android.widget.ImageView
import com.apisense.bee.ui.layout.SensorsLayout
import io.apisense.sting.lib.Sensor
import java.util.*

class SensorsDrawer(private val availableSensors: Set<Sensor>) {
    private val sensorSize: Int = dpToPx(SENSOR_SIZE)
    private val sensorPadding: Int = dpToPx(SENSOR_PADDING)

    /**
     * Empty the content of the given view, then draw all sensors on it,
     * highlighting the ones from usedStings.
     *
     * @param context    The view context.
     * @param view       The [ViewGroup] to draw onto.
     * @param usedStings The list of Stings to highlight.
     */
    fun draw(context: Context, view: ViewGroup, usedStings: List<String>) {
        if (view.childCount != 0) {
            view.removeAllViews()
        }

        drawSensors(context, view, usedStings)
    }

    /**
     * Sort and actual drawing of the sensors, highlighting the ones from usedStings.
     *
     * @param context    The view context.
     * @param view       The [ViewGroup] to draw onto.
     * @param usedStings The list of Stings to highlight.
     */
    private fun drawSensors(context: Context, view: ViewGroup, usedStings: List<String>) {
        for (sensor in asSortedList(availableSensors)) {
            if (usedStings.contains(sensor.stingName)) {
                val sensorView = parametrizedSensorView(context, sensor)
                view.addView(sensorView)
            }
        }
    }

    /**
     * Return the view for one sensor.
     *
     * @param context The view context.
     * @param sensor  The sensor to draw.
     * @return An [ImageView] of the sensor.
     */
    private fun parametrizedSensorView(context: Context, sensor: Sensor): ImageView {
        val sensorView = ImageView(context)

        val params = SensorsLayout.LayoutParams(sensorSize, sensorSize)
        sensorView.layoutParams = params

        val drawable = ContextCompat.getDrawable(context, sensor.iconID)
        sensorView.setImageDrawable(drawable)
        sensorView.setPadding(sensorPadding, 0, sensorPadding, 0)

        sensorView.alpha = SENSOR_ALPHA

        return sensorView
    }

    companion object {
        private const val SENSOR_SIZE = 27
        private const val SENSOR_PADDING = 4
        private const val SENSOR_ALPHA = 0.6f


        private fun <T : Comparable<T>> asSortedList(c: Collection<T>): List<T> {
            val list = ArrayList(c)
            Collections.sort(list)
            return list
        }

        private fun dpToPx(dp: Int): Int {
            return Math.round(dp * (Resources.getSystem().displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
        }
    }
}
