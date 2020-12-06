package com.example.jibi.fragments.main

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.example.jibi.di.main.MainScope
import com.example.jibi.ui.main.transaction.AddTransactionFragment
import com.example.jibi.ui.main.transaction.TransactionFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
@MainScope
class MainFragmentFactory
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory
) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String) =

        when (className) {

            TransactionFragment::class.java.name -> {
                TransactionFragment(viewModelFactory)
            }

            AddTransactionFragment::class.java.name -> {
                AddTransactionFragment(viewModelFactory)
            }

            else -> {
                TransactionFragment(viewModelFactory)
            }
        }


}