package com.apisense.bee.ui.activity

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import com.apisense.bee.BeeApplication
import com.apisense.bee.R
import com.apisense.bee.games.BeeGameActivity
import com.apisense.bee.ui.fragment.*
import com.google.android.gms.common.images.ImageManager
import com.google.android.gms.games.Games
import com.google.android.gms.games.Player
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import io.apisense.sdk.APISENSE
import kotterknife.bindView

class HomeActivity : BeeGameActivity(), HomeFragment.OnStoreClickedListener, SettingsFragment.OnSensorClickedListener {
    private val toolbar: Toolbar by bindView(R.id.toolbar)

    private val headerResult: AccountHeader = generateAccountHeader()
    private val apisenseSdk: APISENSE.Sdk = (application as BeeApplication).sdk
    private var drawerInitializedWithUser: Boolean = false
    private lateinit var drawer: Drawer

    // Drawer items
    private val home = generateDrawerItem(R.string.title_activity_home, R.drawable.ic_home, DRAWER_HOME_IDENTIFIER)
    private val store = generateDrawerItem(R.string.title_activity_store, R.drawable.ic_store_blck, DRAWER_STORE_IDENTIFIER)
    private val play = generateDrawerItem(R.string.title_activity_gpg, R.drawable.ic_gpg, DRAWER_PLAY_IDENTIFIER)
    private val playReward = generateDrawerItem(R.string.title_activity_reward, R.drawable.ic_gpg, DRAWER_PLAY_REWARD_IDENTIFIER)
    private val settings = generateDrawerItem(R.string.title_activity_settings, R.drawable.ic_action_settings, DRAWER_SETTINGS_IDENTIFIER)
    private val profile = generateDrawerItem(R.string.title_activity_account, R.drawable.ic_action_person, DRAWER_ACCOUNT_IDENTIFIER)
    private val about = generateDrawerItem(R.string.title_activity_about, R.drawable.ic_action_about, DRAWER_ABOUT_IDENTIFIER)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        setSupportActionBar(toolbar)
        supportActionBar?.setTitle(R.string.title_activity_home)

        drawerInitializedWithUser = false

