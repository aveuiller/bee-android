package com.apisense.bee.ui.fragment

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.apisense.bee.R
import com.apisense.bee.ui.activity.HomeActivity
import io.apisense.sdk.APISENSE

class AboutFragment : BaseFragment() {

    /**
     * Helper to get the app version info
     *
     * @return a PackageInfo object
     */
    private val appInfo: PackageInfo?
        get() {
            val manager = activity.packageManager
            try {
                return manager.getPackageInfo(activity.packageName, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                Log.w(TAG, "Unable to retrieve package info: ${e.localizedMessage}")
            }

            return null
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val root = inflater.inflate(R.layout.fragment_about, container, false)

        homeActivity.supportActionBar?.setTitle(R.string.title_activity_about)
        homeActivity.selectDrawerItem(HomeActivity.DRAWER_ABOUT_IDENTIFIER)

        val beeVersion = root.findViewById<View>(R.id.about_bee_version) as TextView
        val apisenseVersion = root.findViewById<View>(R.id.about_apisense_version) as TextView
        val copyright = root.findViewById<View>(R.id.about_copyright) as TextView
        Linkify.addLinks(copyright, Linkify.ALL)

        val res = resources
        beeVersion.text = String.format(res.getString(R.string.bee_version), appInfo?.versionName)
        apisenseVersion.text = String.format(res.getString(R.string.apisense_version), APISENSE.VERSION_NAME)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            copyright.text = Html.fromHtml(res.getString(R.string.about_copyright), Html.FROM_HTML_MODE_LEGACY)
        } else {
            copyright.text = Html.fromHtml(res.getString(R.string.about_copyright))
        }
        copyright.movementMethod = LinkMovementMethod.getInstance()
        return root
    }

    companion object {
        private const val TAG = "AboutFragment"
    }
}
