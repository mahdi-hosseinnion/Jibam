package com.ssmmhh.jibam.ui.main.transaction.setting

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.ui.main.transaction.common.BaseFragment
import com.ssmmhh.jibam.util.PreferenceKeys
import com.ssmmhh.jibam.util.PreferenceKeys.APP_CALENDAR_PREFERENCE
import com.ssmmhh.jibam.util.PreferenceKeys.CALENDAR_GREGORIAN
import com.ssmmhh.jibam.util.PreferenceKeys.CALENDAR_SOLAR
import kotlinx.android.synthetic.main.fragment_setting.*
import kotlinx.android.synthetic.main.layout_toolbar_with_back_btn.*
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
) : BaseFragment(
    R.layout.fragment_setting
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        topAppBar.title = getString(R.string.setting)
        initUi()
    }

    private fun initUi() {
        topAppBar.setNavigationOnClickListener {
            navigateBack()
        }

        setDataToCalenderRadioGroup()

        gregorian_rb.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sharedPrefEditor.putString(APP_CALENDAR_PREFERENCE, CALENDAR_GREGORIAN)
                sharedPrefEditor.apply()
            }
        }
        shamsi_rb.setOnCheckedChangeListener { _, isChecked ->
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
            shamsi_rb.isChecked = true
        } else {
            gregorian_rb.isChecked = true
        }
    }


    override fun handleStateMessages() {}

    override fun handleLoading() {}

}