package com.example.jibi.di.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.jibi.di.main.keys.MainViewModelKey
import com.example.jibi.ui.main.transaction.addedittransaction.AddEditTransactionViewModel
import com.example.jibi.ui.main.transaction.categories.CategoriesViewModel
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
    @MainViewModelKey(AddEditTransactionViewModel::class)
    abstract fun bindAddEditTransactionViewModel(
        addEditTransactionViewModel: AddEditTransactionViewModel
    ): ViewModel

    @FlowPreview
    @ExperimentalCoroutinesApi
    @Binds
    @IntoMap
    @MainViewModelKey(CategoriesViewModel::class)
    abstract fun bindCategoriesViewModel(
        categoriesViewModel: CategoriesViewModel
    ): ViewModel

    @FlowPreview
    @ExperimentalCoroutinesApi
    @Binds
    @IntoMap
    @MainViewModelKey(ChartViewModel::class)
    abstract fun bindChartViewModel(
        chartViewModel: ChartViewModel
    ): ViewModel

}