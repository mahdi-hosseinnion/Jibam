package com.example.jibi.ui.main.transaction

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.jibi.R
import com.example.jibi.models.Category
import com.example.jibi.models.CategoryImages
import com.example.jibi.ui.main.transaction.state.TransactionStateEvent
import com.example.jibi.util.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
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
    private val requestManager: RequestManager
) : BaseTransactionFragment(
    R.layout.fragment_add_category,
    viewModelFactory,
    R.id.add_category_toolbar
), AddCategoryListAdapter.Interaction {

    private val args: AddCategoryFragmentArgs by navArgs()

    private lateinit var recyclerAdapter: AddCategoryListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Showing the title
        findNavController()
            .currentDestination?.label = when (args.categoryType) {
            EXPENSES -> getString(R.string.add_expenses_category)
            INCOME -> getString(R.string.add_income_category)
            else -> {
                showUnableToRecognizeCategoryTypeError()
                getString(R.string.unable_to_recognize_category_type)
            }
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

}
