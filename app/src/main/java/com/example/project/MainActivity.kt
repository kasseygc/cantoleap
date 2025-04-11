package com.example.project

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.project.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Define top level destinations - no back button for these
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_vocabulary, R.id.navigation_learning, R.id.navigation_practice
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Add menu to enable Demo Mode
        val demoMenuItem = navView.menu.add(0, R.id.menu_demo_mode, 0, getString(R.string.demo_mode))
        demoMenuItem.setIcon(android.R.drawable.ic_menu_manage)
        demoMenuItem.setOnMenuItemClickListener {
            // Show demo mode dialog
            showDemoModeDialog()
            true
        }
    }

    private fun showDemoModeDialog() {
        val dialog = DemoModeDialog()
        dialog.show(supportFragmentManager, "DemoModeDialog")
    }
}