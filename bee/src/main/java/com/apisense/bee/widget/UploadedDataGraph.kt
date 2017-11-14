package com.apisense.bee.widget

import android.content.Context
import android.util.AttributeSet

import com.apisense.bee.R
import com.apisense.bee.utils.RetroCompatibility
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.HashSet
import java.util.Locale

import io.apisense.sdk.core.statistics.UploadedEntry

/**
 * Graph specifically used to show a collection of [UploadedEntry].
 * Currently display the last 7 days of uploaded data.
 */
class UploadedDataGraph : RadarChart {

    private val endOfDay: Date
        get() {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            return calendar.time
        }

    constructor(context: Context) : super(context) {
        configureUploadGraph()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        configureUploadGraph()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        configureUploadGraph()
    }

    /**
     * Set the given values in the graph.
     * Will display the 7 last days worth of upload.
     *
     * @param uploaded Collection of uploaded entries
     */
    fun setValues(uploaded: Collection<UploadedEntry>) {
        val endOfCurrentDay = endOfDay
        var drawEverythingAfter = Date(endOfCurrentDay.time - ONE_DAY_MS * NB_SHOWN_DAYS)
        var drawEverythingBefore = Date(drawEverythingAfter.time + ONE_DAY_MS)
        val entries = ArrayList(uploaded)
        val toRemove = HashSet<UploadedEntry>()
        var nbTracesForDate: Int
        var uploadDate: Date

        val labels = ArrayList<String>()

        val radarEntries = ArrayList<RadarEntry>()

        for (i in 0 until NB_SHOWN_DAYS) {
            nbTracesForDate = 0
            for (entry in entries) {
                uploadDate = entry.uploadDate
                if (uploadDate.before(drawEverythingBefore) && uploadDate.after(drawEverythingAfter)) {
                    nbTracesForDate += entry.numberOfTraces
                    toRemove.add(entry) // Will not fit anywhere else, avoid useless iterations
                } else if (uploadDate.before(drawEverythingAfter)) {
                    toRemove.add(entry) // Too old to be drawn, avoid useless iterations
                }
            }
            entries.removeAll(toRemove)
            toRemove.clear()

            radarEntries.add(RadarEntry(nbTracesForDate.toFloat()))
            labels.add(DATE_FORMAT.format(drawEverythingBefore))

            drawEverythingAfter = drawEverythingBefore
            drawEverythingBefore = Date(drawEverythingBefore.time + ONE_DAY_MS)
        }

        val set = getConfiguredDataSet(radarEntries)

        val sets = ArrayList<IRadarDataSet>()
        sets.add(set)

        val data = getConfiguredRadarData(sets)

        this.data = data

        val xAxis = xAxis
        xAxis.valueFormatter = ValuePrinter(labels.toTypedArray<String>())
    }

    private fun configureUploadGraph() {
        // Actions on Graph (move, scale, ..)
        setOnChartValueSelectedListener(null)

        // Configure visible elements
        val xAxis = xAxis
        xAxis.setDrawGridLines(false)

        legend.form = Legend.LegendForm.CIRCLE

        this.webLineWidth = 1.5f
        this.webLineWidthInner = 0.75f
        this.webAlpha = 100
        this.animateXY(
                1400, 1400,
                Easing.EasingOption.EaseInOutQuad,
                Easing.EasingOption.EaseInOutQuad)
        this.description.isEnabled = false
    }

    private fun getConfiguredDataSet(entries: List<RadarEntry>): RadarDataSet {
        val set = RadarDataSet(entries, resources.getString(R.string.experiment_activity_7_days))

        // Colors
        val mainColor = RetroCompatibility.retrieveColor(resources, R.color.aps_orange)
        set.color = mainColor
        set.fillColor = mainColor

        // Elements to show
        set.lineWidth = 2f
        set.isHighlightEnabled = false
        set.setDrawFilled(true)

        return set
    }

    private fun getConfiguredRadarData(dataSets: List<IRadarDataSet>): RadarData {
        val data = RadarData(dataSets)
        data.setValueTextSize(10f)
        return data
    }

    /**
     * Defines how to display each entry value.
     */
    private inner class ValuePrinter internal constructor(private val labels: Array<String>) : IAxisValueFormatter {

        override fun getFormattedValue(value: Float, axis: AxisBase): String {
            return labels[value.toInt() % labels.size]
        }
    }

    companion object {
        private val DATE_FORMAT = SimpleDateFormat("dd-MM", Locale.US)
        private const val NB_SHOWN_DAYS = 7
        private const val ONE_DAY_MS = 86400000L
    }
}
