package com.apisense.bee.ui.fragment

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.Toast
import com.apisense.bee.BeeApplication
import com.apisense.bee.R
import com.apisense.bee.callbacks.BeeAPSCallback
import com.apisense.bee.ui.activity.HomeActivity
import com.apisense.bee.ui.activity.LoginActivity
import com.apisense.bee.utils.RetroCompatibility
import io.apisense.sdk.APISENSE
import io.apisense.sdk.core.APSCallback
import io.apisense.sdk.core.store.Crop
import io.apisense.sdk.exception.UserNotConnectedException
import kotterknife.bindView

class SettingsFragment : BaseFragment() {
    private val accessibility: Button by bindView(R.id.settings_accessibility_link)
    private val manageSensors: Button by bindView(R.id.settings_manage_sensor_link)
    private var apisenseSdk: APISENSE.Sdk = (activity.application as BeeApplication).sdk

    /**
     * Check if an AccessibilityService with the same package as the application is enabled.
     * This method consider that we own only one accessibility service.
     *
     * @return true if an accessibility service is enabled, false otherwise.
     */
    // Our only AccessibilityService is enabled
    private val isAccessibilityEnabled: Boolean
        get() {
            val am = activity
                    .getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
            val runningServices = am
                    .getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)

            for (service in runningServices) {
                val associatedApp = service.resolveInfo.serviceInfo.applicationInfo.packageName
                if (activity.packageName == associatedApp) {
                    return true
                }
            }
            return false
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val root = inflater.inflate(R.layout.fragment_settings, container, false)

        homeActivity.supportActionBar!!.title = "Settings"
        homeActivity.selectDrawerItem(HomeActivity.DRAWER_ACCOUNT_IDENTIFIER)

        accessibility.setOnClickListener { manageAccessibilityService() }
        manageSensors.setOnClickListener { manageSensors() }

        return root
    }

    override fun onResume() {
        super.onResume()
        redrawAccessibilityButton()
    }


    /**
     * Redraw the accessibility button depending on the state of the accessibility service.
     */
    private fun redrawAccessibilityButton() {
        if (isAccessibilityEnabled) {
            redrawAccessibilityButton(R.string.settings_accessibility_enabled, R.color.aps_green)
        } else {
            redrawAccessibilityButton(R.string.settings_accessibility_disabled, R.color.aps_accent)
        }
    }

    /**
     * Set the given text and color to the accessibility button.
     *
     * @param textRes  The text resource to set (given the service name as format).
     * @param colorRes The color to set.
     */
    private fun redrawAccessibilityButton(textRes: Int, colorRes: Int) {
        accessibility.setBackgroundColor(RetroCompatibility.retrieveColor(resources, colorRes))
        accessibility.text = getString(textRes, getString(R.string.accessibility_service_name))
    }

    private fun manageSensors() {
        homeActivity.showSensors()

    }

    private fun manageAccessibilityService() {
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    interface OnSensorClickedListener {
        fun showSensors()
    }

    companion object {
        private const val TAG = "Bee::SettingsFragment"
    }

}
