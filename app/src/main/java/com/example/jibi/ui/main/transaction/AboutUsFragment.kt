package com.example.jibi.ui.main.transaction

import android.content.res.Resources
import androidx.lifecycle.ViewModelProvider
import com.example.jibi.R
import kotlinx.android.synthetic.main.fragment_about_us.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject


@ExperimentalCoroutinesApi
@FlowPreview
class AboutUsFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory,
    private val _resources: Resources
) :
    BaseTransactionFragment(R.layout.fragment_about_us, viewModelFactory, R.id.about_us_toolbar,_resources) {
    override fun setTextToAllViews() {
        about_us.text=_getString(R.string.about_us)
    }
}