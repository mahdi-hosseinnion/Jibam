package com.example.jibi.ui.main.transaction.chart

import android.content.res.Resources
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.jibi.R
import com.example.jibi.models.Category
import com.example.jibi.models.Record
import com.example.jibi.ui.main.transaction.BaseTransactionFragment
import com.example.jibi.ui.main.transaction.state.TransactionStateEvent
import kotlinx.android.synthetic.main.fragment_detail_chart.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
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

        val categoryId = args.categoryId
        if (categoryId > 0) {
            viewModel.launchNewJob(
                TransactionStateEvent.OneShotOperationsTransactionStateEvent.GetAllTransactionByCategoryId(
                    categoryId
                )
            )
            viewModel.launchNewJob(
                TransactionStateEvent.OneShotOperationsTransactionStateEvent.GetCategoryById(
                    categoryId
                )
            )
        } else {
            //TODO show unable snackBar and try again
        }
        subscribeObservers()
    }

    private fun subscribeObservers() {
        viewModel.viewState.observe(viewLifecycleOwner) { vs ->
            vs?.let { viewState ->
                viewState.detailChartFields.let {
                    it.category?.let { category ->
                        setCategoryDetail(category)
                    }
                    it.allTransaction?.let { allTransactions ->
                        setAllTransaction(allTransactions)
                    }
                }
            }
        }
    }

    private fun setCategoryDetail(category: Category) {
        txt_test.text = txt_test.text.toString() + "\n" + category.toString()
    }

    private fun setAllTransaction(transactionList: List<Record>) {
        txt_test.text = txt_test.text.toString() + "\n" + transactionList.toString()

    }

}