package com.example.jibi.ui.main.transaction.setting

import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.jibi.R
import com.example.jibi.ui.main.transaction.common.BaseFragment
import com.example.jibi.util.PreferenceKeys
import com.example.jibi.util.PreferenceKeys.APP_CALENDAR_PREFERENCE
import com.example.jibi.util.PreferenceKeys.CALENDAR_GREGORIAN
import com.example.jibi.util.PreferenceKeys.CALENDAR_SOLAR
import com.example.jibi.util.isFarsi
import kotlinx.android.synthetic.main.fragment_setting.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.util.*
import javax.inject.Inject


@ExperimentalCoroutinesApi
@FlowPreview
class SettingFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory,
    private val currentLocale: Locale,
    private val sharedPreferences: SharedPreferences,
    private val sharedPrefEditor: SharedPreferences.Editor
) : BaseFragment(
    R.layout.fragment_setting,
    R.id.setting_toolbar
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findNavController()
            .currentDestination?.label = getString(R.string.setting)
        initUi()
    }

    private fun initUi() {
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