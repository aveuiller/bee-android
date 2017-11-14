package com.apisense.bee.ui.fragment

import android.app.Activity
import android.os.Bundle
import android.support.design.widget.Snackbar
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
import com.apisense.bee.games.SimpleGameAchievement
import com.apisense.bee.ui.activity.HomeActivity
import com.apisense.bee.ui.adapter.DividerItemDecoration
import com.apisense.bee.ui.adapter.SensorRecyclerAdapter
import io.apisense.sdk.APISENSE
import io.apisense.sdk.core.preferences.Preferences
import kotterknife.bindView
import java.util.*

class PrivacyFragment : BaseFragment() {
    private val apisenseSdk: APISENSE.Sdk = (activity.application as BeeApplication).sdk

    private val recyclerView: RecyclerView by bindView(R.id.sensors_list)
    private val emptyListView: TextView by bindView(R.id.sensors_list_empty)
    private val applyButton: TextView by bindView(R.id.sensors_list_save)

    private var preferences = Preferences()

    private lateinit var adapter: SensorRecyclerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val root = inflater.inflate(R.layout.fragment_privacy, container, false)

        applyButton.setOnClickListener { savePreferences(applyButton) }

        homeActivity.supportActionBar!!.setTitle(R.string.title_activity_settings)
        homeActivity.selectDrawerItem(HomeActivity.DRAWER_SETTINGS_IDENTIFIER)

        val sensorList = ArrayList(apisenseSdk.preferencesManager.retrieveAvailableSensors())
        Collections.sort(sensorList)
        Log.i(TAG, "Got sensors: $sensorList")
        adapter = SensorRecyclerAdapter(sensorList)

        recyclerView.setHasFixedSize(true) // Performances
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.addItemDecoration(DividerItemDecoration(activity))

        apisenseSdk.preferencesManager.retrievePreferences(OnPreferencesReturned(activity))
        SimpleGameAchievement(getString(R.string.achievement_secretive_bee)).unlock(this)

        return root
    }

    private fun savePreferences(button: View) {
        preferences.privacyPreferences.disabledSensors = adapter.disabledSensors
        if (apisenseSdk.sessionManager.isConnected) {
            // Avoid saving preferences if user used the logout button.
            apisenseSdk.preferencesManager.savePreferences(preferences, OnPreferencesSaved(activity))
        }
    }

    private inner class OnPreferencesReturned internal constructor(activity: Activity) : BeeAPSCallback<Preferences>(activity) {

        override fun onDone(prefs: Preferences) {
            preferences = prefs
            val disabledSting = preferences.privacyPreferences.disabledSensors
            Log.i(TAG, "Got disabledSting sensors: " + disabledSting)
            for (stingName in disabledSting) {
                adapter.setSensorActivation(stingName, false)
            }

            recyclerView.adapter = adapter
            adapter.notifyDataSetChanged()
        }

        override fun onError(e: Exception) {
            super.onError(e)
            recyclerView.visibility = View.GONE
            applyButton.visibility = View.GONE
            emptyListView.visibility = View.VISIBLE
        }
    }

    private inner class OnPreferencesSaved internal constructor(activity: Activity) : BeeAPSCallback<Void>(activity) {

        override fun onDone(aVoid: Void) {
            Snackbar.make(view!!, "Preferences saved, restarting crops", Snackbar.LENGTH_LONG)
                    .show()
        }
    }

    companion object {
        private const val TAG = "PrivacyFragment"
    }
}
