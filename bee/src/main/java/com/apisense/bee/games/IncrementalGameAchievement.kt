package com.apisense.bee.games

import android.app.Activity
import android.support.v4.app.Fragment
import android.util.Log

import com.google.android.gms.games.Games

class IncrementalGameAchievement(private val achievementID: String) {

    fun increment(fromActivity: BeeGameActivity) {
        if (fromActivity.gameHelper.isSignedIn) {
            Log.d(TAG, "Increasing achievement value: " + achievementID)
            Games.Achievements.increment(fromActivity.apiClient, achievementID, 1)
        }
    }

    fun increment(fromFragment: Fragment) {
        val hostActivity = fromFragment.activity
        if (hostActivity is BeeGameActivity) {
            increment(hostActivity)
        }
    }

    companion object {
        private val TAG = "IncrementalAchievement"
    }
}