        drawer = generateNavigationDrawer(savedInstanceState, headerResult)
        hideHeaderDrawerInformation()
        drawer.actionBarDrawerToggle?.isDrawerIndicatorEnabled = true
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        if (findViewById(R.id.exp_container) != null) {
            if (savedInstanceState != null) {
                return
            }

            if (!apisenseSdk.sessionManager.isConnected) {
                val intent = Intent(this, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            } else {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.exp_container, HomeFragment())
                        .commit()
            }
        }
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen) {
            drawer.closeDrawer()
        } else {
            super.onBackPressed()
        }
    }

    override fun onSignInSucceeded() {
        super.onSignInSucceeded()

        if (!drawerInitializedWithUser) {
            drawerInitializedWithUser = true
            drawer.removeItem(DRAWER_PLAY_IDENTIFIER.toLong())
            drawer.addItemAtPosition(playReward, DRAWER_PLAY_REWARD_IDENTIFIER)
            refreshGPGData()
        }
    }

    override fun onSignInFailed() {
        super.onSignInFailed()
        Log.w(TAG, "Error on GPG signin: " + signInError.toString())
    }

    fun selectDrawerItem(item: Int) {
        drawer.setSelectionAtPosition(item, false)
    }

    // Private methods

    /**
     * Refresh Google Play Games user information
     * in the Drawer
     */
    private fun refreshGPGData() {
        refreshPlayGamesData { player -> setHeaderDrawerInformation(player) }
    }

    /**
     * Hide avatar bubble and text switcher in the drawer
     */
    private fun hideHeaderDrawerInformation() {
        headerResult.view.findViewById<View>(R.id.material_drawer_account_header_text_switcher).visibility = View.GONE
    }

    /**
     * Set Google Play Games user information
     *
     * @param player Player from Google
     */
    private fun setHeaderDrawerInformation(player: Player) {
        ImageManager.create(this@HomeActivity).loadImage({ _, drawable, _ ->
            setHeaderContent(drawable, player)
        }, player.iconImageUri)
    }

    /**
     * Draw header content using Google Play Games data.
     *
     * @param drawable The user icon to draw.
     * @param player   The player info.
     */
    private fun setHeaderContent(drawable: Drawable, player: Player) {
        headerResult.addProfiles(
                ProfileDrawerItem()
                        .withName(resources.getString(R.string.level)
                                + " " + player.levelInfo.currentLevel.levelNumber
                        )
                        .withEmail(player.displayName)
                        .withIcon(drawable)

        )
        hideHeaderDrawerInformation()
    }

    /**
     * Create navigation drawer on every fragment handled by HomeActivity
     */
    private fun generateNavigationDrawer(savedInstanceState: Bundle?, headerResult: AccountHeader): Drawer {
        return DrawerBuilder()
                .withActivity(this)
                .withSavedInstance(savedInstanceState)
                .withToolbar(toolbar)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        home, store,
                        DividerDrawerItem(),
                        play, settings, profile,
                        DividerDrawerItem(),
                        about
                )
                .withTranslucentStatusBar(false)
                .withOnDrawerItemClickListener { _, _, drawerItem ->
                    val identifier = drawerItem.identifier.toInt()
                    when (identifier) {
                    // Based on addDrawerItems order starting from 1..n
                        DRAWER_HOME_IDENTIFIER -> startAndAddFragmentToBackStack(HomeFragment(), false)
                        DRAWER_STORE_IDENTIFIER -> switchToStore()
                        DRAWER_PLAY_IDENTIFIER -> beginUserInitiatedSignIn()
                        DRAWER_PLAY_REWARD_IDENTIFIER -> startActivityForResult(
                                Games.Achievements.getAchievementsIntent(apiClient), 0
                        )
                        DRAWER_SETTINGS_IDENTIFIER -> startAndAddFragmentToBackStack(SettingsFragment(), true)
                        DRAWER_ACCOUNT_IDENTIFIER -> startAndAddFragmentToBackStack(AccountFragment(), true)
                        DRAWER_ABOUT_IDENTIFIER -> startAndAddFragmentToBackStack(AboutFragment(), true)
                        else // Separator cases, nothing to do.
                        -> {
                        }
                    }
                    drawer.closeDrawer()
                    true
                }
                .build()
    }

    /**
     * Generate Header in the drawer
     *
     * @return
     */
    private fun generateAccountHeader(): AccountHeader {
        return AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.drawer_background)
                .build()
    }

    /**
     * Generate PrimaryDrawerItem for the Drawer
     *
     * @param name       Path to resource name
     * @param icon       Path to resource icon
     * @param identifier Static item identifier
     * @return Drawer item
     */
    private fun generateDrawerItem(name: Int, icon: Int, identifier: Int): PrimaryDrawerItem {
        return PrimaryDrawerItem().withName(name)
                .withIcon(icon).withIdentifier(identifier.toLong())
    }

    /**
     * Start a new fragment and add it to the back stack
     *
     * @param instance       Fragment instance to start
     * @param addToBackStack Replace fragment if true, add otherwise
     */
    private fun startAndAddFragmentToBackStack(instance: Fragment, addToBackStack: Boolean) {
        val transaction = supportFragmentManager.beginTransaction()
        if (addToBackStack) {
            supportFragmentManager.popBackStackImmediate()
            transaction.replace(R.id.exp_container, instance)
            transaction.addToBackStack(null)
        } else {
            supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            transaction.replace(R.id.exp_container, instance)
        }
        transaction.commit()
    }

    override fun switchToStore() {
        startAndAddFragmentToBackStack(StoreFragment(), true)
    }

    override fun showSensors() {
        startAndAddFragmentToBackStack(PrivacyFragment(), true)
    }

    companion object {
        private val TAG = "HomeActivity"

        // Drawer item identifiers
        internal val DRAWER_HOME_IDENTIFIER = 1
        internal val DRAWER_STORE_IDENTIFIER = 2
        internal val DRAWER_PLAY_IDENTIFIER = 3
        internal val DRAWER_PLAY_REWARD_IDENTIFIER = 4
        internal val DRAWER_SETTINGS_IDENTIFIER = 5
        internal val DRAWER_ACCOUNT_IDENTIFIER = 6
        internal val DRAWER_ABOUT_IDENTIFIER = 7
    }
}
