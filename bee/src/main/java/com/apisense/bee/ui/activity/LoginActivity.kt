package com.apisense.bee.ui.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.apisense.bee.R
import com.apisense.bee.ui.fragment.LoginFragment
import com.apisense.bee.ui.fragment.RegisterFragment

class LoginActivity : AppCompatActivity(), LoginFragment.OnRegisterClickedListener, RegisterFragment.OnLoginClickedListener {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return
            }

            switchToLogin()
        }
    }

    override fun switchToRegister() {
        val registerFragment = RegisterFragment()
        registerFragment.arguments = intent.extras
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, registerFragment).commit()
    }

    override fun switchToLogin(email: String, password: String) {
        val loginFragment = LoginFragment()
        intent.putExtra(LoginFragment.LOGIN_EMAIL_KW, email)
        intent.putExtra(LoginFragment.LOGIN_PASSWORD_KW, password)
        loginFragment.arguments = intent.extras
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, loginFragment).commit()
    }

    override fun switchToLogin() {
        switchToLogin("", "")
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data) // Call fragment activity result
    }
}
