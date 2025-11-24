package rs.nikolaivanovic.ssfitnessdnevniktreninga

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import java.util.*
import androidx.core.content.edit

class MainActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        // Apply theme before super.onCreate
        when (prefs.getString("theme", "system")) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }

        val lang = prefs.getString("language", null) ?: run {
            prefs.edit { putString("language", "sr") }
            "sr"
        }
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        @Suppress("DEPRECATION")
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        WindowCompat.setDecorFitsSystemWindows(window, false)

        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        val isNight = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        val navBarColor = if (isNight) {
            ContextCompat.getColor(this, R.color.your_app_main_dark_background)
        } else {
            ContextCompat.getColor(this, R.color.white)
        }
        @Suppress("DEPRECATION")
        window.navigationBarColor = navBarColor
        @Suppress("DEPRECATION")
        window.statusBarColor = navBarColor

        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = !isNight
        insetsController.isAppearanceLightNavigationBars = !isNight

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main, HomeFragment())
                .commit()
        }

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbarMain)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_menu)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        toolbar.navigationIcon?.setTint(getColor(R.color.white))

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main, HomeFragment())
                        .commit()
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_workouts -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main, WorkoutsFragment())
                        .commit()
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_workout_history -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main, WorkoutHistoryFragment())
                        .commit()
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_contact -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main, ContactFragment())
                        .commit()
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_settings -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main, SettingsFragment())
                        .commit()
                    drawerLayout.closeDrawers()
                    true
                }
                else -> {
                    drawerLayout.closeDrawers()
                    true
                }
            }
        }
    }
}