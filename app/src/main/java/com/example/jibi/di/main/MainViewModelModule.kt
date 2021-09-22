package com.example.jibi.di.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.jibi.di.main.keys.MainViewModelKey
import com.example.jibi.ui.main.transaction.addedittransaction.detailedittransaction.DetailEditTransactionViewModel
import com.example.jibi.ui.main.transaction.addedittransaction.inserttransaction.InsertTransactionViewModel
import com.example.jibi.ui.main.transaction.categories.viewcategories.ViewCategoriesViewModel
import com.example.jibi.ui.main.transaction.chart.ChartViewModel
import com.example.jibi.ui.main.transaction.transactions.TransactionsViewModel
import com.example.jibi.viewmodels.MainViewModelFactory
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@Module
abstract class MainViewModelModule {

    @Binds
    abstract fun bindViewModelFactory(factory: MainViewModelFactory): ViewModelProvider.Factory

    @FlowPreview
    @ExperimentalCoroutinesApi
    @Binds
    @IntoMap
    @MainViewModelKey(TransactionsViewModel::class)
    abstract fun bindTransactionsViewModel(
        transactionsViewModel: TransactionsViewModel
    ): ViewModel


    @FlowPreview
    @ExperimentalCoroutinesApi
    @Binds
    @IntoMap
    @MainViewModelKey(ViewCategoriesViewModel::class)
    abstract fun bindCategoriesViewModel(
        categoriesViewModel: ViewCategoriesViewModel
    ): ViewModel

    @FlowPreview
    @ExperimentalCoroutinesApi
    @Binds
    @IntoMap
    @MainViewModelKey(ChartViewModel::class)
    abstract fun bindChartViewModel(
        chartViewModel: ChartViewModel
    ): ViewModel

    @FlowPreview
    @ExperimentalCoroutinesApi
    @Binds
    @IntoMap
    @MainViewModelKey(InsertTransactionViewModel::class)
    abstract fun bindInsertTransactionViewModel(
        insertTransactionViewModel: InsertTransactionViewModel
    ): ViewModel

    @FlowPreview
    @ExperimentalCoroutinesApi
    @Binds
    @IntoMap
    @MainViewModelKey(DetailEditTransactionViewModel::class)
    abstract fun bindDetailEditTransactionViewModel(
        detailEditTransactionViewModel: DetailEditTransactionViewModel
    ): ViewModel

}