package com.ssmmhh.jibam.presentation.categories.addcategoires

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.RequestManager
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.data.source.local.entity.CategoryEntity.Companion.EXPENSES_TYPE_MARKER
import com.ssmmhh.jibam.data.source.local.entity.CategoryEntity.Companion.INCOME_TYPE_MARKER
import com.ssmmhh.jibam.data.source.local.entity.CategoryImageEntity
import com.ssmmhh.jibam.data.util.DiscardOrSaveCallback
import com.ssmmhh.jibam.data.util.MessageType
import com.ssmmhh.jibam.data.util.UIComponentType
import com.ssmmhh.jibam.databinding.FragmentAddCategoryBinding
import com.ssmmhh.jibam.presentation.common.BaseFragment
import com.ssmmhh.jibam.presentation.util.ToolbarLayoutListener
import com.ssmmhh.jibam.presentation.util.forceKeyboardToOpenForEditText
import com.ssmmhh.jibam.util.EventObserver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview


@FlowPreview
@ExperimentalCoroutinesApi
class AddCategoryFragment(
    viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager
) : BaseFragment(), ToolbarLayoutListener {

    private val args: AddCategoryFragmentArgs by navArgs()

    private val viewModel by viewModels<AddCategoryViewModel> { viewModelFactory }

    private lateinit var binding: FragmentAddCategoryBinding

    private lateinit var recyclerAdapter: AddCategoryListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddCategoryBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
            listener = this@AddCategoryFragment
            lifecycleOwner = this@AddCategoryFragment.viewLifecycleOwner
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setCategoryType(args.categoryType)
        setupUI()
        initRecyclerView()
        subscribeObservers()
    }

    private fun setupUI() {
        forceKeyboardToOpenForEditText(requireActivity(), binding.edtCategoryName)
        //Add listener for if user try to navigate back with device back button.
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner
        ) {
            checkForDismissDialogBeforeNavigatingBack()
        }
    }

    private fun initRecyclerView() {

        binding.insertCategoryRecyclerView.apply {
            val mLayoutManager =
                GridLayoutManager(this@AddCategoryFragment.context, RECYCLER_VIEW_SPAN_SIZE)
            //control span size for full size item
            mLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (recyclerAdapter.getItemViewType(position)) {
                        AddCategoryListAdapter.HEADER_ITEM -> RECYCLER_VIEW_SPAN_SIZE//full size
                        else -> 1//one of four part(grid view)
                    }
                }
            }
            layoutManager = mLayoutManager
            recyclerAdapter = AddCategoryListAdapter(
                viewModel,
                requestManager,
            )
            adapter = recyclerAdapter
        }

    }

    private fun subscribeObservers() {
        viewModel.images.observe(viewLifecycleOwner) {
            recyclerAdapter.submitList(it)
        }
        viewModel.categoryType.observe(viewLifecycleOwner) {
            it?.let { type ->
                setCategoryTypeToolbar(type)
            }
        }
        viewModel.categorySuccessfullyInsertedEvent.observe(viewLifecycleOwner, EventObserver {
            navigateBack()
        })

    }

    private fun setCategoryTypeToolbar(categoryType: Int) {
        binding.toolbarTitle = when (categoryType) {
            EXPENSES_TYPE_MARKER -> getString(R.string.add_expenses_category)
            INCOME_TYPE_MARKER -> getString(R.string.add_income_category)
            else -> getString(R.string.unknown)
        }
    }

    override fun handleLoading() {
        viewModel.countOfActiveJobs.observe(
            viewLifecycleOwner
        ) {
            showProgressBar(viewModel.areAnyJobsActive())
        }
    }

    override fun handleStateMessages() {
        viewModel.stateMessage.observe(viewLifecycleOwner) {
            it?.let {
                handleNewStateMessage(it) { viewModel.clearStateMessage() }
            }
        }
    }

    private fun checkForDismissDialogBeforeNavigatingBack() {
        if (viewModel.categoryName.value.isNullOrBlank())
            navigateBack()
        else
            showDiscardOrSaveDialog()

    }

    override fun navigateBack() {
        activityCommunicationListener.hideSoftKeyboard()
        super.navigateBack()
    }


    private fun showDiscardOrSaveDialog() {
        val callback = object : DiscardOrSaveCallback {
            override fun save() {
                viewModel.insertCategory()
            }

            override fun discard() {
                navigateBack()
            }

            override fun cancel() {}
        }
        viewModel.addToMessageStack(
            message = intArrayOf(R.string.you_changes_have_not_saved),
            uiComponentType = UIComponentType.DiscardOrSaveDialog(callback),
            messageType = MessageType.Info
        )
    }

    override fun onClickOnNavigation(view: View) {
        checkForDismissDialogBeforeNavigatingBack()
    }

    override fun onClickOnMenuButton(view: View) {}

    companion object {
        const val RECYCLER_VIEW_SPAN_SIZE = 5
    }
}
