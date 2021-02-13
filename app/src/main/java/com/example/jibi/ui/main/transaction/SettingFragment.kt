package com.example.jibi.ui.main.transaction

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.jibi.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject


@ExperimentalCoroutinesApi
@FlowPreview
class SettingFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory,
) :
    BaseTransactionFragment(R.layout.fragment_setting, viewModelFactory, R.id.setting_toolbar) {

}