package com.apisense.bee.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.apisense.bee.BeeApplication
import com.apisense.bee.R
import com.apisense.bee.callbacks.BeeAPSCallback
import com.apisense.bee.games.SimpleGameAchievement
import com.apisense.bee.ui.activity.HomeActivity
import com.apisense.bee.ui.activity.LoginActivity
import com.facebook.login.LoginManager
import io.apisense.sdk.APISENSE
import io.apisense.sdk.core.APSCallback
import io.apisense.sdk.core.store.Crop
import io.apisense.sdk.exception.UserNotConnectedException
import kotterknife.bindView

class AccountFragment : BaseFragment() {
    private val logoutButton: Button by bindView(R.id.account_logout)
    private val shareButton: Button by bindView(R.id.account_share)

    private val apisenseSdk: APISENSE.Sdk = (activity.application as BeeApplication).sdk

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val root = inflater.inflate(R.layout.fragment_account, container, false)

        homeActivity.supportActionBar?.setTitle(R.string.title_activity_account)
        homeActivity.selectDrawerItem(HomeActivity.DRAWER_ACCOUNT_IDENTIFIER)

        shareButton.setOnClickListener { doApplicationShare() }
        logoutButton.setOnClickListener { doDisconnect() }

        return root
    }

    private fun doApplicationShare() {
        SimpleGameAchievement(getString(R.string.achievement_recruiting_bee)).unlock(this)
        val resources = resources
        val sendIntent = Intent(Intent.ACTION_SEND)
                .setAction(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_TEXT, resources.getString(R.string.share_bee_text))
        sendIntent.type = "text/plain"
        startActivity(Intent.createChooser(sendIntent, resources.getText(R.string.action_share)))
    }

    private fun doDisconnect() {
        LoginManager.getInstance().logOut()
        apisenseSdk.sessionManager.logout(SignedOutCallback())
    }

    inner class SignedOutCallback : APSCallback<Void> {
        override fun onDone(aVoid: Void) {
            Toast.makeText(activity, R.string.status_changed_to_anonymous, Toast.LENGTH_SHORT).show()
            apisenseSdk.cropManager.stopAll(object : BeeAPSCallback<Crop>(activity) {
                override fun onDone(crop: Crop) {
                    Log.i(TAG, "Crop " + crop.location + " successfully stopped")
                }
            })
            openSlideShow()
        }

        override fun onError(e: Exception) {
            if (e is UserNotConnectedException) {
                openSlideShow()
            }
        }

        private fun openSlideShow() {
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
            activity.finish()
        }
    }

    companion object {
        private val TAG = "Bee::AccountFragment"
    }

}
