package com.example.ajiekc.tochka.ui.auth

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.example.ajiekc.tochka.Injection
import com.example.ajiekc.tochka.LceState
import com.example.ajiekc.tochka.R
import com.example.ajiekc.tochka.data.AuthData
import com.example.ajiekc.tochka.ui.navigation.NavigationActivity
import com.example.ajiekc.tochka.extensions.PreferenceHelper
import com.example.ajiekc.tochka.extensions.PreferenceHelper.get
import com.example.ajiekc.tochka.extensions.PreferenceHelper.set
import com.example.ajiekc.tochka.extensions.hide
import com.example.ajiekc.tochka.extensions.show
import com.example.ajiekc.tochka.extensions.toast
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKCallback
import com.vk.sdk.VKSdk
import com.vk.sdk.api.VKError
import kotlinx.android.synthetic.main.activity_auth.*


class AuthActivity : AppCompatActivity() {

    companion object {
        fun start(activity: Activity) {
            val intent = Intent(activity, AuthActivity::class.java)
            activity.startActivity(intent)
        }

        const val AUTH_TYPE_PREF = "auth_type_pref"
        const val USERNAME_PREF = "user_name_pref"
        const val PHOTO_URL_PREF = "photo_url_pref"
        const val RC_SIGN_IN = 9001
        val TAG: String = AuthActivity::class.java.simpleName
    }

    private lateinit var mFBCallbackManager: CallbackManager
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var mViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        skipAuthIfAlreadyLoggedIn()
        val viewModelFactory = Injection.provideAuthViewModelFactory()
        mViewModel = ViewModelProviders.of(this, viewModelFactory).get(AuthViewModel::class.java)
        registerSdkCallbacks()
        mViewModel.viewState.observe(this, Observer {
            renderState(it)
        })
    }

    private fun skipAuthIfAlreadyLoggedIn() {
        val preferences = PreferenceHelper.defaultPrefs(this)
        if (preferences[AUTH_TYPE_PREF, ""]?.isNotEmpty() == true) {
            startNavigationActivity()
        }
    }

    private fun renderState(authState: AuthViewState?) {
        when (authState?.state) {
            LceState.LOADING ->  {
                progress.show()
                vk_button.hide()
                fb_button.hide()
                google_button.hide()
            }
            LceState.CONTENT -> {
                progress.hide()
                onDataReceive(authState.data)
            }
            LceState.ERROR -> {
                toast(getString(R.string.auth_error))
                progress.hide()
                vk_button.show()
                fb_button.show()
                google_button.show()
            }
        }
    }

    private fun onDataReceive(data: AuthData?) {
        val preferences = PreferenceHelper.defaultPrefs(this)
        preferences[USERNAME_PREF] = "${data?.firstName} ${data?.lastName}"
        preferences[PHOTO_URL_PREF] =  data?.photoUrl
        startNavigationActivity()
    }

    private fun startNavigationActivity() {
        NavigationActivity.start(this)
        finish()
    }

    private fun registerSdkCallbacks() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        mFBCallbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(mFBCallbackManager, FBAuthCallback())
    }

    fun onLoginButtonClick(view: View) {
        when (view.id) {
            R.id.vk_button -> VKSdk.login(this)
            R.id.fb_button -> LoginManager.getInstance()
                    .logInWithReadPermissions(this, arrayListOf("public_profile"))
            R.id.google_button -> signIn()
        }
    }

    private fun signIn() {
        startActivityForResult(mGoogleSignInClient.signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (VKSdk.onActivityResult(requestCode, resultCode, data, VKAuthCallback())) {
            return
        }
        if (mFBCallbackManager.onActivityResult(requestCode, resultCode, data)) {
            return
        }
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        Log.d(TAG, "isSuccessful: ${task.isSuccessful}")
        try {
            val account = task.getResult(ApiException::class.java)
            mViewModel.getUserData(AuthType.GOOGLE, account = account)
            setAuthSuccess(AuthType.GOOGLE)
            Log.d(TAG, "GOOGLE: ${account.displayName}")
        } catch (e: ApiException) {
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
        }
    }

    inner class VKAuthCallback : VKCallback<VKAccessToken> {

        override fun onResult(res: VKAccessToken) {
            Log.d(TAG, "onResult VK: ${res.accessToken}")
            mViewModel.getUserData(AuthType.VK, res.accessToken, res.userId)
            setAuthSuccess(AuthType.VK)
        }

        override fun onError(error: VKError?) {
            Log.e(TAG, "onError VK:")
        }
    }

    inner class FBAuthCallback : FacebookCallback<LoginResult> {
        override fun onSuccess(loginResult: LoginResult) {
            Log.d(TAG, "onResult FB: ${loginResult.accessToken.token}")
            mViewModel.getUserData(AuthType.FB, loginResult.accessToken.token)
            setAuthSuccess(AuthType.FB)
        }

        override fun onCancel() {
            Log.d(TAG, "onCancel FB")
        }

        override fun onError(exception: FacebookException) {
            Log.e(TAG, "onError FB")
        }
    }

    fun setAuthSuccess(type: AuthType) {
        val preferences = PreferenceHelper.defaultPrefs(this)
        preferences[AUTH_TYPE_PREF] = type.name
    }
}
