package com.ssmmhh.jibam.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ssmmhh.jibam.di.factory.MainViewModelFactory
import com.ssmmhh.jibam.di.key.MainViewModelKey
import com.ssmmhh.jibam.feature_addedittransaction.detailedittransaction.DetailEditTransactionViewModel
import com.ssmmhh.jibam.feature_addedittransaction.inserttransaction.InsertTransactionViewModel
import com.ssmmhh.jibam.feature_categories.addcategoires.AddCategoryViewModel
import com.ssmmhh.jibam.feature_categories.viewcategories.ViewCategoriesViewModel
import com.ssmmhh.jibam.feature_chart.ChartViewModel
import com.ssmmhh.jibam.feature_transactions.TransactionsViewModel
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

    @FlowPreview
    @ExperimentalCoroutinesApi
    @Binds
    @IntoMap
    @MainViewModelKey(AddCategoryViewModel::class)
    abstract fun bindAddCategoryViewModel(
        addCategoryViewModel: AddCategoryViewModel
    ): ViewModel

}