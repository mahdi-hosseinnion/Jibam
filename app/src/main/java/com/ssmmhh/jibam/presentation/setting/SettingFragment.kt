package com.ssmmhh.jibam.presentation.setting

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.databinding.FragmentAboutUsBinding
import com.ssmmhh.jibam.databinding.FragmentSettingBinding
import com.ssmmhh.jibam.presentation.common.BaseFragment
import com.ssmmhh.jibam.presentation.util.ToolbarLayoutListener
import com.ssmmhh.jibam.util.PreferenceKeys
import com.ssmmhh.jibam.util.PreferenceKeys.APP_CALENDAR_PREFERENCE
import com.ssmmhh.jibam.util.PreferenceKeys.CALENDAR_GREGORIAN
import com.ssmmhh.jibam.util.PreferenceKeys.CALENDAR_SOLAR
import com.ssmmhh.jibam.util.localizeNumber
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.util.*


@ExperimentalCoroutinesApi
@FlowPreview
class SettingFragment(
    viewModelFactory: ViewModelProvider.Factory,
    private val currentLocale: Locale,
    private val sharedPreferences: SharedPreferences,
    private val sharedPrefEditor: SharedPreferences.Editor
) : BaseFragment(), ToolbarLayoutListener {
    private lateinit var binding: FragmentSettingBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingBinding.inflate(inflater, container, false).apply {
            listener = this@SettingFragment
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
    }

    private fun initUi() {
        setDataToCalenderRadioGroup()
        binding.gregorianRb.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sharedPrefEditor.putString(APP_CALENDAR_PREFERENCE, CALENDAR_GREGORIAN)
                sharedPrefEditor.apply()
            }
        }
        binding.shamsiRb.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sharedPrefEditor.putString(APP_CALENDAR_PREFERENCE, CALENDAR_SOLAR)
                sharedPrefEditor.apply()
            }
        }
    }

    private fun setDataToCalenderRadioGroup() {
        val calendar = sharedPreferences.getString(
            APP_CALENDAR_PREFERENCE,
            PreferenceKeys.calendarDefault(currentLocale)
        )

        if (calendar == CALENDAR_SOLAR) {
            binding.shamsiRb.isChecked = true
        } else {
            binding.gregorianRb.isChecked = true
        }
    }


    override fun handleStateMessages() {}

    override fun handleLoading() {}

    override fun onClickOnNavigation(view: View) {
        navigateBack()
    }

    override fun onClickOnMenuButton(view: View) {}

}