package com.apisense.bee.games

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import com.apisense.bee.R
import com.apisense.bee.games.utils.BaseGameActivity
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.games.Games
import com.google.android.gms.games.Player
import com.google.android.gms.games.achievement.Achievement
import java.util.*

/**
 * This class is used to encapsulate the default Play Games activity.
 * Also handle generic data retrieval about player and game statistics.
 */
abstract class BeeGameActivity : BaseGameActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Disable auto-login
        gameHelper.setMaxAutoSignInAttempts(0)
    }

    override fun onSignInSucceeded() {
        SimpleGameAchievement(getString(R.string.achievement_new_bee)).unlock(this)
    }

    override fun onSignInFailed() {
        // Nothing
    }

    protected fun refreshPlayGamesData(onPlayerFetched: (Player) -> Unit) {
        if (isSignedIn) {
            RetrievePlayerData(apiClient, onPlayerFetched).execute()
        } else {
            Log.w(TAG, "User not connected to GPG yet, skip data refresh")
        }
    }

    protected fun refreshAchievements(onAchievementsFetched: (List<Achievement>) -> Unit) {
        if (isSignedIn) {
            RetrieveAchievements(apiClient, onAchievementsFetched).execute()
        } else {
            Log.w(TAG, "User not connected to GPG yet, skip achievements refresh")
        }
    }

    companion object {
        private val TAG = "BeeGameActivity"

        /**
         * This method returns the count of finished achievements on the game
         */
        protected fun countUnlocked(currentAchievements: List<Achievement>): Int {
            var count = 0
            for (achievement in currentAchievements) {
                if (achievement.state == Achievement.STATE_UNLOCKED) {
                    count++
                }
            }
            return count
        }

        private class RetrievePlayerData(val apiClient: GoogleApiClient, private val onPlayerFetched: (Player) -> Unit) : AsyncTask<Void, Void, Player>() {

            override fun doInBackground(vararg params: Void): Player {
                return Games.Players.getCurrentPlayer(apiClient)
            }

            override fun onPostExecute(player: Player) {
                onPlayerFetched(player)
            }
        }

        private class RetrieveAchievements(val apiClient: GoogleApiClient, private val onAchievementsFetched: (List<Achievement>) -> Unit) : AsyncTask<Void, Void, Void>() {

            override fun doInBackground(vararg params: Void): Void? {

                val loadAchievementsResult = Games.Achievements.load(apiClient, false) // true will disable cache usage.
                loadAchievementsResult.setResultCallback { loadedAchievementsResult ->
                    val currentAchievements = ArrayList<Achievement>()

                    val achievementBuffer = loadedAchievementsResult.achievements
                    for (achievement in achievementBuffer) {
                        Log.v(TAG, "Achievement=" + achievement.name + "&status=" + achievement.state)
                        currentAchievements.add(achievement)
                    }

                    onAchievementsFetched(currentAchievements)

                    // Close buffers, achievement no more accessible
                    achievementBuffer.release()
                    loadedAchievementsResult.release()
                }
                return null
            }
        }
    }

}
