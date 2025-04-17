package ru.sem.soilultrasound.presentation.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import ru.sem.soilultrasound.R
import ru.sem.soilultrasound.databinding.FragmentScannerBinding
import ru.sem.soilultrasound.databinding.FragmentSettingsBinding
import ru.sem.soilultrasound.navigator.BaseScreen
import ru.sem.soilultrasound.presentation.scanner.ScannerVM
import ru.sem.soilultrasound.utils.showToast

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    class Screen() : BaseScreen

    private lateinit var binding: FragmentSettingsBinding
    private val viewModel: SettingsVM by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getIpAddress()?.let { text ->
            binding.ipAddressText.setText(text)
        }
        initListeners()
    }

    private fun initListeners() {
        binding.apply {
            saveButton.setOnClickListener {
                val ip = ipAddressText.text.toString()
                viewModel.saveIpAddress(ip)
                showToast(R.string.save_success, requireContext())
            }
        }
    }
}