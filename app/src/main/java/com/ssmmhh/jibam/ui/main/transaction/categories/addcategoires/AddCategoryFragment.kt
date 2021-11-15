package com.ssmmhh.jibam.ui.main.transaction.categories.addcategoires

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.addCallback
import androidx.annotation.StringRes
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.snackbar.Snackbar
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.databinding.FragmentAddCategoryBinding
import com.ssmmhh.jibam.models.CategoryImages
import com.ssmmhh.jibam.ui.main.transaction.categories.addcategoires.AddCategoryViewModel.Companion.INSERT_CATEGORY_SUCCESS_MARKER
import com.ssmmhh.jibam.ui.main.transaction.common.BaseFragment
import com.ssmmhh.jibam.util.*
import com.ssmmhh.jibam.util.Constants.EXPENSES_TYPE_MARKER
import com.ssmmhh.jibam.util.Constants.INCOME_TYPE_MARKER
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview


@FlowPreview
@ExperimentalCoroutinesApi
class AddCategoryFragment(
    viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager
) : BaseFragment(), AddCategoryListAdapter.Interaction {

    private val viewModel by viewModels<AddCategoryViewModel> { viewModelFactory }

    private val args: AddCategoryFragmentArgs by navArgs()

    private lateinit var recyclerAdapter: AddCategoryListAdapter

    private var _binding: FragmentAddCategoryBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddCategoryBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

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
        forceKeyBoardToOpenForEditText(binding.edtCategoryName)
        //add on back pressed for if user insert something
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner
        ) {
            checkForInsertionBeforeNavigateBack()
        }
        binding.edtCategoryName.addTextChangedListener {
            if (!binding.addCategoryFab.isShown) {
                binding.addCategoryFab.show()
            }
        }
        /**
         * on clicks
         */
        binding.toolbar?.topAppBarNormal?.setNavigationOnClickListener {
            checkForInsertionBeforeNavigateBack()
        }
        binding.addCategoryFab.setOnClickListener {
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
                binding.addCategoryFab.isEnabled = true
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
        const val RECYCLER_VIEW_SPAN_SIZE = 5
    }

    private fun initRecyclerView() {
        val onScrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    binding.addCategoryFab?.hide()
                } else {
                    binding.addCategoryFab?.show()
                }

            }

        }

        binding.insertCategoryRecyclerView.apply {
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
        binding.addCategoryFab.show()
    }

    override fun restoreListPosition() {
    }

    private fun setCategoryTypeToolbar(categoryType: Int) {
        binding.toolbar?.topAppBarNormal?.title =
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
        binding.cardView.setCardBackgroundColor(
            resources.getColor(
                CategoriesImageBackgroundColors.getCategoryColorById(categoryImages.id)
            )
        )
        //load image
        requestManager
            .load(categoryImageUrl)
            .centerInside()
            .transition(DrawableTransitionOptions.withCrossFade())
            .error(R.drawable.ic_error)
            .into(binding.categoryImage)
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
