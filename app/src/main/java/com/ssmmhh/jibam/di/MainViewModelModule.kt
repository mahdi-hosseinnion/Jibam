package com.ssmmhh.jibam.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ssmmhh.jibam.di.factory.MainViewModelFactory
import com.ssmmhh.jibam.di.key.MainViewModelKey
import com.ssmmhh.jibam.presentation.addedittransaction.AddEditTransactionViewModel
import com.ssmmhh.jibam.presentation.categories.addcategoires.AddCategoryViewModel
import com.ssmmhh.jibam.presentation.categories.viewcategories.ViewCategoriesViewModel
import com.ssmmhh.jibam.presentation.chart.chart.ChartViewModel
import com.ssmmhh.jibam.presentation.chart.detailchart.DetailChartViewModel
import com.ssmmhh.jibam.presentation.setting.SettingViewModel
import com.ssmmhh.jibam.presentation.transactiondetail.TransactionDetailViewModel
import com.ssmmhh.jibam.presentation.transactions.TransactionsViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

/**
 * Bind view models with view model factory
 */
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
    @MainViewModelKey(DetailChartViewModel::class)
    abstract fun bindDetailChartViewModel(
        detailChartViewModel: DetailChartViewModel
    ): ViewModel

    @FlowPreview
    @ExperimentalCoroutinesApi
    @Binds
    @IntoMap
    @MainViewModelKey(AddCategoryViewModel::class)
    abstract fun bindAddCategoryViewModel(
        addCategoryViewModel: AddCategoryViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @MainViewModelKey(SettingViewModel::class)
    abstract fun bindSettingViewModel(
        settingViewModel: SettingViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @MainViewModelKey(AddEditTransactionViewModel::class)
    abstract fun bindAddEditTransactionViewModel(
        addEditTransactionViewModel: AddEditTransactionViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @MainViewModelKey(TransactionDetailViewModel::class)
    abstract fun bindTransactionDetailViewModel(
        transactionDetailViewModel: TransactionDetailViewModel
    ): ViewModel

}