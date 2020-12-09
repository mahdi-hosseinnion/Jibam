package com.example.jibi.ui.main.transaction

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.jibi.R
import com.example.jibi.ui.UICommunicationListener
import com.example.jibi.ui.main.MainViewModel
import com.example.jibi.util.mahdiLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
abstract class BaseTransactionFragment
constructor(
    @LayoutRes
    private val layoutRes: Int,
    private val viewModelFactory: ViewModelProvider.Factory
) : Fragment(layoutRes) {
    private val TAG = "BaseTransactionFragment"

    init {
        mahdiLog(TAG, "called hash:${this.hashCode()}")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }



    protected val viewModel: MainViewModel by viewModels {
        viewModelFactory
    }
    lateinit var uiCommunicationListener: UICommunicationListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            uiCommunicationListener = context as UICommunicationListener
        } catch (e: ClassCastException) {
            Log.e(TAG, "$context must implement UICommunicationListener")
        }

    }

}