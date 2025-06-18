package ru.sem.soilultrasound.presentation.scanner

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import ru.sem.soilultrasound.databinding.FragmentScannerBinding
import ru.sem.soilultrasound.navigator.BaseScreen
import ru.sem.soilultrasound.presentation.compose.PointPlotter
import ru.sem.soilultrasound.presentation.compose.PointsWrapper
import ru.sem.soilultrasound.utils.collectStarted
import ru.sem.soilultrasound.utils.showWarning

@AndroidEntryPoint
class ScannerFragment : Fragment() {

    class Screen() : BaseScreen

    private lateinit var binding: FragmentScannerBinding
    private val viewModel: ScannerVM by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners() {
        with(binding) {
            expand.setOnClickListener {
                scrollViewOutput.isVisible = scrollViewOutput.isGone
            }
            startScanningButton.setOnClickListener {
                viewModel.startScanning()
            }
            settingsButton.setOnClickListener {
                settingsScanner.root.isVisible = true
            }
            settingsScanner.closeIcon.setOnClickListener {
                settingsScanner.root.isVisible = false
            }
            sendPackButton.setOnClickListener {
                viewModel.sendSignals()
            }
        }
        initSettingsScreen()
        viewModel.state.collectStarted(viewLifecycleOwner, ::handleState)
//        viewModel.date.collectStarted(viewLifecycleOwner) { event -> //TODO для отслеживания сообщений вебсокета
//            event.getContentIfNotHandled()?.let {
//                with(binding.outputTv) {
//                    text = "$it"
//                }
//            }
//        }
    }

    private fun handleState(state: ScannerState) {
        binding.apply {
            state.showError?.getContentIfNotHandled()?.let {
                binding.textError.showWarning()
            }
            binding.graph.setContent {
                state.showResultScanning.forEach {
                    Log.d("DKSDKKSD:", "x:${it.x} y:${it.y}")
                }
                PointPlotter(PointsWrapper(state.showResultScanning))
            }
        }
    }

    private fun initSettingsScreen() {
        with(binding.settingsScanner) {
            seekBarFrequency.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    seekBarLowFrequency.progress = 0
                    val value = progress * MAX_FREQUENCY_VALUE / 100
                    frequencyValue.text = value.toString()
                    viewModel.setFrequency(value)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            })
            seekBarLowFrequency.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    seekBarFrequency.progress = 0
                    val value = progress * MAX_LOW_FREQUENCY_VALUE / 100
                    lowFrequencyValue.text = value.toString()
                    viewModel.setFrequency(value)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            })
            seekBarDutyCycle.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val value = progress * MAX_DUTY_CYCLE_VALUE / 100
                    dutyCycleValue.text = value.toString()
                    viewModel.setDutyCycle(value)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            })
            seekBarSignals.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val value = progress * MAX_SIGNALS_COUNT / 100
                    signalsValue.text = value.toString()
                    viewModel.setupSignalsCount(value)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            })
        }
    }

    companion object {
        const val MAX_FREQUENCY_VALUE = 40000
        const val MAX_LOW_FREQUENCY_VALUE = 40
        const val MAX_DUTY_CYCLE_VALUE = 255
        const val MAX_SIGNALS_COUNT = 20
    }
}