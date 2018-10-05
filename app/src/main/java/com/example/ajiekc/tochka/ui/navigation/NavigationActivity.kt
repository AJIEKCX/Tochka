package com.example.ajiekc.tochka.ui.navigation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import com.example.ajiekc.tochka.R
import com.example.ajiekc.tochka.ui.auth.AuthActivity
import com.example.ajiekc.tochka.ui.auth.AuthType
import com.example.ajiekc.tochka.ui.search.SearchUsersFragment
import com.example.ajiekc.tochka.extensions.PreferenceHelper
import com.example.ajiekc.tochka.extensions.PreferenceHelper.clear
import com.example.ajiekc.tochka.extensions.PreferenceHelper.get
import com.example.ajiekc.tochka.extensions.loadRoundedImage
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.squareup.picasso.Picasso
import com.vk.sdk.VKSdk
import kotlinx.android.synthetic.main.activity_navigation.*
import kotlinx.android.synthetic.main.toolbar.*

class NavigationActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        fun start(activity: Activity) {
            val intent = Intent(activity, NavigationActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)

        updateNavHeader()

        if (savedInstanceState == null) {
            replaceFragment(SearchUsersFragment())
        }
    }

    private fun updateNavHeader() {
        val preferences = PreferenceHelper.defaultPrefs(this)
        val view = nav_view.getHeaderView(0)
        val userNameView = view.findViewById<TextView>(R.id.user_name_view)
        val userImageView = view.findViewById<ImageView>(R.id.user_image_view)
        userNameView.text = preferences[AuthActivity.USERNAME_PREF, ""]
        val photoUrl = preferences[AuthActivity.PHOTO_URL_PREF, ""]
        Picasso.get()
                .loadRoundedImage(photoUrl, userImageView, R.drawable.ic_user_placeholder)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                replaceFragment(SearchUsersFragment())
            }
            R.id.nav_logout -> {
                logout()
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.content, fragment)
                .commit()
    }

    private fun logout() {
        val preferences = PreferenceHelper.defaultPrefs(this)
        val authType = preferences[AuthActivity.AUTH_TYPE_PREF, ""]
        when (authType) {
            AuthType.VK.name -> VKSdk.logout()
            AuthType.FB.name -> LoginManager.getInstance().logOut()
            AuthType.GOOGLE.name -> {
                GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .signOut()
            }
        }
        preferences.clear()
        AuthActivity.start(this)
        finish()
    }
}
