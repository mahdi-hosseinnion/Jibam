package com.example.jibi.ui.main.transaction

import androidx.lifecycle.ViewModelProvider
import com.example.jibi.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject


@ExperimentalCoroutinesApi
@FlowPreview
class AboutUsFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory,
) :
    BaseTransactionFragment(R.layout.fragment_about_us, viewModelFactory, R.id.about_us_toolbar) {

}