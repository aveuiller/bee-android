package com.apisense.bee

import android.os.AsyncTask

import com.facebook.FacebookSdk
import com.rollbar.Rollbar
import com.rollbar.payload.Payload

import io.apisense.sdk.APISENSE
import io.apisense.sdk.APSApplication
import io.apisense.sting.environment.EnvironmentStingModule
import io.apisense.sting.motion.MotionStingModule
import io.apisense.sting.network.NetworkStingModule
import io.apisense.sting.phone.PhoneStingModule
import io.apisense.sting.visualization.VisualizationStingModule

class BeeApplication : APSApplication() {
    private val rollbar: Rollbar by lazy {
        Rollbar(
                BuildConfig.ROLLBAR_KEY,
                BuildConfig.ROLLBAR_ENV
        )
    }

    override fun onCreate() {
        super.onCreate()
        FacebookSdk.sdkInitialize(applicationContext)
    }

    override fun generateAPISENSESdk(): APISENSE.Sdk {
        return APISENSE(this)
                .useSdkKey(com.apisense.bee.BuildConfig.SDK_KEY)
                .bindStingPackage(PhoneStingModule(), NetworkStingModule(),
                        MotionStingModule(), EnvironmentStingModule(),
                        VisualizationStingModule())
                .useScriptExecutionService(true)
                .sdk
    }

    fun reportException(throwable: Throwable) {
        ExceptionReport(rollbar, throwable).execute()
    }

    companion object {
        class ExceptionReport(val rollbar: Rollbar, val throwable: Throwable) : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg params: Void): Void? {
                val rollbarPayload = Payload.fromError(
                        BuildConfig.ROLLBAR_KEY, BuildConfig.ROLLBAR_ENV,
                        throwable, null
                )
                rollbar.sender.send(rollbarPayload)
                return null
            }
        }
    }
}
