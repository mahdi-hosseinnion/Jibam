package com.ssmmhh.jibam.presentation.addedittransaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.ssmmhh.jibam.databinding.FragmentAddEditTransactionBinding
import com.ssmmhh.jibam.presentation.common.BaseFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class AddEditTransactionFragment(
    viewModelFactory: ViewModelProvider.Factory,
) : BaseFragment() {

    private lateinit var binding: FragmentAddEditTransactionBinding

    private val viewModel by viewModels<AddEditTransactionViewModel> { viewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddEditTransactionBinding.inflate(inflater, container, false).apply {

        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun handleStateMessages() {
        viewModel.stateMessage.observe(viewLifecycleOwner) {
            it?.let {
                //Change undo snack bar parent view to fragment's root
                handleNewStateMessage(it) { viewModel.clearStateMessage() }
            }
        }
    }

    override fun handleLoading() {
        viewModel.countOfActiveJobs.observe(
            viewLifecycleOwner
        ) {
            showProgressBar(viewModel.areAnyJobsActive())
        }
    }
}
