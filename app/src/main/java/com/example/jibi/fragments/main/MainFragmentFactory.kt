package com.example.jibi.fragments.main

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.example.jibi.di.main.MainScope
import com.example.jibi.ui.main.transaction.AddTransactionFragment
import com.example.jibi.ui.main.transaction.DetailTransFragment
import com.example.jibi.ui.main.transaction.TransactionFragment
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
                TransactionFragment(viewModelFactory, requestManager)
            }

            AddTransactionFragment::class.java.name -> {
                AddTransactionFragment(viewModelFactory)
            }
            DetailTransFragment::class.java.name -> {
                DetailTransFragment(viewModelFactory, requestManager, currentLocale)
            }

            else -> {
                TransactionFragment(viewModelFactory, requestManager)
            }
        }


}