package com.apisense.bee.ui.fragment

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.*
import android.widget.TextView
import com.apisense.bee.R
import com.apisense.bee.callbacks.OnCropStarted
import com.apisense.bee.callbacks.OnCropStopped
import com.apisense.bee.callbacks.OnCropUnsubscribed
import com.apisense.bee.utils.CropPermissionHandler
import com.apisense.bee.widget.UploadedDataGraph
import com.apisense.bee.widget.VisualizationPagerAdapter
import com.rd.PageIndicatorView
import io.apisense.sdk.core.statistics.CropLocalStatistics
import io.apisense.sdk.core.store.Crop
import io.apisense.sting.visualization.VisualizationManager
import kotterknife.bindView
import java.util.*

class HomeDetailsFragment : CommonDetailsFragment() {
    companion object {
        private const val TAG = "HomeDetailsFragment"
    }

    private val viewPager: ViewPager by bindView(R.id.viewpager)
    private val pagerIndicator: PageIndicatorView by bindView(R.id.pagerIndicator)

    private var currentPagerPosition = 0
    private lateinit var pagerAdapter: VisualizationPagerAdapter
    private lateinit var mStartButton: MenuItem
    private lateinit var mStopButton: MenuItem

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val view = inflater.inflate(R.layout.fragment_home_details, container, false)
        homeActivity.supportActionBar?.setTitle(R.string.title_activity_experiment_details)

        val visManager = VisualizationManager.getInstance()

        val statisticsGraph = getStatisticsGraph(inflater, apisenseSdk.statisticsManager.getCropUsage(crop))

        val visualizations = ArrayList<View>()
        visualizations.add(statisticsGraph)
        visualizations.addAll(visManager.getCropVisualizations(context, crop))

        pagerAdapter = VisualizationPagerAdapter(visualizations)

        viewPager.adapter = pagerAdapter
        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                if (currentPagerPosition != position) {
                    currentPagerPosition = position
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    //refresh visualization when scroll stops
                    pagerAdapter.invalidateView(currentPagerPosition)
                }
            }
        })

        viewPager.post {
            val width = viewPager.width

            val indicatorWidth = (width / visualizations.size).toFloat()
            val indicatorRadius = indicatorWidth * 6 / 10 / 2
            val indicatorPadding = indicatorWidth * 4 / 10

            val defaultRadius = resources.getDimension(R.dimen.viewpager_indicator_radius).toInt()

            if (defaultRadius > indicatorRadius) {
                pagerIndicator.setRadius(indicatorRadius)
                pagerIndicator.setPadding(indicatorPadding)
            }
        }

        return view
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.detail_action_start -> doStartStop()
            R.id.detail_action_stop -> doStartStop()
            R.id.detail_action_unsubscribe -> doSubscribeUnsubscribe()
            R.id.detail_action_update -> doStopUpdate()
            else -> Log.w(TAG, "Unknown item")
        }
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_experiment_details, menu)
        super.onCreateOptionsMenu(menu, inflater)

        mStartButton = menu.getItem(0)
        mStopButton = menu.getItem(1)

        if (apisenseSdk.cropManager.isRunning(crop)) {
            displayStopButton()
        } else {
            displayStartButton()
        }
    }

    // Buttons Handlers
    private fun doStartStop() {
        if (apisenseSdk.cropManager.isRunning(crop)) {
            apisenseSdk.cropManager.stop(crop, object : OnCropStopped(activity) {
                override fun onDone(crop: Crop) {
                    super.onDone(crop)
                    displayStartButton()
                }
            })
        } else {
            cropPermissionHandler.startOrRequestPermissions()
        }
    }

    private fun doStopUpdate() {
        if (apisenseSdk.cropManager.isRunning(crop)) {
            apisenseSdk.cropManager.stop(crop, object : OnCropStopped(activity) {
                override fun onDone(crop: Crop) {
                    super.onDone(crop)
                    displayStartButton()
                }
            })
        }

        doUpdate()
    }

    private fun displayStopButton() {
        mStartButton.isVisible = false
        mStopButton.isVisible = true
    }

    private fun displayStartButton() {
        mStartButton.isVisible = true
        mStopButton.isVisible = false
    }


    override fun prepareCropPermissionHandler(): CropPermissionHandler {
        return CropPermissionHandler(activity, crop, object : OnCropStarted(activity) {
            override fun onDone(crop: Crop) {
                super.onDone(crop)
                displayStopButton()
            }
        })
    }

    // Actions

    private fun doSubscribeUnsubscribe() {
        apisenseSdk.cropManager.unsubscribe(crop, object : OnCropUnsubscribed(activity, crop.name) {
            override fun onDone(crop: Crop) {
                super.onDone(crop)
                fragmentManager.popBackStack()
            }
        })
    }

    private fun getStatisticsGraph(inflater: LayoutInflater, cropUsage: CropLocalStatistics): View {
        val statisticGraph = inflater.inflate(R.layout.stats_uploaded_data, viewPager, false)
        val stats = UploadedData(statisticGraph)

        view?.bindView<TextView>(R.id.local_traces);

        stats.localTraces.text = getString(R.string.crop_stats_local_traces, cropUsage.toUpload)
        stats.totalTraces.text = getString(R.string.crop_stats_total_uploaded, cropUsage.totalUploaded)

        val uploaded = cropUsage.uploaded
        if (uploaded.isEmpty()) {
            stats.chart.visibility = View.GONE
            stats.noUpload.visibility = View.VISIBLE
        } else {
            stats.chart.setValues(cropUsage.uploaded)
        }

        return statisticGraph
    }

    class UploadedData(val view: View) {
        internal val localTraces: TextView
            get() = view.findViewById(R.id.local_traces)
        internal val totalTraces: TextView
            get() = view.findViewById(R.id.total_uploaded)
        internal val chart: UploadedDataGraph
            get() = view.findViewById(R.id.uploaded_chart)
        internal val noUpload: TextView
            get() = view.findViewById(R.id.no_upload)
    }
}
