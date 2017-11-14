package com.apisense.bee.callbacks

import android.content.Context
import android.widget.Toast

import com.apisense.bee.R
import io.apisense.sdk.core.APSCallback
import io.apisense.sdk.core.store.Crop

open class OnCropStarted(private val context: Context) : APSCallback<Crop> {

    override fun onDone(crop: Crop) {
        val message = String.format(context.getString(R.string.experiment_started), crop.name)
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onError(e: Exception) {
        Toast.makeText(context, "Error on start (" + e.localizedMessage + ")", Toast.LENGTH_SHORT).show()
    }
}
