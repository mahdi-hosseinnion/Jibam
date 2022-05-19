package com.ssmmhh.jibam.presentation.transactions

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.data.model.Month
import com.ssmmhh.jibam.data.source.local.dto.TransactionDto
import com.ssmmhh.jibam.data.util.UIComponentType
import com.ssmmhh.jibam.databinding.FragmentTransactionBinding
import com.ssmmhh.jibam.presentation.common.BaseFragment
import com.ssmmhh.jibam.presentation.transactions.state.TransactionsViewState
import com.ssmmhh.jibam.presentation.util.MonthChangerToolbarLayoutListener
import com.ssmmhh.jibam.presentation.util.ToolbarLayoutListener
import com.ssmmhh.jibam.presentation.util.forceKeyboardToOpenForEditText
import com.ssmmhh.jibam.util.*
import com.ssmmhh.jibam.util.PreferenceKeys.APP_CALENDAR_PREFERENCE
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.math.BigDecimal
import java.util.*

@FlowPreview
@ExperimentalCoroutinesApi
class TransactionsFragment(
    viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager,
    private val currentLocale: Locale,
    private val sharedPreferences: SharedPreferences,
    private val sharedPrefsEditor: SharedPreferences.Editor
) : BaseFragment(),
    TransactionsListAdapter.Interaction,
    ToolbarLayoutListener,
    MonthChangerToolbarLayoutListener {

    private val TAG = "TransactionFragment"

    private val viewModel by viewModels<TransactionsViewModel> { viewModelFactory }

    private lateinit var recyclerAdapter: TransactionsListAdapter

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private lateinit var binding: FragmentTransactionBinding

    private lateinit var transactionsBottomSheetAnimator: TransactionsBottomSheetAnimator

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTransactionBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
            this.lifecycleOwner = this@TransactionsFragment.viewLifecycleOwner
            toolbarListener = this@TransactionsFragment
            monthChangerListener = this@TransactionsFragment
            onBackPressedCallback = this@TransactionsFragment.onBackPressedCallback
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setToolbarNavigationToMenuIcon()
        setupNavigationItemSelectedListener()
        initBottomSheetBehavior()
        initBottomSheetAnimator()
        initRecyclerView()
        subscribeObservers()

        //Handle back 'button' calls for bottom sheet and search view
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )
    }

    /**
     * Handle backstack for when the bottom sheet is in expanded state.
     * Disable search mode if it is enable.
     * Collapse bottom sheet if it is in expanded state.
     */
    private val onBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            //check for search view
            if (viewModel.isSearchVisible()) {
                viewModel.disableSearchState()
            } else {
                binding.transactionRecyclerView.scrollToPosition(0)
                bottomSheetBehavior.state = STATE_COLLAPSED
                this.isEnabled = false
            }
        }

    }

    private fun setToolbarNavigationToMenuIcon() {
        binding.transactionToolbar.topAppBarMonth.navigationIcon =
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.ic_round_menu_24,
                requireContext().theme
            )
        binding.transactionToolbar.topAppBarMonth.navigationContentDescription =
            getString(R.string.navigation_drawer_cd)

    }

    private fun setupNavigationItemSelectedListener() {
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->

            when (menuItem.itemId) {
                R.id.chartFragment -> {
                    findNavController().navigate(R.id.action_transactionFragment_to_chartFragment)
                }
                R.id.viewCategoriesFragment -> {
                    findNavController().navigate(R.id.action_transactionFragment_to_viewCategoriesFragment)
                }
                R.id.settingFragment -> {
                    findNavController().navigate(R.id.action_transactionFragment_to_settingFragment)
                }
                R.id.aboutUsFragment -> {
                    findNavController().navigate(R.id.action_transactionFragment_to_aboutUsFragment)
                }
            }

            return@setNavigationItemSelectedListener true
        }
    }

    private fun initBottomSheetBehavior() {
        bottomSheetBehavior = from(binding.mainStandardBottomSheet)

        bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
        bottomSheetCallback.onStateChanged(
            bottomSheet = binding.mainStandardBottomSheet,
            newState = bottomSheetBehavior.state
        )
    }

    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            onBackPressedCallback.isEnabled = newState == STATE_EXPANDED
            if (newState == STATE_EXPANDED && viewModel.isSearchVisible()) {
                // If theres bug with search bar this line is dangerous and bugkhiz
                //we should use this here b/c when user click on search and bottom sheet is in collapse state
                // edit text will crash the appbar
                binding.bottomSheetSearchEdt.visibility = View.VISIBLE
                forceKeyboardToOpenForEditText(requireActivity(), binding.bottomSheetSearchEdt)
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
    }

    private fun initBottomSheetAnimator() {
        transactionsBottomSheetAnimator = TransactionsBottomSheetAnimator(
            context = this.requireContext(),
            bottomSheet = binding.mainStandardBottomSheet,
            bottomSheetAppBar = binding.lastTransacionAppBar,
            backButton = binding.mainBottomSheetBackArrow,
            searchButton = binding.mainBottomSheetSearchBtn,
            bottomSheetTopHandler = binding.viewHastam,
            bottomSheetTopHandlerPin = binding.viewHastam2,
        )
        bottomSheetBehavior.addBottomSheetCallback(transactionsBottomSheetAnimator)
        transactionsBottomSheetAnimator.onStateChanged(
            bottomSheet = binding.mainStandardBottomSheet,
            newState = bottomSheetBehavior.state
        )
    }

    private fun initRecyclerView() {

        val onScrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (bottomSheetBehavior.state == STATE_EXPANDED) {
                    if (dy > 0) {
                        binding.addFab.hide()
                    } else {
                        binding.addFab.show()
                    }

                }
            }

            override fun onScrollStateChanged(
                recyclerView: RecyclerView,
                newState: Int
            ) {
                super.onScrollStateChanged(recyclerView, newState)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastPosition =
                    layoutManager.findFirstCompletelyVisibleItemPosition()
                if (lastPosition == 0) {
                    //user should be able to only drag the bottom sheet down when it's in first
                    //position
                    if (viewModel.isSearchInvisible()) {
                        //while searching you should not be able to drag down
                        bottomSheetBehavior.isDraggable = true
                    }
                } else {
                    bottomSheetBehavior.isDraggable = false
                }
            }
        }
        val swipeToDeleteHandler =
            object : SwipeToDeleteCallback(this@TransactionsFragment.requireContext()) {
                override fun onSwiped(
                    viewHolder: RecyclerView.ViewHolder,
                    direction: Int
                ) {
                    val adapter = binding.transactionRecyclerView.adapter as TransactionsListAdapter

                    val deletedTrans = adapter.removeItemAt(viewHolder.adapterPosition) ?: return

                    viewModel.deleteTransaction(deletedTrans)
                }
            }
        binding.transactionRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@TransactionsFragment.context)
            recyclerAdapter = TransactionsListAdapter(
                requestManager,
                this@TransactionsFragment,
                currentLocale,
                isCalendarSolar = sharedPreferences.isCalendarSolar(currentLocale)
            )
            addOnScrollListener(onScrollListener)

            val itemTouchHelper = ItemTouchHelper(swipeToDeleteHandler)
            itemTouchHelper.attachToRecyclerView(binding.transactionRecyclerView)

            binding.transactionRecyclerView.isNestedScrollingEnabled = true
            adapter = recyclerAdapter
        }

    }

    private fun subscribeObservers() {

        viewModel.viewState.observe(viewLifecycleOwner) { vs ->
            vs?.let { viewState ->
                viewState.currentMonth?.let { setMonthFieldsValues(it) }
            }
        }
        viewModel.transactions.observe(viewLifecycleOwner) { transactionList ->
            recyclerAdapter.submitList(transactionList)
        }
        viewModel.summeryMoney.observe(viewLifecycleOwner) { sm ->
            binding.txtBalance.text = separate3By3AndRoundIt(sm.balance, currentLocale)

            if (sm.expenses != BigDecimal.ZERO) {
                binding.txtExpenses.text =
                    separate3By3AndRoundIt(sm.expenses.negate(), currentLocale)
            } else {
                binding.txtExpenses.text = separate3By3AndRoundIt(BigDecimal.ZERO, currentLocale)
            }
            binding.txtIncome.text = separate3By3AndRoundIt(sm.income, currentLocale)
        }

        viewModel.searchViewState.observe(viewLifecycleOwner) {
            when (it) {
                TransactionsViewState.SearchViewState.VISIBLE -> {
                    enableSearchMode()
                }
                TransactionsViewState.SearchViewState.INVISIBLE -> {
                    disableSearchMode()
                }
            }

        }
        viewModel.navigateToAddTransactionEvent.observe(viewLifecycleOwner, EventObserver {
            navigateToAddTransactionFragment()
        })
    }

    private fun setMonthFieldsValues(month: Month) {
        val year = month.year?.let { "\n${it.toLocaleString()}" } ?: ""
        binding.transactionToolbar.toolbarMonthChanger.toolbarMonth.text =
            resources.getString(month.monthNameResId) + year
    }

    private fun enableSearchMode() {
        bottomSheetBehavior.state = STATE_EXPANDED
        //user shouldn't be able to drag down when searchView is enable
        bottomSheetBehavior.isDraggable = false
        //disconnect recyclerView to bottomSheet
        binding.transactionRecyclerView.isNestedScrollingEnabled = false

        binding.bottomSheetSearchClear.visibility = View.INVISIBLE

        //invisible the non-search stuff
        binding.mainBottomSheetSearchBtn.visibility = View.GONE
        binding.bottomSheetTitle.visibility = View.GONE

        if (bottomSheetBehavior.state == STATE_EXPANDED) {
            binding.bottomSheetSearchEdt.visibility = View.VISIBLE
            forceKeyboardToOpenForEditText(requireActivity(), binding.bottomSheetSearchEdt)
        }
    }

    private fun disableSearchMode() {

        activityCommunicationListener.hideSoftKeyboard()
        //invisible search stuff
        binding.bottomSheetSearchEdt.visibility = View.GONE
        binding.bottomSheetSearchClear.visibility = View.GONE

        //visible non-search stuff
        binding.mainBottomSheetSearchBtn.visibility = View.VISIBLE
        binding.bottomSheetTitle.visibility = View.VISIBLE

        //make bottom sheet draggable again after disabling searchView
        bottomSheetBehavior.isDraggable = true
        //connect recyclerView to bottomSheet
        binding.transactionRecyclerView.isNestedScrollingEnabled = true
        //submit that
        binding.bottomSheetSearchEdt.setText("")
    }

    private fun navigateToAddTransactionFragment() {
        //on category selected and bottomSheet hided
        val action =
            TransactionsFragmentDirections.actionTransactionFragmentToCreateTransactionFragment()
        findNavController().navigate(action)
    }

    override fun onResume() {
        super.onResume()
        closeDrawerIfItIsOpen(false)

        //check for calendar type
        checkForCalendarTypeChange()
        //enable backStack listener when user  navigate to next fragment or rotate screen
        if (bottomSheetBehavior.state == STATE_EXPANDED || viewModel.isSearchVisible()) {
            onBackPressedCallback.isEnabled = true
        }

        if (bottomSheetBehavior.state == STATE_EXPANDED)
            transactionsBottomSheetAnimator.setAnimationStateToExpandedMode()
        else if (bottomSheetBehavior.state == STATE_COLLAPSED) {
            transactionsBottomSheetAnimator.setAnimationStateToCollapsedMode()
        }
        calculateTransactionsBottomSheetPeekHeight()
    }

    private fun calculateTransactionsBottomSheetPeekHeight() {
        binding.fragmentTransacionRoot.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.fragmentTransacionRoot.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val rootHeight = binding.fragmentTransacionRoot.height
                val layoutHeight = binding.transactionFragmentView.height
                bottomSheetBehavior.peekHeight = (rootHeight - layoutHeight).coerceAtLeast(0)
            }
        })

    }

    private fun closeDrawerIfItIsOpen(animate: Boolean = true) {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START, animate)
        }
    }

    private fun checkForCalendarTypeChange() {

        val viewModelValue = viewModel.getCalenderType()
        val prefValue = sharedPreferences.getString(
            APP_CALENDAR_PREFERENCE,
            PreferenceKeys.calendarDefault(currentLocale)
        )

        if (viewModelValue == prefValue) {
            return
        }
        if (viewModelValue.isNullOrBlank()) {
            viewModel.setCalenderType(prefValue)
            return
        }
        if (viewModelValue != prefValue && prefValue != null) {
            viewModel.calenderTypeHaveBeenChangedTo(prefValue)
        }

    }

    override fun onDestroyView() {
        bottomSheetBehavior.removeBottomSheetCallback(bottomSheetCallback)
        bottomSheetBehavior.removeBottomSheetCallback(transactionsBottomSheetAnimator)
        super.onDestroyView()
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
                //Change undo snack bar parent view to fragment's root
                val message = if (it.response.uiComponentType is UIComponentType.UndoSnackBar) {
                    it.copy(
                        response = it.response.copy(
                            uiComponentType = UIComponentType.UndoSnackBar(
                                callback = it.response.uiComponentType.callback,
                                parentView = binding.fragmentTransacionRoot
                            )
                        )
                    )
                } else {
                    it
                }
                handleNewStateMessage(message) { viewModel.clearStateMessage() }
            }
        }
    }

    override fun onClickedOnTransaction(position: Int, item: TransactionDto) {
        navigateToDetailTransactionFragment(item.id)
    }

    private fun navigateToDetailTransactionFragment(id: Int) {
        val action =
            TransactionsFragmentDirections.actionTransactionFragmentToDetailEditTransactionFragment(
                transactionId = id
            )
        findNavController().navigate(action)
    }

    override fun restoreListPosition() {}

    override fun onClickOnNavigation(view: View) {
        binding.drawerLayout.open()
    }

    override fun onClickOnMenuButton(view: View) {}

    override fun onClickOnMonthName(view: View) {
        viewModel.showMonthPickerBottomSheet(parentFragmentManager)
    }

    override fun onClickOnPreviousMonthButton(view: View) {
        viewModel.navigateToPreviousMonth()
    }

    override fun onClickOnNextMonthButton(view: View) {
        viewModel.navigateToNextMonth()
    }

}