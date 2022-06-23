package com.ssmmhh.jibam.presentation.transactiondetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.ssmmhh.jibam.databinding.FragmentTransactionDetailBinding
import com.ssmmhh.jibam.presentation.common.BaseFragment
import com.ssmmhh.jibam.presentation.util.ToolbarLayoutListener
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class TransactionDetailFragment(
    viewModelFactory: ViewModelProvider.Factory,
) : BaseFragment(), ToolbarLayoutListener {

    private lateinit var binding: FragmentTransactionDetailBinding

    private val viewModel by viewModels<TransactionDetailViewModel> { viewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTransactionDetailBinding.inflate(inflater, container, false).apply {
            listener = this@TransactionDetailFragment
        }
        return binding.root
    }

    override fun handleStateMessages() {
        viewModel.stateMessage.observe(viewLifecycleOwner) {
            it?.let {
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

    override fun onClickOnNavigation(view: View) {
        navigateBack()
    }

    override fun onClickOnMenuButton(view: View) {
        TODO("Not yet implemented")
    }
}