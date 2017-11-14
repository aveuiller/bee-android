package com.apisense.bee.callbacks


import android.app.Activity
import android.widget.Toast

import com.apisense.bee.R
import io.apisense.sdk.core.store.Crop

open class OnCropUnsubscribed(activity: Activity, private val cropName: String) : BeeAPSCallback<Crop>(activity) {

    override fun onDone(crop: Crop) {
        val toastMessage = String.format(activity.getString(R.string.experiment_unsubscribed), cropName)
        Toast.makeText(activity, toastMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onError(e: Exception) {
        super.onError(e)
        val toastMessage = String.format("Error while unsubscribing from %s", cropName)
        Toast.makeText(activity, toastMessage, Toast.LENGTH_SHORT).show()
    }
}
