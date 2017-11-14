package com.apisense.bee.ui.fragment

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import com.apisense.bee.BeeApplication
import com.apisense.bee.R
import com.apisense.bee.callbacks.BeeAPSCallback
import io.apisense.sdk.APISENSE
import io.apisense.sdk.core.bee.Bee

import kotterknife.bindView

class RegisterFragment : Fragment() {
    private val signUpButton: Button by bindView(R.id.registerSignUpBtn)
    private val signInButton: Button by bindView(R.id.registerSignInBtn)
    private val pseudoEditText: EditText by bindView(R.id.registerEmailEditText)
    private val passwordEditText: EditText by bindView(R.id.registerPasswordEditText)
    private val passwordConfirmEditText: EditText by bindView(R.id.registerPasswordRepeatEditText)

    private val mLoginCallback: OnLoginClickedListener = activity as OnLoginClickedListener
    private val apisenseSdk: APISENSE.Sdk = (activity.application as BeeApplication).sdk

    interface OnLoginClickedListener {
        fun switchToLogin()

        fun switchToLogin(email: String, password: String)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        signUpButton.setOnClickListener { attemptRegister(signUpButton) }
        signInButton.setOnClickListener { signInClicked(signInButton) }

        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    private fun signInClicked(view: Button) {
        mLoginCallback.switchToLogin()
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     *
     *
     * If there are form errors (invalid email, missing fields, etc.),
     * The errors are presented and no actual login attempt is made.
     *
     * @param registerButton The button used to call this method.
     */
    private fun attemptRegister(registerButton: View) {
        resetFieldsError()
        val pseudo = pseudoEditText.text.toString()
        val password = passwordEditText.text.toString()
        val passwordRepeat = passwordConfirmEditText.text.toString()

        val focusView = findIncorrectField(password, passwordRepeat)
        focusView?.requestFocus() ?: createAccount(pseudo, password)
    }

    /**
     * Tells whether a field from the creation form is incorrect.
     *
     * @param password       The password to validate.
     * @param passwordRepeat The password confirmation.
     * @return The view containing a validation error if any, null if no error found.
     */
    private fun findIncorrectField(password: String, passwordRepeat: String): View? {
        // Check for a valid password.
        if (TextUtils.isEmpty(password)) {
            passwordEditText.error = getString(R.string.error_field_required)
            return passwordEditText
        } else if (password.length < 7) {
            passwordEditText.error = getString(R.string.signin_error_invalid_password)
            return passwordEditText
        } else if (passwordRepeat != password) {
            passwordConfirmEditText.error = getString(R.string.register_error_invalid_repeat_password)
            return passwordConfirmEditText
        }
        return null
    }

    /**
     * Actually creates the account, tells the user to check for validation email,
     * and redirects the application to the already filled login form.
     *
     * @param pseudo   The pseudo to use on account creation.
     * @param password The password to use on account creation.
     */
    private fun createAccount(pseudo: String, password: String) {
        apisenseSdk.sessionManager
                .createBee(pseudo, password, object : BeeAPSCallback<Bee>(activity) {
                    override fun onDone(bee: Bee) {
                        Toast.makeText(getActivity(),
                                getString(R.string.validation_mail_incoming), Toast.LENGTH_LONG).show()
                        mLoginCallback.switchToLogin(pseudo, password)
                    }

                    override fun onError(e: Exception) {
                        super.onError(e)
                        Snackbar.make(pseudoEditText, e.localizedMessage, Snackbar.LENGTH_LONG).show()
                    }
                })
    }

    /**
     * Remove any validation error shown on the form.
     */
    private fun resetFieldsError() {
        pseudoEditText.error = null
        passwordEditText.error = null
        passwordConfirmEditText.error = null
    }

    companion object {
        private const val TAG = "RegisterFragment"
    }
}


