package com.apisense.bee.callbacks

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.apisense.bee.BeeApplication
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginResult
import io.apisense.sdk.APISENSE
import io.apisense.sdk.core.APSCallback
import io.apisense.sdk.core.bee.LoginProvider
import org.json.JSONException

class FacebookLoginCallback(activity: Activity, private val onLoggedIn: APSCallback<Void>) : FacebookCallback<LoginResult> {
    private val beeApp: BeeApplication = activity.application as BeeApplication
    private val apisenseSdk: APISENSE.Sdk = beeApp.sdk

    override fun onSuccess(loginResult: LoginResult) {
        val accessToken = loginResult.accessToken
        val request = GraphRequest.newMeRequest(accessToken) { jsObject, _ ->
            try {
                val email = jsObject.getString(EMAIL_FIELD)
                apisenseSdk.sessionManager.login(email, accessToken.token,
                        LoginProvider.FACEBOOK, onLoggedIn)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        val parameters = Bundle()
        parameters.putString("fields", EMAIL_FIELD)
        request.parameters = parameters
        request.executeAsync()

    }

    override fun onCancel() {
        Log.e(TAG, "Facebook login canceled")
    }

    override fun onError(exception: FacebookException) {
        beeApp.reportException(exception)
        Log.e(TAG, "Error while connecting to facebook", exception)
    }

    companion object {
        private val TAG = "FacebookCallback"
        private val EMAIL_FIELD = "email"
    }
}
