package com.example.jibi.fragments.main

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.example.jibi.di.main.MainScope
import com.example.jibi.ui.main.transaction.*
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
    private val currentLocale: Locale
) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String) =

        when (className) {

            TransactionFragment::class.java.name -> {
                TransactionFragment(viewModelFactory, requestManager, currentLocale)
            }

            AddTransactionFragment::class.java.name -> {
                AddTransactionFragment(viewModelFactory, requestManager, currentLocale)
            }
            DetailTransFragment::class.java.name -> {
                DetailTransFragment(viewModelFactory, requestManager, currentLocale)
            }
            AddCategoryFragment::class.java.name -> {
                AddCategoryFragment(viewModelFactory, requestManager)
            }
            ViewCategoriesFragment::class.java.name -> {
                ViewCategoriesFragment(viewModelFactory, requestManager)
            }
            SettingFragment::class.java.name -> {
                SettingFragment(viewModelFactory)
            }
            AboutUsFragment::class.java.name -> {
                AboutUsFragment(viewModelFactory)
            }

            else -> {
                TransactionFragment(viewModelFactory, requestManager, currentLocale)
            }
        }


}