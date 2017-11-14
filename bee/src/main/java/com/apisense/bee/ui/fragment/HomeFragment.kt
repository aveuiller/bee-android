package com.apisense.bee.ui.fragment

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.FragmentTransaction
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.apisense.bee.BeeApplication
import com.apisense.bee.R
import com.apisense.bee.callbacks.BeeAPSCallback
import com.apisense.bee.ui.activity.HomeActivity
import com.apisense.bee.ui.adapter.DividerItemDecoration
import com.apisense.bee.ui.adapter.SubscribedExperimentsRecyclerAdapter
import io.apisense.JSDoc
import io.apisense.sdk.APISENSE
import io.apisense.sdk.core.store.Crop
import kotterknife.bindView
import java.util.*

@JSDoc
class HomeFragment : BaseFragment() {
    private val storeButton: FloatingActionButton by bindView(R.id.store)
    private val mRecyclerView: RecyclerView by bindView(R.id.home_experiment_lists)
    private val mEmptyHome: TextView by bindView(R.id.home_empty_list)

    private val apisenseSdk: APISENSE.Sdk = (activity.application as BeeApplication).sdk

    private lateinit var mStoreListener: OnStoreClickedListener
    private lateinit var mAdapter: SubscribedExperimentsRecyclerAdapter
    private lateinit var autoUpdateRunning: Timer

    interface OnStoreClickedListener {
        fun switchToStore()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val view = inflater.inflate(R.layout.fragment_home, container, false)
        mStoreListener = activity as OnStoreClickedListener

        homeActivity.supportActionBar!!.setTitle(R.string.title_activity_home)
        homeActivity.selectDrawerItem(HomeActivity.DRAWER_HOME_IDENTIFIER)

        mAdapter = SubscribedExperimentsRecyclerAdapter(object : SubscribedExperimentsRecyclerAdapter.OnItemClickListener {
            override fun onItemClick(crop: Crop) {
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

        })
        mRecyclerView.adapter = mAdapter

        mRecyclerView.setHasFixedSize(true) // Performances
        val mLayoutManager = LinearLayoutManager(activity)
        mRecyclerView.layoutManager = mLayoutManager
        mRecyclerView.addItemDecoration(DividerItemDecoration(activity))

        retrieveActiveExperiments()

        apisenseSdk.cropManager.synchroniseSubscriptions(OnCropModifiedOnStartup())

        storeButton.setOnClickListener { doGoToStore(storeButton) }

        return view
    }

    override fun onResume() {
        super.onResume()
        retrieveActiveExperiments()
        autoUpdateRunning = Timer()
        autoUpdateRunning.schedule(object : TimerTask() {
            override fun run() {
                activity.runOnUiThread { retrieveActiveExperiments() }
            }
        }, 0, 3000) // updates each 3 seconds
    }

    override fun onPause() {
        super.onPause()
        autoUpdateRunning.cancel()
    }

    /* onClick */

    private fun doGoToStore(storeButton: View) {
        mStoreListener.switchToStore()
    }

    /* Crop management */

    fun setExperiments(experiments: ArrayList<Crop>) {
        mAdapter.setInstalledCrops(experiments)
        mAdapter.notifyDataSetChanged()
    }

    private fun retrieveActiveExperiments() {
        apisenseSdk.cropManager.getSubscriptions(ExperimentListRetrievedCallback())
    }

    /* Callbacks */

    private inner class ExperimentListRetrievedCallback : BeeAPSCallback<List<Crop>>(activity) {

        override fun onDone(response: List<Crop>) {
            Log.i(TAG, "number of Active Experiments: ${response.size}")
            if (response.isEmpty()) {
                mEmptyHome.visibility = View.VISIBLE
            } else {
                mEmptyHome.visibility = View.GONE
                setExperiments(ArrayList(response))
            }
        }
    }

    private inner class OnCropModifiedOnStartup : BeeAPSCallback<Crop>(activity) {

        override fun onDone(crop: Crop) {
            Log.d(TAG, "Crop ${crop.name} started back")
            retrieveActiveExperiments()
            mAdapter.notifyDataSetChanged()
        }
    }

    companion object {
        private const val TAG = "HomeFragment"
    }
}
