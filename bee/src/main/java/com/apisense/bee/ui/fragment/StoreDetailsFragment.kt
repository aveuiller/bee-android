package com.apisense.bee.ui.fragment

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.view.*
import android.widget.TextView
import com.apisense.bee.R
import com.apisense.bee.callbacks.BeeAPSCallback
import com.apisense.bee.callbacks.OnCropSubscribed
import com.apisense.bee.callbacks.OnCropUnsubscribed
import com.apisense.bee.games.IncrementalGameAchievement
import io.apisense.sdk.core.store.Crop
import io.apisense.sdk.core.store.CropGlobalStatistics
import kotterknife.bindView

class StoreDetailsFragment : CommonDetailsFragment() {

    private val subButton: FloatingActionButton by bindView(R.id.experimentSubBtn)
    private val cropDetailsButton: FloatingActionButton by bindView(R.id.crop_details)
    private val subscribersStats: TextView by bindView(R.id.detail_stats_subscribers)

    private lateinit var updateButton: MenuItem

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val view = inflater.inflate(R.layout.fragment_store_details, container, false)
        homeActivity.supportActionBar!!.setTitle(R.string.title_activity_store_experiment_details)

        subButton.setOnClickListener { doSubscribeUnsubscribe() }
        cropDetailsButton.setOnClickListener { onCLickOnCropDetails() }
        updateSubscriptionMenu()

        return view
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.detail_action_update -> doUpdate()
        }
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.store_experiment_details, menu)
        super.onCreateOptionsMenu(menu, inflater)
        updateButton = menu!!.findItem(R.id.detail_action_update)
        setUpdateButtonVisible(apisenseSdk.cropManager.isInstalled(crop))
    }

    // OnClick

    private fun doSubscribeUnsubscribe() {
        if (apisenseSdk.cropManager.isInstalled(crop)) {
            apisenseSdk.cropManager.unsubscribe(crop, StoreDetailsCropUnsubscribed())
        } else {
            apisenseSdk.cropManager.subscribe(crop, StoreDetailsCropSubscribed(this))
        }
    }

    private fun onCLickOnCropDetails() {
        if (apisenseSdk.cropManager.isInstalled(crop)) {
            val extra = Bundle()
            extra.putParcelable("crop", crop)

            val homeDetailsFragment = HomeDetailsFragment()
            homeDetailsFragment.arguments = extra
            activity.supportFragmentManager.beginTransaction()
                    .replace(R.id.exp_container, homeDetailsFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack(null)
                    .commit()
        }
    }

    // Usage

    private fun setUpdateButtonVisible(value: Boolean) {
        updateButton.isVisible = value
    }

    override fun displayExperimentInformation() {
        super.displayExperimentInformation()
        apisenseSdk.statisticsManager.findGlobalStatistics(crop.slug, object : BeeAPSCallback<CropGlobalStatistics>(activity) {
            override fun onDone(stats: CropGlobalStatistics) {
                subscribersStats.text = getString(R.string.crop_stats_subscribers, stats.numberOfSubscribers)
            }
        })
    }

    private inner class StoreDetailsCropUnsubscribed : OnCropUnsubscribed(activity, crop.name) {

        override fun onDone(crop: Crop) {
            super.onDone(crop)
            updateSubscriptionMenu()
            setUpdateButtonVisible(false)
        }
    }

    private fun updateSubscriptionMenu() {
        if (apisenseSdk.cropManager.isInstalled(crop)) {
            subButton.setImageResource(R.drawable.ic_trash)
        } else {
            subButton.setImageResource(R.drawable.ic_action_new)
        }
    }

    private inner class StoreDetailsCropSubscribed(private val mFragment: Fragment)
        : OnCropSubscribed(activity, crop, cropPermissionHandler) {

        override fun onDone(crop: Crop) {
            super.onDone(crop)
            updateSubscriptionMenu()
            setUpdateButtonVisible(true)
            // Increment every subscription related achievements
            IncrementalGameAchievement(getString(R.string.achievement_bronze_wings)).increment(mFragment)
            IncrementalGameAchievement(getString(R.string.achievement_silver_wings)).increment(mFragment)
            IncrementalGameAchievement(getString(R.string.achievement_gold_wings)).increment(mFragment)
            IncrementalGameAchievement(getString(R.string.achievement_crystal_wings)).increment(mFragment)
        }
    }
}
