package com.example.project

import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.project.databinding.ActivityMainBinding
import com.example.project.ui.auth.AuthUser
import com.example.project.ui.auth.User
import com.example.project.ui.auth.isInvalid

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var authUser: AuthUser

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // Initialize AuthUser
        authUser = AuthUser(activityResultRegistry)
        lifecycle.addObserver(authUser)

        val navView: BottomNavigationView = binding.navView

        // Using the correct ID that matches the XML
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Define top level destinations - no back button for these
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_vocabulary, R.id.navigation_learning, R.id.navigation_practice
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        observeUserAuthentication() // Set up observer for user changes
    }

    private fun observeUserAuthentication() {
        authUser.observeUser().observe(this) { user ->
            Log.d(TAG, "User changed: $user")
            updateUI(user)
            if (user.isInvalid()) {
                authUser.login() // Trigger login if invalid
            }
        }
    }

    private fun updateUI(user: User) {
        // Update UI elements based on user authentication state
        binding.toolbar.title = if (user.isInvalid()) {
            getString(R.string.app_name)
        } else {
            "${getString(R.string.app_name)} - ${user.name}"
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}