package com.example.jibi.ui.main.transaction.common

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.jibi.ui.UICommunicationListener
import com.example.jibi.ui.main.MainViewModel
import com.example.jibi.util.StateMessageCallback
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
abstract class BaseFragment
constructor(
    @LayoutRes
    private val layoutRes: Int,
    private val viewModelFactory: ViewModelProvider.Factory,
    @IdRes
    private val toolbar: Int? = null,
    private val _resources: Resources
) : Fragment(layoutRes) {
    private val TAG = "BaseTransactionFragment"

    //    protected val viewModel: MainViewModel by viewModels(ownerProducer = { requireParentFragment() }) {
//        viewModelFactory
//    }
//    protected val viewModel by viewModels<ViewModel> { getViewModelFactory() }

    lateinit var uiCommunicationListener: UICommunicationListener
    lateinit var _View: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _View = view
//        subscribeToObservers()
    }

//TODO FIX THIS 6724
//    private fun subscribeToObservers() {
//
//        viewModel.countOfActiveJobs.observe(viewLifecycleOwner) {
//            uiCommunicationListener.showProgressBar(viewModel.areAnyJobsActive())
//
//        }
//
//        viewModel.stateMessage.observe(viewLifecycleOwner) { stateMessage ->
//            stateMessage?.let {
//                uiCommunicationListener.onResponseReceived(
//                    response = it.response,
//                    stateMessageCallback = object : StateMessageCallback {
//                        override fun removeMessageFromStack() {
//                            viewModel.clearStateMessage()
//                        }
//                    }
//                )
//            }
//        }
//    }


    override fun onResume() {
        super.onResume()
        setTextToAllViews()
        toolbar?.let {
            uiCommunicationListener.setupActionBarWithNavController(_View.findViewById(toolbar))
        }
    }

    fun _getString(@StringRes resId: Int): String {
        return _resources.getString(resId)
    }

    //its needed to farsi support
    abstract fun setTextToAllViews()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            uiCommunicationListener = context as UICommunicationListener
        } catch (e: ClassCastException) {
            Log.e(TAG, "$context must implement UICommunicationListener")
        }

    }

}