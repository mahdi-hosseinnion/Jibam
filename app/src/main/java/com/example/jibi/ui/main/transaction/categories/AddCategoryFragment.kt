package com.example.jibi.ui.main.transaction.categories

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.jibi.R
import com.example.jibi.models.Category
import com.example.jibi.models.CategoryImages
import com.example.jibi.ui.main.transaction.categories.state.CategoriesStateEvent
import com.example.jibi.ui.main.transaction.common.BaseFragment
import com.example.jibi.ui.main.transaction.transactions.TransactionsListAdapter
import com.example.jibi.util.*
import kotlinx.android.synthetic.main.fragment_add_category.*
import kotlinx.android.synthetic.main.fragment_transaction.*
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

    private val viewModel by viewModels<CategoriesViewModel> { viewModelFactory }

    private val args: AddCategoryFragmentArgs by navArgs()

    private lateinit var newCategory: Category

    private lateinit var recyclerAdapter: AddCategoryListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        //Showing the title
        topAppBar.title = when (args.categoryType) {
            EXPENSES -> {
                //TODO FIX ORDER ISSUE
                newCategory = Category(0, 1, edt_categoryName.text.toString(), "", 0)
                getString(R.string.add_expenses_category)
            }
            INCOME -> {
                //TODO FIX ORDER ISSUE
                newCategory = Category(0, 2, edt_categoryName.text.toString(), "", 0)

                getString(R.string.add_income_category)
            }
            else -> {
                showUnableToRecognizeCategoryTypeError()
                getString(R.string.unable_to_recognize_category_type)
            }
        }
        forceKeyBoardToOpenForEditText(edt_categoryName)

        edt_categoryName.addTextChangedListener {
            newCategory = newCategory.copy(name = it.toString())
        }
        topAppBar.setNavigationOnClickListener {
            uiCommunicationListener.hideSoftKeyboard()
            navigateBack()
        }
        initRecyclerView()
        subscribeObservers()
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

    private fun subscribeObservers() {
        viewModel.categoriesImages.observe(viewLifecycleOwner) {
            recyclerAdapter.submitList(it)
        }
        viewModel.viewState.observe(viewLifecycleOwner) { viewState ->
            viewState.insertedCategoryRow?.let {
                navigateBack()
            }
        }

    }

    private fun showUnableToRecognizeCategoryTypeError() {
        val callback = object : AreYouSureCallback {
            override fun proceed() {
                navigateBack()
            }

            override fun cancel() {
                navigateBack()
            }
        }
        val stateCallback = object : StateMessageCallback {
            override fun removeMessageFromStack() {
                navigateBack()
            }
        }

        uiCommunicationListener.onResponseReceived(
            Response(
                getString(R.string.unable_to_recognize_category_type),
                //TODO SHOW OK DIALOG
                UIComponentType.AreYouSureDialog(callback),
                MessageType.Error
            ), stateCallback
        )
    }

    companion object {
        const val EXPENSES = 1
        const val INCOME = 2
        const val RECYCLER_VIEW_SPAN_SIZE = 4
    }

    private fun initRecyclerView() {

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


            layoutManager = mLayoutManager
            adapter = recyclerAdapter
        }

    }


    override fun onItemSelected(position: Int, categoryImages: CategoryImages) {
        newCategory = newCategory.copy(img_res = categoryImages.image_res)
        //set image to image view
        setCategoryImageTo(categoryImages)
    }

    override fun restoreListPosition() {
    }

    fun setCategoryImageTo(categoryImages: CategoryImages) {
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
        val newCategory = newCategory
        if (isValidForInsertion(newCategory)) {
            viewModel.launchNewJob(
                CategoriesStateEvent.InsertCategory(
                    newCategory
                )
            )
            //TODO FIX THIS
            uiCommunicationListener.hideSoftKeyboard()
            findNavController().navigateUp()
        }
    }

    private fun isValidForInsertion(category: Category?): Boolean {
        if (category == null) {
            return false
        }
        return true
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save -> {
                insertNewCategory()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun forceKeyBoardToOpenForEditText(editText: EditText) {
        editText.requestFocus()
        val imm: InputMethodManager =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

}
