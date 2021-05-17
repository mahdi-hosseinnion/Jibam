package com.example.jibi.ui.main.transaction

import android.content.res.Resources
import android.os.Bundle
import android.view.*
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.jibi.R
import com.example.jibi.models.Category
import com.example.jibi.models.CategoryImages
import com.example.jibi.ui.main.transaction.state.TransactionStateEvent
import com.example.jibi.util.*
import kotlinx.android.synthetic.main.fragment_add_category.*
import kotlinx.android.synthetic.main.fragment_transaction.*
import kotlinx.android.synthetic.main.layout_category_images_list_item.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject


@FlowPreview
@ExperimentalCoroutinesApi
class AddCategoryFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager,
    private val _resources: Resources
) : BaseTransactionFragment(
    R.layout.fragment_add_category,
    viewModelFactory,
    R.id.add_category_toolbar, _resources
), AddCategoryListAdapter.Interaction {
    override fun setTextToAllViews() {
        edt_categoryName.hint = _getString(R.string.category_name)
    }

    private val args: AddCategoryFragmentArgs by navArgs()

    private lateinit var newCategory: Category

    private lateinit var recyclerAdapter: AddCategoryListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        //Showing the title
        findNavController()
            .currentDestination?.label = when (args.categoryType) {
            EXPENSES -> {
                //TODO FIX ORDER ISSUE
                newCategory = Category(0, 1, edt_categoryName.text.toString(), "", 0)
                _getString(R.string.add_expenses_category)
            }
            INCOME -> {
                //TODO FIX ORDER ISSUE
                newCategory = Category(0, 2, edt_categoryName.text.toString(), "", 0)

                _getString(R.string.add_income_category)
            }
            else -> {
                showUnableToRecognizeCategoryTypeError()
                _getString(R.string.unable_to_recognize_category_type)
            }
        }

        edt_categoryName.addTextChangedListener {
            newCategory = newCategory.copy(name = it.toString())
        }

        initRecyclerView()
        subscribeObservers()
    }

    private fun subscribeObservers() {
        viewModel.getCategoryImages().observe(viewLifecycleOwner) {
            recyclerAdapter.submitList(it)
        }
    }

    private fun showUnableToRecognizeCategoryTypeError() {
        val callback = object : AreYouSureCallback {
            override fun proceed() {
                findNavController().navigateUp()
            }

            override fun cancel() {
                findNavController().navigateUp()
            }
        }
        val stateCallback = object : StateMessageCallback {
            override fun removeMessageFromStack() {
                findNavController().navigateUp()

            }
        }

        uiCommunicationListener.onResponseReceived(
            Response(
                _getString(R.string.unable_to_recognize_category_type),
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
                this@AddCategoryFragment.requireActivity().packageName,
                _resources
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
                    TransactionListAdapter.listOfColor[(categoryImages.id.minus(
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
                TransactionStateEvent.OneShotOperationsTransactionStateEvent.InsertCategory(
                    newCategory
                ), true
            )
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

}
