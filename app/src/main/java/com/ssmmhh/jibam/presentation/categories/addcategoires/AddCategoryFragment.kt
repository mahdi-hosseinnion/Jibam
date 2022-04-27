package com.ssmmhh.jibam.presentation.categories.addcategoires

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.addCallback
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.snackbar.Snackbar
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.data.source.local.entity.CategoryEntity.Companion.EXPENSES_TYPE_MARKER
import com.ssmmhh.jibam.data.source.local.entity.CategoryEntity.Companion.INCOME_TYPE_MARKER
import com.ssmmhh.jibam.data.source.local.entity.CategoryImageEntity
import com.ssmmhh.jibam.data.util.DiscardOrSaveCallback
import com.ssmmhh.jibam.data.util.MessageType
import com.ssmmhh.jibam.data.util.UIComponentType
import com.ssmmhh.jibam.databinding.FragmentAddCategoryBinding
import com.ssmmhh.jibam.presentation.categories.addcategoires.AddCategoryViewModel.Companion.INSERT_CATEGORY_SUCCESS_MARKER
import com.ssmmhh.jibam.presentation.common.BaseFragment
import com.ssmmhh.jibam.presentation.util.ToolbarLayoutListener
import com.ssmmhh.jibam.util.localizeNumber
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview


@FlowPreview
@ExperimentalCoroutinesApi
class AddCategoryFragment(
    viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager
) : BaseFragment(), AddCategoryListAdapter.Interaction, ToolbarLayoutListener {

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
        forceKeyBoardToOpenForEditText(binding.edtCategoryName)
        //add on back pressed for if user insert something
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner
        ) {
            checkForInsertionBeforeNavigateBack()
        }
        binding.addCategoryFab.setOnClickListener {
            insertNewCategory()
        }
    }

    private fun initRecyclerView() {

        binding.insertCategoryRecyclerView.apply {
            val mLayoutManager =
                GridLayoutManager(this@AddCategoryFragment.context, RECYCLER_VIEW_SPAN_SIZE)
            recyclerAdapter = AddCategoryListAdapter(
                requestManager,
                this@AddCategoryFragment,
            )
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
            adapter = recyclerAdapter
        }

    }

    private fun subscribeObservers() {
        viewModel.categoriesImageEntity.observe(viewLifecycleOwner) {
            recyclerAdapter.submitList(it)
        }
        viewModel.viewState.observe(viewLifecycleOwner) { vs ->
            vs?.let { viewState ->
                viewState.categoryType?.let { setCategoryTypeToolbar(it) }
            }
        }

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
                val message = it.response.message

                if (message.contentEquals(intArrayOf(R.string.unable_to_recognize_category_type_pls_return_back))
                ) {
                    navigateBack()
                }

                if (message.contentEquals(intArrayOf(R.string.category_successfully_inserted))
                ) {
                    navigateBack()
                }
                binding.addCategoryFab.isEnabled = true
            }
        }
    }

    override fun onItemSelected(position: Int, categoryImageEntity: CategoryImageEntity) {
        viewModel.setCategoryImage(categoryImageEntity)
    }

    override fun restoreListPosition() {
    }

    private fun insertNewCategory() {
        if (isValidForInsertion()) {
            binding.addCategoryFab.isEnabled = false
            val viewModelResponse =
                viewModel.insertCategory(binding.edtCategoryName.text.toString())

            if (viewModelResponse != INSERT_CATEGORY_SUCCESS_MARKER) {
                binding.addCategoryFab.isEnabled = true
                showSnackBar(viewModelResponse)
            }

        }

    }

    private fun isValidForInsertion(): Boolean {
        if (binding.edtCategoryName.text.toString().isBlank()) {
            showSnackBar(R.string.category_should_have_name)
            forceKeyBoardToOpenForEditText(binding.edtCategoryName)
            return false
        }
        return true
    }


    private fun forceKeyBoardToOpenForEditText(editText: EditText) {
        editText.requestFocus()
        val imm: InputMethodManager =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun showSnackBar(@StringRes resId: Int) {
        Snackbar.make(
            binding.addCategoryFragmentRoot,
            getString(resId),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun checkForInsertionBeforeNavigateBack() {
        if (binding.edtCategoryName.text.toString().isNotBlank())
            showDiscardOrSaveDialog()
        else
            navigateBack()
    }

    override fun navigateBack() {
        activityCommunicationListener.hideSoftKeyboard()
        super.navigateBack()
    }


    private fun showDiscardOrSaveDialog() {
        val callback = object : DiscardOrSaveCallback {
            override fun save() {
                insertNewCategory()
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
        checkForInsertionBeforeNavigateBack()
    }

    override fun onClickOnMenuButton(view: View) {}

    companion object {
        const val RECYCLER_VIEW_SPAN_SIZE = 5
    }
}
