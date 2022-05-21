package com.ssmmhh.jibam.presentation.setting

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.ssmmhh.jibam.databinding.FragmentSettingBinding
import com.ssmmhh.jibam.presentation.common.BaseFragment
import com.ssmmhh.jibam.presentation.util.ToolbarLayoutListener
import com.ssmmhh.jibam.util.EventObserver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview


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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeObservers()
    }

    private fun subscribeObservers() {
        viewModel.calendarTypeChangedEvent.observe(viewLifecycleOwner, EventObserver {
            notifyThePreviousFragmentThatCalendarTypeHasChanged()
        })
    }

    override fun handleStateMessages() {}

    override fun handleLoading() {}

    override fun onClickOnNavigation(view: View) {
        navigateBack()
    }

    override fun onClickOnMenuButton(view: View) {}

    private fun notifyThePreviousFragmentThatCalendarTypeHasChanged() {
        Log.d(
            "update calendar type",
            "notifyThePreviousBackStackEntryThatCalendarTypeHasChanged: called"
        )
        //Notifies the previous back stack that the calendar type has changed.
        findNavController().previousBackStackEntry?.savedStateHandle?.set(
            DID_CALENDAR_TYPE_CHANGE,
            true
        )
    }

    companion object {
        const val DID_CALENDAR_TYPE_CHANGE = "didCalendarTypeChange"
    }
}