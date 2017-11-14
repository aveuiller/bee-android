package com.apisense.bee.utils

import android.content.res.Resources
import android.os.Build

object RetroCompatibility {
    fun retrieveColor(res: Resources, colorId: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            res.getColor(colorId, null)
        } else {
            res.getColor(colorId)
        }
    }
}
