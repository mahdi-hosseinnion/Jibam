package com.example.jibi.ui.main.transaction

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.jibi.R
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