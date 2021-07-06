package com.example.jibi.ui.main.transaction.chart

import android.content.res.Resources
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.RequestManager
import com.example.jibi.R
import com.example.jibi.ui.main.transaction.BaseTransactionFragment
import kotlinx.android.synthetic.main.fragment_detail_chart.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.util.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
class DetailChartFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory,
    private val _resources: Resources
) : BaseTransactionFragment(
    R.layout.fragment_detail_chart,
    viewModelFactory,
    R.id.detailChartFragment_toolbar,
    _resources
) {

    val args: DetailChartFragmentArgs by navArgs()

    override fun setTextToAllViews() {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        txt_test.text = args.categoryId.toString()
    }

}