package com.example.jibi.ui.main.transaction

import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.provider.SyncStateContract
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.jibi.R
import com.example.jibi.util.Constants
import com.example.jibi.util.LocaleHelper
import com.example.jibi.util.PreferenceKeys
import kotlinx.android.synthetic.main.fragment_setting.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Appendable
import javax.inject.Inject


@ExperimentalCoroutinesApi
@FlowPreview
class SettingFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory,
    private val _resources: Resources,
    private val sharedPreferences: SharedPreferences
) :
    BaseTransactionFragment(
        R.layout.fragment_setting,
        viewModelFactory,
        R.id.setting_toolbar,
        _resources
    ) {
    override fun setTextToAllViews() {
        language.text = _getString(R.string.language)
        warning.text = _getString(R.string.changelanugaealart)
        default_radio.text = _getString(R.string.system_default)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findNavController()
            .currentDestination?.label = _getString(R.string.setting)

        val appLanguage = sharedPreferences.getString(
            PreferenceKeys.APP_LANGUAGE_PREF,
            Constants.APP_DEFAULT_LANGUAGE
        )

        if (appLanguage == Constants.SYSTEM_DEFAULT_LANG_CODE) {
            default_radio.isChecked = true
        } else if (appLanguage == Constants.PERSIAN_LANG_CODE) {
            persion_radio.isChecked = true
        } else {
            english_radio.isChecked = true
        }
        language_radio_group.setOnCheckedChangeListener { radioGroup, i ->
            when (i) {
                R.id.default_radio -> {
                    if (appLanguage == Constants.SYSTEM_DEFAULT_LANG_CODE) {
                        return@setOnCheckedChangeListener
                    }
                    LocaleHelper.setLocale(requireContext(), Constants.SYSTEM_DEFAULT_LANG_CODE)
                    recreateApp()
                }
                R.id.persion_radio -> {
                    if (appLanguage == Constants.PERSIAN_LANG_CODE) {
                        return@setOnCheckedChangeListener
                    }
                    LocaleHelper.setLocale(requireContext(), Constants.PERSIAN_LANG_CODE)
                    recreateApp()
                }
                R.id.english_radio -> {
                    if (appLanguage == Constants.ENGLISH_LANG_CODE) {
                        return@setOnCheckedChangeListener
                    }
                    LocaleHelper.setLocale(requireContext(), Constants.ENGLISH_LANG_CODE)

                    recreateApp()

                }
            }
        }

    }

    fun recreateApp() {
        uiCommunicationListener.showProgressBar(true)
        lifecycleScope.launch {
            delay(1000)
            uiCommunicationListener.showProgressBar(false)
            uiCommunicationListener.recreateActivity()
        }
    }
}