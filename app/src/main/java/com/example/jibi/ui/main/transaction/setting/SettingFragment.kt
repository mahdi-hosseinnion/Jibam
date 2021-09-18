package com.example.jibi.ui.main.transaction.setting

import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.jibi.R
import com.example.jibi.ui.main.transaction.common.BaseFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject


@ExperimentalCoroutinesApi
@FlowPreview
class SettingFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory,
    private val sharedPreferences: SharedPreferences
) : BaseFragment(
    R.layout.fragment_setting,
    R.id.setting_toolbar
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findNavController()
            .currentDestination?.label = getString(R.string.setting)

    }

    override fun handleStateMessages() {}

    override fun handleLoading() {}

}