package com.example.jibi.ui.main.transaction

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.jibi.R
import com.example.jibi.di.main.MainScope
import kotlinx.android.synthetic.main.fragment_transaction.*
import javax.inject.Inject

@MainScope
class TransactionFragment
@Inject
constructor() : Fragment(R.layout.fragment_transaction) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lastTransaction.setOnClickListener {
            findNavController().navigate(R.id.action_transactionFragment_to_createTransactionFragment)
        }
    }

}