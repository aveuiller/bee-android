package com.apisense.bee.callbacks

import android.app.Activity
import android.widget.Toast

import com.apisense.bee.R
import com.apisense.bee.utils.CropPermissionHandler
import io.apisense.sdk.core.store.Crop


open class OnCropSubscribed(activity: Activity, private val crop: Crop, private val permissionHandler: CropPermissionHandler) : BeeAPSCallback<Crop>(activity) {

    override fun onDone(crop: Crop) {
        val toastMessage = String.format(activity.getString(R.string.experiment_subscribed), crop.name)
        Toast.makeText(activity, toastMessage, Toast.LENGTH_SHORT).show()
        permissionHandler.startOrRequestPermissions()
    }

    override fun onError(e: Exception) {
        super.onError(e)
        val toastMessage = String.format("Error while subscribing to %s", crop.name)
        Toast.makeText(activity, toastMessage, Toast.LENGTH_SHORT).show()
    }
}
