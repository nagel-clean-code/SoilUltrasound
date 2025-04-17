package ru.sem.soilultrasound

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.FragmentManager
import dagger.hilt.android.AndroidEntryPoint
import ru.sem.soilultrasound.databinding.ActivityMainBinding
import ru.sem.soilultrasound.navigator.MainNavigator
import ru.sem.soilultrasound.navigator.Navigator
import ru.sem.soilultrasound.presentation.scanner.ScannerFragment
import ru.sem.soilultrasound.presentation.settings.SettingsFragment
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), Navigator {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var navigator: MainNavigator

    private val viewModel: MainActivityVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigationView.setOnItemSelectedListener {
            val screen = when (it.itemId) {
                R.id.scanner -> ScannerFragment.Screen()
                R.id.settings -> SettingsFragment.Screen()
//                R.id.library -> ProfileFragment.Screen()
                else -> ScannerFragment.Screen()
            }
            clearBackStack()
            navigator.launchFragment(this, screen, false)
            true
        }
    }

    override fun showSettingsFragment() {
        navigator.launchFragment(this, SettingsFragment.Screen())
    }

    override fun showScannerFragment() {
        navigator.launchFragment(this, ScannerFragment.Screen())
    }

    override fun clearBackStack() {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    override fun goBack() {
        if (supportFragmentManager.backStackEntryCount != 0) {
            onBackPressed()
        }
    }
}