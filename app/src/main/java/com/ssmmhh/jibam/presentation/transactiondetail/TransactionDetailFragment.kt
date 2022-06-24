package com.ssmmhh.jibam.presentation.transactiondetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.ssmmhh.jibam.databinding.FragmentTransactionDetailBinding
import com.ssmmhh.jibam.presentation.common.BaseFragment
import com.ssmmhh.jibam.presentation.util.ToolbarLayoutListener
import com.ssmmhh.jibam.util.EventObserver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class TransactionDetailFragment(
    viewModelFactory: ViewModelProvider.Factory,
) : BaseFragment(), ToolbarLayoutListener {

    private lateinit var binding: FragmentTransactionDetailBinding

    private val viewModel by viewModels<TransactionDetailViewModel> { viewModelFactory }

    private val navigationArgs: TransactionDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTransactionDetailBinding.inflate(inflater, container, false).apply {
            listener = this@TransactionDetailFragment
            this.viewmodel = viewModel
            this.lifecycleOwner = this@TransactionDetailFragment.viewLifecycleOwner
            this.toolbar.topAppBarImgBtn.visibility = View.VISIBLE
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.start(navigationArgs.transactionId)
        subscribeObservers()
    }

    private fun subscribeObservers() {
        viewModel.navigateToEditTransactionEvent.observe(viewLifecycleOwner, EventObserver {
            navigateToEditTransaction()
        })
        viewModel.navigateBackEvent.observe(viewLifecycleOwner, EventObserver {
            navigateBack()
        })
    }

    private fun navigateToEditTransaction() {
        val action =
            TransactionDetailFragmentDirections.actionTransactionDetailFragmentToAddEditTransactionFragment(
                navigationArgs.transactionId
            )
        findNavController().navigate(action)
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
        viewModel.showAreYouSureDialogBeforeDelete()
    }
}