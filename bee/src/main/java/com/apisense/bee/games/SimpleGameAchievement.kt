package com.apisense.bee.games

import android.app.Activity
import android.support.v4.app.Fragment
import android.util.Log

import com.google.android.gms.games.Games

class SimpleGameAchievement(private val achievementID: String) {

    fun unlock(fromActivity: BeeGameActivity) {
        if (fromActivity.gameHelper.isSignedIn) {
            Log.d(TAG, "Unlocking achievement: " + achievementID)
            Games.Achievements.unlock(fromActivity.apiClient, achievementID)
        }
    }

    fun unlock(fromFragment: Fragment) {
        val hostActivity = fromFragment.activity
        if (hostActivity is BeeGameActivity) {
            unlock(hostActivity)
        }
    }

    companion object {
        private val TAG = "SimpleAchievement"
    }
}
