package com.example.jibi.ui.main.transaction

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.jibi.ui.main.MainViewModel

abstract class BaseTransactionFragment
constructor(
    @LayoutRes
    private val layoutRes: Int,
    private val viewModelFactory: ViewModelProvider.Factory
) : Fragment(layoutRes) {
    val viewModel: MainViewModel by viewModels {
        viewModelFactory
    }
}