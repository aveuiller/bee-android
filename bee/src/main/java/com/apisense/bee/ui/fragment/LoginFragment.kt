package com.apisense.bee.ui.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.apisense.bee.BeeApplication
import com.apisense.bee.R
import com.apisense.bee.callbacks.BeeAPSCallback
import com.apisense.bee.callbacks.FacebookLoginCallback
import com.apisense.bee.ui.activity.HomeActivity
import com.facebook.CallbackManager
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.GoogleApiClient
import io.apisense.sdk.APISENSE
import io.apisense.sdk.core.APSCallback
import io.apisense.sdk.core.bee.LoginProvider
import kotterknife.bindView

class LoginFragment : Fragment() {
    private val signInBtn: Button by bindView(R.id.signInLoginBtn)
    private val registerBtn: Button by bindView(R.id.signInRegisterBtn)
    private val forgotPasswordBtn: Button by bindView(R.id.forgot_password_button)

    private val fbButton: LoginButton by bindView(R.id.fb_login_button)
    private val googleButton: SignInButton by bindView(R.id.google_login_button)

    private val pseudoEditText: EditText by bindView(R.id.signInPseudo)
    private val passwordEditText: EditText by bindView(R.id.signInPassword)


    private val apisenseSdk: APISENSE.Sdk = (activity.application as BeeApplication).sdk
    private val registerCallback: OnRegisterClickedListener = activity as OnRegisterClickedListener
    private val facebookCallbackManager: CallbackManager = CallbackManager.Factory.create()

    private var googleApiClient: GoogleApiClient? = null

    interface OnRegisterClickedListener {
        fun switchToRegister()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        prepareFacebookLogin(fbButton)
        googleButton.setSize(SignInButton.SIZE_WIDE)
        googleButton.setOnClickListener { signInWithGoogle() }

        signInBtn.setOnClickListener { doLogin(signInBtn) }
        registerBtn.setOnClickListener { onRegisterBtnClicked(registerBtn) }
        forgotPasswordBtn.setOnClickListener { requestPasswordReset(forgotPasswordBtn) }

        passwordEditText.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                signInBtn.performClick()
                return@OnEditorActionListener true
            }
            false
        })

        return view
    }

    /**
     * Call google API to log user in
     */
    private fun signInWithGoogle() {
        googleApiClient?.disconnect()

        val gAppId = getString(R.string.google_web_client_id)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(gAppId)
                .build()
        googleApiClient = GoogleApiClient.Builder(context)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN_REQUEST_CODE)
    }

    /**
     * Call facebook API to sign user in
     *
     * @param loginButton The facebook button.
     */
    private fun prepareFacebookLogin(loginButton: LoginButton) {
        loginButton.setReadPermissions("email")
        loginButton.fragment = this
        loginButton.registerCallback(facebookCallbackManager,
                FacebookLoginCallback(activity, OnLoggedIn(activity, loginButton))
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LoginFragment.GOOGLE_SIGN_IN_REQUEST_CODE) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)

            if (result.isSuccess) {
                val account = result.signInAccount
                if (account != null) {
                    apisenseSdk.sessionManager.login(account.email, account.idToken,
                            LoginProvider.GOOGLE, OnLoggedIn(activity, signInBtn)
                    )
                } else {
                    Log.e(TAG, "No account retrieved")
                    Snackbar.make(signInBtn,
                            getString(R.string.failed_to_connect), Snackbar.LENGTH_SHORT).show()
                }
            } else {
                Log.e(TAG, "That's an error: " + result.status)
                Snackbar.make(signInBtn,
                        getString(R.string.failed_to_connect), Snackbar.LENGTH_SHORT).show()
            }
        } else {
            facebookCallbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val extras = arguments
        if (extras != null) {
            val email = extras.getString(LOGIN_EMAIL_KW)
            if (email != null) {
                pseudoEditText.setText(email)
            }

            val password = extras.getString(LOGIN_PASSWORD_KW)
            if (password != null) {
                passwordEditText.setText(password)
            }
        }
    }

    private fun askForEmail() {
        pseudoEditText.error = getString(R.string.missing_email_address)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    internal fun onLoginBtnClicked(view: Button) {
        doLogin(view)
    }

    internal fun onRegisterBtnClicked(view: Button) {
        registerCallback.switchToRegister()
    }

    internal fun requestPasswordReset(view: View) {
        val email = pseudoEditText.text.toString()
        if (email.isEmpty()) {
            askForEmail()
        } else {
            apisenseSdk.sessionManager.resetPassword(email,
                    OnPasswordResetRequested(activity, view))
        }
    }

    // Private methods

    /**
     * Check if sign in form is correctly filled
     *
     * @return true or false
     */
    private fun isInputCorrect(email: String, password: String): Boolean {
        return !(TextUtils.isEmpty(email) || TextUtils.isEmpty(password))
    }

    /**
     * Run sign in task in background
     *
     * @param loginButton button pressed to start task
     */
    private fun doLogin(loginButton: Button) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS)

        val email = pseudoEditText.text.toString()
        val password = passwordEditText.text.toString()
        if (!isInputCorrect(email, password)) {
            Snackbar.make(loginButton, resources.getString(R.string.empty_field), Snackbar.LENGTH_SHORT).show()
            return
        }

        if (apisenseSdk.sessionManager.isConnected) {
            apisenseSdk.sessionManager.logout(object : APSCallback<Void> {
                override fun onDone(response: Void) {
                    loginButton.text = resources.getString(R.string.login)
                    Snackbar.make(loginButton,
                            resources.getString(R.string.status_changed_to_anonymous),
                            Snackbar.LENGTH_SHORT).show()
                }

                override fun onError(e: Exception) {
                    Snackbar.make(loginButton,
                            resources.getString(R.string.experiment_exception_on_closure),
                            Snackbar.LENGTH_SHORT).show()
                }
            })
        } else {
            apisenseSdk.sessionManager.login(
                    pseudoEditText.text.toString(), passwordEditText.text.toString(),
                    OnLoggedIn(activity, loginButton)
            )
        }
    }

    private class OnLoggedIn(private val activity: Activity, private val loginButton: Button) : APSCallback<Void> {

        override fun onDone(response: Void) {
            loginButton.text = activity.getString(R.string.logout)
            val intent = Intent(activity, HomeActivity::class.java)
            activity.startActivity(intent)
            activity.finish()
        }

        override fun onError(e: Exception) {
            Snackbar.make(loginButton, activity.getString(R.string.failed_to_connect),
                    Snackbar.LENGTH_SHORT).show()
        }
    }

    /**
     * Behavior when the password reset request has ended.
     */
    private class OnPasswordResetRequested internal constructor(activity: Activity, private val source: View) : BeeAPSCallback<Void>(activity) {

        override fun onDone(aVoid: Void) {
            Snackbar.make(source, activity.getString(R.string.mail_incoming), Snackbar.LENGTH_LONG)
                    .show()
        }

        override fun onError(e: Exception) {
            Snackbar.make(source, e.localizedMessage, Snackbar.LENGTH_LONG).show()
        }
    }

    companion object {

        val LOGIN_EMAIL_KW = "login_email"
        val LOGIN_PASSWORD_KW = "login_psswd"

        private val TAG = "SignIn Fragment"
        private val GOOGLE_SIGN_IN_REQUEST_CODE = 9001
    }
}
