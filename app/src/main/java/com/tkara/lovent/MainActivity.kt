package com.tkara.lovent

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.tkara.lovent.fragments.HomeFragment
import com.tkara.lovent.fragments.DiscoverFragment
import com.tkara.lovent.fragments.CreateEventFragment
import com.tkara.lovent.fragments.MyEventsFragment
import com.tkara.lovent.fragments.ProfileFragment

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    // Session Manager
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Hide action bar
        supportActionBar?.hide()

        // Initialize session manager
        sessionManager = SessionManager.getInstance(this)

        // Check if user is logged in
        checkUserSession()

        // Initialize views
        initViews()

        // Set up navigation
        setupBottomNavigation()

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }
    }

    private fun initViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation)
    }

    private fun checkUserSession() {
        if (!sessionManager.isLoggedIn()) {
            // User not logged in, redirect to login
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_discover -> {
                    loadFragment(DiscoverFragment())
                    true
                }
                R.id.nav_create -> {
                    loadFragment(CreateEventFragment())
                    true
                }
                R.id.nav_events -> {
                    loadFragment(MyEventsFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }

        // Set default selection
        bottomNavigation.selectedItemId = R.id.nav_home
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        // Session'ı yenile
        sessionManager.refreshSession()
    }
}