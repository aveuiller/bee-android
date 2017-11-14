package com.apisense.bee.utils

import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat

import com.apisense.bee.BeeApplication
import io.apisense.sdk.APISENSE
import io.apisense.sdk.core.APSCallback
import io.apisense.sdk.core.store.Crop

class CropPermissionHandler(private val source: Activity, private val crop: Crop, private val callback: APSCallback<Crop>) {
    private val apisenseSdk: APISENSE.Sdk = (source.application as BeeApplication).sdk

    /**
     * Check for needed permissions, and request them if some are denied.
     * (The crop start will then be managed in
     * [CropPermissionHandler.onRequestPermissionsResult])
     *
     * If every permissions are granted, start the crop.
     */
    fun startOrRequestPermissions() {
        val deniedPermissions = apisenseSdk.cropManager.deniedPermissions(crop)
        if (deniedPermissions.isEmpty()) {
            apisenseSdk.cropManager.start(crop, callback)
        } else {
            ActivityCompat.requestPermissions(source,
                    deniedPermissions.toTypedArray<String>(),
                    REQUEST_PERMISSION_START_CROP
            )
        }
    }

    /**
     * Contains the common behavior for a permission request callback about a crop.
     *
     * @param requestCode The request code.
     * @param permissions The asked permissions.
     * @param grantResults The grant result.
     */
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSION_START_CROP) {
            if (permissionsGranted(grantResults)) {
                apisenseSdk.cropManager.start(crop, callback)
            }
        }
    }

    /**
     * Tells if every asked permissions has been granted.
     *
     * @param grantResults The permissions grant status.
     * @return True if every permissions are granted, false otherwise.
     */
    private fun permissionsGranted(grantResults: IntArray): Boolean {
        var everythingGranted = true
        for (grantResult in grantResults) {
            everythingGranted = everythingGranted && grantResult == PackageManager.PERMISSION_GRANTED
        }
        return everythingGranted
    }

    companion object {
        private val REQUEST_PERMISSION_START_CROP = 1
    }
}

