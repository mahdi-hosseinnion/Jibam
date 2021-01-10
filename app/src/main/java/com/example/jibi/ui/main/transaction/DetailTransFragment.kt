package com.example.jibi.ui.main.transaction

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.example.jibi.R
import com.example.jibi.models.Record
import kotlinx.android.synthetic.main.fragment_detail_trans.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
class DetailTransFragment
@Inject

constructor(
    viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager,
    private val currentLocale: Locale

) : BaseTransactionFragment(
    R.layout.fragment_detail_trans,
    viewModelFactory
) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeObservers()
    }


    private fun subscribeObservers() {
        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            viewState?.detailTransFields?.let {
                setTranProperties(it)
            }
        })
    }

    private fun setTranProperties(trans: Record) {
        edt_money_detail.setText(trans.money.toString())

        trans.memo?.let { edt_memo_detail.setText(it) }
        setDateProperties(trans.date)
    }

    private fun setDateProperties(time: Int) {
        val dv: Long = ((time.toLong()) * 1000) // its need to be in milisecond
        val df: Date = Date(dv)
        txt_date_detail.setText(SimpleDateFormat("MM/dd/yy (E)", currentLocale).format(df))
        txt_time_detail.setText(SimpleDateFormat("K:m a", currentLocale).format(df))
    }

    override fun onResume() {
        uiCommunicationListener.showToolbar()
        super.onResume()
    }
}