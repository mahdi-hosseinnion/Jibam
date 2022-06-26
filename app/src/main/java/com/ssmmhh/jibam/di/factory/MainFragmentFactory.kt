package com.ssmmhh.jibam.di.factory

import android.content.SharedPreferences
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.ssmmhh.jibam.presentation.aboutus.AboutUsFragment
import com.ssmmhh.jibam.presentation.addedittransaction.AddEditTransactionFragment
import com.ssmmhh.jibam.presentation.categories.addcategoires.AddCategoryFragment
import com.ssmmhh.jibam.presentation.categories.viewcategories.ViewCategoriesFragment
import com.ssmmhh.jibam.presentation.chart.chart.ChartFragment
import com.ssmmhh.jibam.presentation.chart.detailchart.DetailChartFragment
import com.ssmmhh.jibam.presentation.setting.SettingFragment
import com.ssmmhh.jibam.presentation.transactiondetail.TransactionDetailFragment
import com.ssmmhh.jibam.presentation.transactions.TransactionsFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@FlowPreview
@ExperimentalCoroutinesApi
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
                    currentLocale,
                    sharedPreferences,
                )
            }

            AddCategoryFragment::class.java.name -> {
                AddCategoryFragment(viewModelFactory)
            }
            ViewCategoriesFragment::class.java.name -> {
                ViewCategoriesFragment(
                    viewModelFactory,
                    requestManager,
                )
            }
            SettingFragment::class.java.name -> {
                SettingFragment(
                    viewModelFactory,
                )
            }
            AboutUsFragment::class.java.name -> {
                AboutUsFragment()
            }
            ChartFragment::class.java.name -> {
                ChartFragment(
                    viewModelFactory,
                )
            }
            DetailChartFragment::class.java.name -> {
                DetailChartFragment(
                    viewModelFactory,
                    currentLocale,
                    sharedPreferences
                )
            }
            AddEditTransactionFragment::class.java.name -> {
                AddEditTransactionFragment(
                    viewModelFactory,
                    requestManager,
                    sharedPreferences,
                )
            }
            TransactionDetailFragment::class.java.name -> {
                TransactionDetailFragment(
                    viewModelFactory,
                    sharedPreferences,
                )
            }

            else -> {
                super.instantiate(classLoader, className)
            }
        }


}