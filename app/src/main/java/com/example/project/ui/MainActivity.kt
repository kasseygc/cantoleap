package com.example.project

import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
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
    private lateinit var navController: NavController

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

        // Initialize NavController
        navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Define top level destinations - no back button for these
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_vocabulary, R.id.navigation_learning, R.id.navigation_practice
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Set up bottom navigation with special handling
        navView.setOnItemSelectedListener { menuItem ->
            // Clear the back stack when switching between tabs
            val builder = NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setPopUpTo(
                    when (menuItem.itemId) {
                        R.id.navigation_vocabulary -> R.id.navigation_vocabulary
                        R.id.navigation_learning -> R.id.navigation_learning
                        R.id.navigation_practice -> R.id.navigation_practice
                        else -> R.id.navigation_vocabulary
                    }, true
                )

            when (menuItem.itemId) {
                R.id.navigation_vocabulary ->
                    navController.navigate(R.id.navigation_vocabulary, null, builder.build())
                R.id.navigation_learning -> {
                    // If coming from another tab, we need to pass a default category
                    val bundle = Bundle().apply {
                        putString("category", "greetings") // Default category
                    }
                    navController.navigate(R.id.navigation_learning, bundle, builder.build())
                }
                R.id.navigation_practice ->
                    navController.navigate(R.id.navigation_practice, null, builder.build())
            }

            true
        }

        observeUserAuthentication()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
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
        binding.toolbar.title = "CantoLeap"
    }
}