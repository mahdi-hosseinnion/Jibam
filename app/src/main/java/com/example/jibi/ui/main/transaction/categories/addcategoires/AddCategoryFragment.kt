package com.example.jibi.ui.main.transaction.categories.addcategoires

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.addCallback
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.jibi.R
import com.example.jibi.models.CategoryImages
import com.example.jibi.ui.main.transaction.categories.addcategoires.AddCategoryListAdapter.Companion.DEFAULT_CATEGORY_IMAGE_POSITION
import com.example.jibi.ui.main.transaction.categories.addcategoires.AddCategoryViewModel.Companion.INSERT_CATEGORY_SUCCESS_MARKER
import com.example.jibi.ui.main.transaction.common.BaseFragment
import com.example.jibi.ui.main.transaction.transactions.TransactionsListAdapter
import com.example.jibi.util.*
import com.example.jibi.util.Constants.EXPENSES_TYPE_MARKER
import com.example.jibi.util.Constants.INCOME_TYPE_MARKER
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_add_category.*
import kotlinx.android.synthetic.main.fragment_add_transaction.*
import kotlinx.android.synthetic.main.layout_category_images_list_item.view.*
import kotlinx.android.synthetic.main.layout_toolbar_with_back_btn.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject


@FlowPreview
@ExperimentalCoroutinesApi
class AddCategoryFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager
) : BaseFragment(
    R.layout.fragment_add_category
), AddCategoryListAdapter.Interaction {

    private val viewModel by viewModels<AddCategoryViewModel> { viewModelFactory }

    private val args: AddCategoryFragmentArgs by navArgs()

    private lateinit var recyclerAdapter: AddCategoryListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getCategoryTypeFromArgs(args.categoryType)

        setupUI()
        initRecyclerView()
        subscribeObservers()
    }

    private fun getCategoryTypeFromArgs(categoryType: Int) {

        if (categoryType == -1) {
            //default value
            //TODO ADD ON OK Clicked to dialog
            viewModel.addToMessageStack(
                message = getString(R.string.unable_to_recognize_category_type_pls_return_back),
                uiComponentType = UIComponentType.Dialog,
                messageType = MessageType.Error
            )
        } else {
            viewModel.setCategoryType(categoryType)
        }
    }

    private fun setupUI() {
        forceKeyBoardToOpenForEditText(edt_categoryName)
        //add on back pressed for if user insert something
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner
        ) {
            checkForInsertionBeforeNavigateBack()
        }
        /**
         * on clicks
         */
        topAppBar.setNavigationOnClickListener {
            checkForInsertionBeforeNavigateBack()
        }
        add_category_fab.setOnClickListener {
            insertNewCategory()
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

                if (message == getString(R.string.unable_to_recognize_category_type_pls_return_back)
                ) {
                    navigateBack()
                }

                if (message == getString(R.string.category_successfully_inserted)
                ) {
                    navigateBack()
                }
                add_category_fab.isEnabled = true
            }
        }
    }

    private fun subscribeObservers() {
        viewModel.categoriesImages.observe(viewLifecycleOwner) {
            recyclerAdapter.submitList(it)
        }
        viewModel.viewState.observe(viewLifecycleOwner) { vs ->
            vs?.let { viewState ->
                viewState.categoryImage?.let { setCategoryImageToImageView(it) }
                viewState.categoryType?.let { setCategoryTypeToolbar(it) }
            }
        }

    }


    companion object {
        const val EXPENSES = 1
        const val INCOME = 2
        const val RECYCLER_VIEW_SPAN_SIZE = 4
    }

    private fun initRecyclerView() {
        val onScrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    add_category_fab?.hide()
                } else {
                    add_category_fab?.show()
                }

            }

        }

        insert_category_recyclerView.apply {
            val mLayoutManager =
                GridLayoutManager(this@AddCategoryFragment.context, RECYCLER_VIEW_SPAN_SIZE)
            recyclerAdapter = AddCategoryListAdapter(
                requestManager,
                this@AddCategoryFragment,
                this@AddCategoryFragment.requireActivity().packageName
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

            addOnScrollListener(onScrollListener)

            layoutManager = mLayoutManager
            adapter = recyclerAdapter
        }

    }


    override fun onItemSelected(position: Int, categoryImages: CategoryImages) {
        viewModel.setCategoryImage(categoryImages)
        add_category_fab.show()
    }

    override fun restoreListPosition() {
    }

    private fun setCategoryTypeToolbar(categoryType: Int) {
        topAppBar.title =
            if (categoryType == EXPENSES_TYPE_MARKER)
                getString(R.string.add_expenses_category)
            else if (categoryType == INCOME_TYPE_MARKER)
                getString(R.string.add_income_category)
            else
                ""
    }

    private fun setCategoryImageToImageView(categoryImages: CategoryImages) {
        val categoryImageUrl = this.resources.getIdentifier(
            "ic_cat_${categoryImages.image_res}",
            "drawable",
            this@AddCategoryFragment.requireActivity().packageName
        )

        // set background
        if (categoryImages.id > 0) {
            cardView.setCardBackgroundColor(
                resources.getColor(
                    TransactionsListAdapter.listOfColor[(categoryImages.id.minus(
                        1
                    ))]
                )
            )
        }
        //load image
        requestManager
            .load(categoryImageUrl)
            .centerInside()
            .transition(DrawableTransitionOptions.withCrossFade())
            .error(R.drawable.ic_error)
            .into(category_image)
    }

    private fun insertNewCategory() {
        if (isValidForInsertion()) {
            add_category_fab.isEnabled = false
            val viewModelResponse = viewModel.insertCategory(edt_categoryName.text.toString())

            if (viewModelResponse != INSERT_CATEGORY_SUCCESS_MARKER) {
                add_category_fab.isEnabled = true
                showSnackBar(viewModelResponse)
            }

        }

    }

    private fun isValidForInsertion(): Boolean {
        if (edt_categoryName.text.toString().isBlank()) {
            showSnackBar(R.string.category_should_have_name)
            forceKeyBoardToOpenForEditText(edt_categoryName)
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
            add_category_fragment_root,
            getString(resId),
            Snackbar.LENGTH_SHORT
        ).setAnchorView(fab_submit).show()
    }

    private fun checkForInsertionBeforeNavigateBack() {
        if (edt_categoryName.text.toString().isNotBlank())
            showDiscardOrSaveDialog()
        else
            navigateBack()
    }

    override fun navigateBack() {
        uiCommunicationListener.hideSoftKeyboard()
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
            message = getString(R.string.you_changes_have_not_saved),
            uiComponentType = UIComponentType.DiscardOrSaveDialog(callback),
            messageType = MessageType.Info
        )
    }

}
