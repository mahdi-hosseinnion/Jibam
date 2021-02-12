package com.example.jibi.ui.main.transaction

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.jibi.ui.UICommunicationListener
import com.example.jibi.ui.main.MainViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
abstract class BaseTransactionFragment
constructor(
    @LayoutRes
    private val layoutRes: Int,
    private val viewModelFactory: ViewModelProvider.Factory,
    @IdRes
    private val toolbar: Int? = null,
) : Fragment(layoutRes) {
    private val TAG = "BaseTransactionFragment"
    protected val viewModel: MainViewModel by viewModels(ownerProducer = { requireParentFragment() }) {
        viewModelFactory
    }
    lateinit var uiCommunicationListener: UICommunicationListener
    lateinit var _View: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _View = view
    }

    override fun onResume() {
        super.onResume()
        toolbar?.let {
            uiCommunicationListener.setupActionBarWithNavController(_View.findViewById(toolbar))
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            uiCommunicationListener = context as UICommunicationListener
        } catch (e: ClassCastException) {
            Log.e(TAG, "$context must implement UICommunicationListener")
        }

    }
}