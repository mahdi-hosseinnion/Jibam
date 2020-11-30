package com.example.jibi.ui.main.transaction

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.example.jibi.R
import com.example.jibi.di.main.MainScope
import javax.inject.Inject

@MainScope
class CreateTransactionFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory
) : BaseTransactionFragment(
    R.layout.fragment_create_transaction,
    viewModelFactory
) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}