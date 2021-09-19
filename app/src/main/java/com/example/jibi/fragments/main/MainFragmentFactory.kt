package com.example.jibi.fragments.main

import android.content.SharedPreferences
import android.content.res.Resources
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.example.jibi.di.main.MainScope
import com.example.jibi.ui.main.transaction.aboutus.AboutUsFragment
import com.example.jibi.ui.main.transaction.addedittransaction.detailedittransaction.DetailEditTransactionFragment
import com.example.jibi.ui.main.transaction.addedittransaction.inserttransaction.InsertTransactionFragment
import com.example.jibi.ui.main.transaction.categories.AddCategoryFragment
import com.example.jibi.ui.main.transaction.categories.ViewCategoriesFragment
import com.example.jibi.ui.main.transaction.chart.ChartFragment
import com.example.jibi.ui.main.transaction.chart.DetailChartFragment
import com.example.jibi.ui.main.transaction.setting.SettingFragment
import com.example.jibi.ui.main.transaction.transactions.TransactionsFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@MainScope
class MainFragmentFactory
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager,
    private val currentLocale: Locale,
    private val sharedPreferences: SharedPreferences,
    private val sharedPrefsEditor: SharedPreferences.Editor
) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String) =

        when (className) {

            TransactionsFragment::class.java.name -> {
                TransactionsFragment(
                    viewModelFactory,
                    requestManager,
                    currentLocale,
                    sharedPreferences,
                    sharedPrefsEditor
                )
            }

            InsertTransactionFragment::class.java.name -> {
                InsertTransactionFragment(
                    viewModelFactory,
                    requestManager,
                    currentLocale,
                    sharedPreferences,
                    sharedPrefsEditor
                )
            }
            DetailEditTransactionFragment::class.java.name -> {
                DetailEditTransactionFragment(
                    viewModelFactory,
                    requestManager,
                    currentLocale,
                    sharedPreferences,
                    sharedPrefsEditor
                )
            }
            AddCategoryFragment::class.java.name -> {
                AddCategoryFragment(viewModelFactory, requestManager)
            }
            ViewCategoriesFragment::class.java.name -> {
                ViewCategoriesFragment(
                    viewModelFactory,
                    requestManager,
                    sharedPreferences,
                    sharedPrefsEditor
                )
            }
            SettingFragment::class.java.name -> {
                SettingFragment(
                    viewModelFactory,
                    currentLocale,
                    sharedPreferences,
                    sharedPrefsEditor
                )
            }
            AboutUsFragment::class.java.name -> {
                AboutUsFragment(
                    viewModelFactory,
                    requestManager
                )
            }
            ChartFragment::class.java.name -> {
                ChartFragment(
                    viewModelFactory,
                    requestManager,
                    currentLocale
                )
            }
            DetailChartFragment::class.java.name -> {
                DetailChartFragment(
                    viewModelFactory,
                    requestManager,
                    currentLocale,
                    sharedPreferences
                )
            }

            else -> {
                TransactionsFragment(
                    viewModelFactory, requestManager, currentLocale, sharedPreferences,
                    sharedPrefsEditor
                )
            }
        }


}