package com.apisense.bee.callbacks

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Toast

import com.apisense.bee.BeeApplication
import com.apisense.bee.R
import com.apisense.bee.ui.activity.LoginActivity
import io.apisense.sdk.core.APSCallback
import io.apisense.sdk.exception.UserNotConnectedException

/**
 * Common implementation of the APSCallback
 * handling generic errors.
 * At the moment, this callback will:
 * - ask the user to log back in if the session is invalid when contacting server.
 *
 * @param <T> Type of the returned object.
</T> */
abstract class BeeAPSCallback<T>(protected var activity: Activity) : APSCallback<T> {

    override fun onError(e: Exception) {
        (activity.application as BeeApplication).reportException(e)
        Log.e("Bee::" + activity.javaClass.getSimpleName(), "Got an error on callback", e)
        // Retrofit encapsulate the APISENSE Exception.
        if (e.cause is UserNotConnectedException) {
            val loginIntent = Intent(activity, LoginActivity::class.java)
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            Toast.makeText(activity, R.string.error_invalid_session, Toast.LENGTH_LONG).show()
            activity.startActivity(loginIntent)
        }
    }
}
