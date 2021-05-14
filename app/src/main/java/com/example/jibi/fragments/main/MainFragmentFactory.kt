package com.example.jibi.fragments.main

import android.content.SharedPreferences
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
    private val currentLocale: Locale,
    private val sharedPreferences: SharedPreferences,
    private val sharedPrefsEditor: SharedPreferences.Editor
) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String) =

        when (className) {

            TransactionFragment::class.java.name -> {
                TransactionFragment(
                    viewModelFactory,
                    requestManager,
                    currentLocale,
                    sharedPreferences,
                    sharedPrefsEditor
                )
            }

            AddTransactionFragment::class.java.name -> {
                AddTransactionFragment(viewModelFactory, requestManager, currentLocale,sharedPreferences,sharedPrefsEditor)
            }
            AddCategoryFragment::class.java.name -> {
                AddCategoryFragment(viewModelFactory, requestManager)
            }
            ViewCategoriesFragment::class.java.name -> {
                ViewCategoriesFragment(viewModelFactory, requestManager,sharedPreferences,sharedPrefsEditor)
            }
            SettingFragment::class.java.name -> {
                SettingFragment(viewModelFactory)
            }
            AboutUsFragment::class.java.name -> {
                AboutUsFragment(viewModelFactory)
            }

            else -> {
                TransactionFragment(
                    viewModelFactory, requestManager, currentLocale, sharedPreferences,
                    sharedPrefsEditor
                )
            }
        }


}