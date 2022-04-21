package com.ssmmhh.jibam.presentation.setting

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.databinding.FragmentAboutUsBinding
import com.ssmmhh.jibam.databinding.FragmentSettingBinding
import com.ssmmhh.jibam.presentation.common.BaseFragment
import com.ssmmhh.jibam.presentation.transactions.TransactionsViewModel
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
) : BaseFragment(), ToolbarLayoutListener {

    private lateinit var binding: FragmentSettingBinding

    private val viewModel by viewModels<SettingViewModel> { viewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingBinding.inflate(inflater, container, false).apply {
            viewmodel = this@SettingFragment.viewModel
            listener = this@SettingFragment
        }
        return binding.root
    }

    override fun handleStateMessages() {}

    override fun handleLoading() {}

    override fun onClickOnNavigation(view: View) {
        navigateBack()
    }

    override fun onClickOnMenuButton(view: View) {}

}