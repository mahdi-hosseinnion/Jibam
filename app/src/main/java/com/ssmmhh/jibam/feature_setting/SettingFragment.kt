package com.ssmmhh.jibam.feature_setting

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.databinding.FragmentSettingBinding
import com.ssmmhh.jibam.feature_common.BaseFragment
import com.ssmmhh.jibam.util.PreferenceKeys
import com.ssmmhh.jibam.util.PreferenceKeys.APP_CALENDAR_PREFERENCE
import com.ssmmhh.jibam.util.PreferenceKeys.CALENDAR_GREGORIAN
import com.ssmmhh.jibam.util.PreferenceKeys.CALENDAR_SOLAR
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
) : BaseFragment() {

    private var _binding: FragmentSettingBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.topAppBarNormal.title = getString(R.string.setting)
        initUi()
    }

    private fun initUi() {
        binding.toolbar.topAppBarNormal.setNavigationOnClickListener {
            navigateBack()
        }

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

}