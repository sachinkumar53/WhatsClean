package com.sachin.app.whatsclean

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.hoc081098.viewbindingdelegate.viewBinding
import com.sachin.app.whatsclean.databinding.ActivityMainBinding
import com.sachin.app.whatsclean.util.extension.checkStoragePermission
import com.sachin.app.whatsclean.util.extension.findNavHostFragment
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private val binding: ActivityMainBinding by viewBinding()
    private val viewModel: MainViewModel by viewModels()
    private lateinit var appBarConfiguration: AppBarConfiguration


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val c = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        if (!c) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                456
            )
        }
        setSupportActionBar(binding.toolbar)

        val navController = setupNavController()

        setupUIComponents(navController)

        subscribeUi()
    }

    private fun setupNavController(): NavController {
        val navHostFragment = findNavHostFragment(binding.fragmentContainerView.id)
        val navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)

        navGraph.setStartDestination(
            if (checkStoragePermission()) {
                if (viewModel.needsAuthentication(this))
                    R.id.authenticationFragment
                else R.id.dashboardFragment
            } else R.id.permissionFragment
        )

        navController.graph = navGraph

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.dashboardFragment, R.id.permissionFragment),
            binding.root
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        return navController
    }

    private fun setupUIComponents(navController: NavController) {
        binding.navigationView
            .getHeaderView(0)
            ?.findViewById<TextView>(
                R.id.app_version
            )?.text = "Version ${BuildConfig.VERSION_NAME}"

        binding.navigationView.setNavigationItemSelectedListener {
            viewModel.onNavigationItemSelected(this, navController, it.itemId)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.root.setDrawerLockMode(
                if (destination.id == R.id.dashboardFragment)
                    LOCK_MODE_UNLOCKED
                else LOCK_MODE_LOCKED_CLOSED,
                GravityCompat.START
            )
            val isExceptionFragment = setOf(
                R.id.permissionFragment,
                R.id.authenticationFragment
            ).any { id ->
                destination.id == id
            }

            binding.adviewContainer.apply {
                if (isExceptionFragment && isVisible)
                    isVisible = false
                else if (!isExceptionFragment && !isVisible)
                    isVisible = true
            }
        }
    }


    private fun subscribeUi() {
        viewModel.theme.observe(this) {
            viewModel.setAppTheme(it)
        }

    }


    override fun onStart() {
        super.onStart()
        val navController = findNavController(R.id.fragmentContainerView)
        if (
            viewModel.needsAuthentication(this) &&
            navController.currentDestination?.id != R.id.authenticationFragment
        ) {
            val options = NavOptions.Builder()
                .setPopUpTo(R.id.dashboardFragment, true)
                .build()

            navController.navigate(R.id.authenticationFragment, null, options)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.fragmentContainerView)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

}