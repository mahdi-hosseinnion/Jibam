package com.ssmmhh.jibam.fragments.main

import android.content.SharedPreferences
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.ssmmhh.jibam.di.main.MainScope
import com.ssmmhh.jibam.ui.main.transaction.aboutus.AboutUsFragment
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.detailedittransaction.DetailEditTransactionFragment
import com.ssmmhh.jibam.ui.main.transaction.addedittransaction.inserttransaction.InsertTransactionFragment
import com.ssmmhh.jibam.ui.main.transaction.categories.addcategoires.AddCategoryFragment
import com.ssmmhh.jibam.ui.main.transaction.categories.viewcategories.ViewCategoriesFragment
import com.ssmmhh.jibam.ui.main.transaction.chart.ChartFragment
import com.ssmmhh.jibam.ui.main.transaction.chart.DetailChartFragment
import com.ssmmhh.jibam.ui.main.transaction.setting.SettingFragment
import com.ssmmhh.jibam.ui.main.transaction.transactions.TransactionsFragment
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