package com.ssmmhh.jibam.feature_transactions

import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
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
import com.ssmmhh.jibam.databinding.FragmentTransactionBinding
import com.ssmmhh.jibam.data.model.Month
import com.ssmmhh.jibam.data.source.local.dto.TransactionDto
import com.ssmmhh.jibam.data.source.local.entity.TransactionEntity
import com.ssmmhh.jibam.data.source.repository.buildResponse
import com.ssmmhh.jibam.feature_common.BaseFragment
import com.ssmmhh.jibam.feature_transactions.state.TransactionsStateEvent
import com.ssmmhh.jibam.feature_transactions.state.TransactionsViewState
import com.ssmmhh.jibam.feature_transactions.TransactionsFragmentDirections
import com.ssmmhh.jibam.util.*
import com.ssmmhh.jibam.util.PreferenceKeys.APP_CALENDAR_PREFERENCE
import com.ssmmhh.jibam.util.PreferenceKeys.PROMOTE_FAB_TRANSACTION_FRAGMENT
import com.ssmmhh.jibam.util.PreferenceKeys.PROMOTE_MONTH_MANGER
import com.ssmmhh.jibam.util.PreferenceKeys.PROMOTE_SUMMERY_MONEY
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import uk.co.samuelwall.materialtaptargetprompt.extras.backgrounds.RectanglePromptBackground
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal
import java.math.BigDecimal
import java.util.*
import kotlin.random.Random

@FlowPreview
@ExperimentalCoroutinesApi
class TransactionsFragment(
    viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager,
    private val currentLocale: Locale,
    private val sharedPreferences: SharedPreferences,
    private val sharedPrefsEditor: SharedPreferences.Editor
) : BaseFragment(), TransactionsListAdapter.Interaction {

    private val TAG = "TransactionFragment"

    private val viewModel by viewModels<TransactionsViewModel> { viewModelFactory }


    private lateinit var recyclerAdapter: TransactionsListAdapter

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private var bottomSheetPeekHeight = 0

    private val closeBottomWidth by lazy { convertDpToPx(56) }
    private val bottomSheetRadios by lazy { convertDpToPx(16) }
    private val normalAppBarHeight by lazy { convertDpToPx(40) }

    private var _binding: FragmentTransactionBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTransactionBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            onBottomSheetStateChanged(newState)
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            onBottomSheetSlide(slideOffset)
        }
    }
    private val backStackForBottomSheet = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            //check for search view
            if (viewModel.isSearchVisible()) {
                disableSearchMode()
            } else {
                binding.transactionRecyclerView.scrollToPosition(0)
                bottomSheetBehavior.state = STATE_COLLAPSED
                this.isEnabled = false
            }
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            backStackForBottomSheet
        )
        binding.transactionToolbar.topAppBarMonth.navigationIcon =
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.ic_round_menu_24,
                requireContext().theme
            )
        binding.transactionToolbar.topAppBarMonth.navigationContentDescription =
            getString(R.string.navigation_drawer_cd)
        binding.transactionToolbar.topAppBarMonth.setNavigationOnClickListener {
            binding.drawerLayout.open()
//            uiCommunicationListener.openDrawerMenu()
        }

        bottomSheetBehavior = BottomSheetBehavior.from(binding.mainStandardBottomSheet)

        bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
        onBottomSheetStateChanged(bottomSheetBehavior.state)
        setupNavigationView()
        initRecyclerView()
        subscribeObservers()

        savedInstanceState?.getString(SEARCH_QUERY)?.let { query ->
            //if user rotate the screen and fragment get rebuild
            Log.d(TAG, "onViewCreated:savedInstanceState withThisQuery: $query")
            if (query.isNotEmpty()) {
                enableSearchMode(query)
            }
        }

        if (viewModel.isSearchVisible()) {
            enableSearchMode()
        }

        binding.addFab.setOnClickListener {
            navigateToAddTransactionFragment()
        }
        //TODO DELETE THIS LINE FOR FINAL PROJECT JUST OF TESTING
        binding.txtBalance.setOnClickListener {
//            insertRandomTransaction()
        }
        //TODO DELETE THIS LINE FOR FINAL PROJECT JUST OF TESTING
        binding.txtExpenses.setOnClickListener {
            //TODD JUST FOR TESTING
//            resetPromoteState()
        }
        //TODO DELETE THIS LINE FOR FINAL PROJECT JUST OF TESTING
        binding.txtIncome.setOnClickListener {
//            showSummeryMoneyPromote()
        }
        binding.mainBottomSheetBackArrow.setOnClickListener {
            //check for search view
            if (viewModel.isSearchVisible()) {
                disableSearchMode()
            } else {
                binding.transactionRecyclerView.scrollToPosition(0)
                bottomSheetBehavior.state = STATE_COLLAPSED
            }
        }
        //search view stuff
        binding.mainBottomSheetSearchBtn.setOnClickListener {
            enableSearchMode()
        }
        binding.bottomSheetSearchClear.setOnClickListener {
            binding.bottomSheetSearchEdt.setText("")
        }
        binding.transactionToolbar.toolbarMonthChanger.toolbarMonth.setOnClickListener {
            viewModel.showMonthPickerBottomSheet(parentFragmentManager)
        }
        binding.transactionToolbar.toolbarMonthChanger.monthManagerPrevious.setOnClickListener {
            viewModel.navigateToPreviousMonth()
        }
        binding.transactionToolbar.toolbarMonthChanger.monthManagerNext.setOnClickListener {
            viewModel.navigateToNextMonth()
        }
        checkForGuidePromote()
    }

    private fun setupNavigationView() {
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

    private fun closeDrawer(animate: Boolean = true) {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START, animate)
        }
    }

    private fun resetPromoteState() {
        sharedPrefsEditor.putBoolean(
            PROMOTE_FAB_TRANSACTION_FRAGMENT,
            true
        ).apply()
        sharedPrefsEditor.putBoolean(
            PreferenceKeys.PROMOTE_ADD_TRANSACTION,
            true
        ).apply()
        sharedPrefsEditor.putBoolean(
            PreferenceKeys.PROMOTE_CATEGORY_LIST,
            true
        ).apply()
        sharedPrefsEditor.putBoolean(
            PreferenceKeys.PROMOTE_VIEW_CATEGORY_LIST,
            true
        ).apply()
        sharedPrefsEditor.putBoolean(
            PreferenceKeys.PROMOTE_ADD_CATEGORY_NAME,
            true
        ).apply()
        sharedPrefsEditor.putBoolean(
            PreferenceKeys.APP_INTRO_PREFERENCE,
            true
        ).apply()
        sharedPrefsEditor.putBoolean(
            PreferenceKeys.PROMOTE_SUMMERY_MONEY,
            true
        ).apply()
        sharedPrefsEditor.putBoolean(
            PreferenceKeys.PROMOTE_MONTH_MANGER,
            true
        ).apply()

        Toast.makeText(requireContext(), "PROMOTE GUIDE STATE RESET", Toast.LENGTH_SHORT)
            .show()
    }

    private fun enableSearchMode(query: String? = null) {
        bottomSheetBehavior.state = STATE_EXPANDED
        //user shouldn't be able to drag down when searchView is enable
        bottomSheetBehavior.isDraggable = false
        //disconnect recyclerView to bottomSheet
        binding.transactionRecyclerView.isNestedScrollingEnabled = false

        binding.bottomSheetSearchClear.visibility = View.INVISIBLE

        binding.bottomSheetSearchEdt.addTextChangedListener(onSearchViewTextChangeListener)
        //invisible search stuff
        binding.mainBottomSheetSearchBtn.visibility = View.GONE
        binding.bottomSheetTitle.visibility = View.GONE
        //this should come after all
        viewModel.setSearchViewState(TransactionsViewState.SearchViewState.VISIBLE)

        query?.let {
            binding.bottomSheetSearchEdt.setText(it)
        }
        if (bottomSheetBehavior.state == STATE_EXPANDED) {
            binding.bottomSheetSearchEdt.visibility = View.VISIBLE
            forceKeyBoardToOpenForMoneyEditText(binding.bottomSheetSearchEdt)
        }
    }

    private fun disableSearchMode() {

        activityCommunicationListener.hideSoftKeyboard()
        //invisible search stuff
        binding.bottomSheetSearchEdt.visibility = View.GONE
        binding.bottomSheetSearchClear.visibility = View.GONE

        binding.bottomSheetSearchEdt.removeTextChangedListener(onSearchViewTextChangeListener)
        binding.bottomSheetSearchEdt.setText("")
        //visible search stuff
        binding.mainBottomSheetSearchBtn.visibility = View.VISIBLE
        binding.bottomSheetTitle.visibility = View.VISIBLE

        //this should come after all
        viewModel.setSearchViewState(TransactionsViewState.SearchViewState.INVISIBLE)

        //make bottom sheet draggable again after disabling searchView
        bottomSheetBehavior.isDraggable = true
        //connect recyclerView to bottomSheet
        binding.transactionRecyclerView.isNestedScrollingEnabled = true

        //submit that
        viewModel.setSearchQuery("")
    }

    private val onSearchViewTextChangeListener = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            if (p0.isNullOrEmpty()) {
                binding.bottomSheetSearchClear.visibility = View.INVISIBLE
            } else {
                binding.bottomSheetSearchClear.visibility = View.VISIBLE
            }
            //search for something
        }

        override fun afterTextChanged(p0: Editable?) {
            viewModel.setSearchQuery(p0.toString())
        }
    }

    //JUST FOR TEST
    var startOfMonth = System.currentTimeMillis().div(1_000)
    var endOfMonth = System.currentTimeMillis().div(1_000)

    private fun insertRandomTransaction() {
        val categoryId = Random.nextInt(1, 42)
        var money = Random.nextInt(0, 1000).toBigDecimal()
        //it just hardcoded TODO CHANGE IT LATER
        //we mark money with "-" base of category id
        val dateRange = 2_592_000_000L//30 day to millisecond
        if (categoryId <= 30) {
            money = money.negate()
        }
        if (Random.nextBoolean()) {
            viewModel.launchNewJob(
                TransactionsStateEvent.InsertTransaction(
                    TransactionEntity(
                        id = 0,
                        money = money,
                        memo = null,
                        cat_id = categoryId,
                        date = Random.nextLong(
                            startOfMonth.toLong(),
                            endOfMonth.toLong()
                        )
                    )
                )
            )
        } else {
            viewModel.launchNewJob(
                TransactionsStateEvent.InsertTransaction(
                    TransactionEntity(
                        id = 0,
                        money = money,
                        memo = "memo ${Random.nextInt(451252)}",
                        cat_id = categoryId,
                        date = Random.nextLong(
                            ((System.currentTimeMillis()).minus(dateRange)).div(1_000),
                            ((System.currentTimeMillis())).div(1_000)
                        )
                    )
                )
            )
        }
    }


    private fun navigateToAddTransactionFragment() {
        //on category selected and bottomSheet hided
        val action =
            TransactionsFragmentDirections.actionTransactionFragmentToCreateTransactionFragment()
        findNavController().navigate(action)
    }

    override fun handleLoading() {
        viewModel.countOfActiveJobs.observe(
            viewLifecycleOwner
        ) {
            Log.d(TAG, "handleLoading: activeJob: ${viewModel.getAllActiveJobs()}")

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

        viewModel.viewState.observe(viewLifecycleOwner) { vs ->
            vs?.let { viewState ->
                viewState.currentMonth?.let { setMonthFieldsValues(it) }
            }
        }
        viewModel.transactions.observe(viewLifecycleOwner) { transactionList ->
            recyclerAdapter.submitList(transactionList)
        }
        viewModel.summeryMoney.observe(viewLifecycleOwner) { sm ->
            if (sm.inNotNull()) {
                checkForSummeryMoneyPromote()
            }
            binding.txtBalance.text = separate3By3AndRoundIt(sm.balance, currentLocale)

            if (sm.expenses != BigDecimal.ZERO) {
                binding.txtExpenses.text =
                    separate3By3AndRoundIt(sm.expenses.negate(), currentLocale)
            } else {
                binding.txtExpenses.text = separate3By3AndRoundIt(BigDecimal.ZERO, currentLocale)
            }
            binding.txtIncome.text = separate3By3AndRoundIt(sm.income, currentLocale)
        }

    }

    private fun setMonthFieldsValues(month: Month) {
        binding.transactionToolbar.toolbarMonthChanger.toolbarMonth.text = month.nameOfMonth
        startOfMonth = month.startOfMonth
        endOfMonth = month.endOfMonth
    }


    private fun initRecyclerView() {

        val onScrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (bottomSheetBehavior.state == STATE_EXPANDED) {
                    if (dy > 0) {
                        binding.addFab?.hide()
                    } else {
                        binding.addFab?.show()
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
                    if (viewModel.isSearchInVisible()) {
                        //while searching you should not be able to drag down
                        bottomSheetBehavior.isDraggable = true
                    }
                } else {
                    bottomSheetBehavior.isDraggable = false
                }
            }
        }
        binding.transactionRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@TransactionsFragment.context)
            recyclerAdapter = TransactionsListAdapter(
                requestManager,
                this@TransactionsFragment,
                currentLocale
            )
            addOnScrollListener(onScrollListener)

//            //swipe to delete
            val swipeHandler =
                object : SwipeToDeleteCallback(this@TransactionsFragment.requireContext()) {
                    override fun onSwiped(
                        viewHolder: RecyclerView.ViewHolder,
                        direction: Int
                    ) {

                        val adapter =
                            binding.transactionRecyclerView.adapter as TransactionsListAdapter
                        val deletedTrans =
                            adapter.getTransaction(viewHolder.adapterPosition) ?: return
                        //delete from list
                        val removedHeader = adapter.removeAt(viewHolder.adapterPosition)
                        //add to recently deleted
                        val recentlyDeletedHeader =
                            TransactionsViewState.RecentlyDeletedTransaction(
                                deletedTrans,
                                viewHolder.adapterPosition,
                                removedHeader
                            )
                        viewModel.setRecentlyDeletedTrans(
                            recentlyDeletedHeader
                        )
                        //delete from database
                        viewModel.launchNewJob(
                            TransactionsStateEvent.DeleteTransaction(
                                transactionId = deletedTrans.id,
                                showSuccessToast = false
                            )
                        )
                        //show snackBar
                        showUndoSnackBar()

                    }
                }

            val itemTouchHelper = ItemTouchHelper(swipeHandler)
            itemTouchHelper.attachToRecyclerView(binding.transactionRecyclerView)

            binding.transactionRecyclerView.isNestedScrollingEnabled = true
            adapter = recyclerAdapter
        }

    }

    private fun showUndoSnackBar() {
        val undoCallback = object : UndoCallback {
            override fun undo() {
                insertRecentlyDeletedTrans()
            }

            override fun onDismiss() {
                viewModel.setRecentlyDeletedTrans(null)
            }
        }
        activityCommunicationListener.onResponseReceived(
            buildResponse(
                getString(R.string.transaction_successfully_deleted),
                UIComponentType.UndoSnackBar(undoCallback, binding.fragmentTransacionRoot),
                MessageType.Info
            ), object : StateMessageCallback {
                override fun removeMessageFromStack() {
                }
            }
        )
    }

    private fun insertRecentlyDeletedTrans() {
        val recentlyDeletedFields =
            viewModel.getCurrentViewStateOrNew().recentlyDeletedFields
        recentlyDeletedFields?.recentlyDeletedTrans?.let {
            //insert to list
            recyclerAdapter.insertRemovedTransactionAt(
                it,
                recentlyDeletedFields.recentlyDeletedTransPosition,
                recentlyDeletedFields.recentlyDeletedHeader
            )
            //insert into database
            viewModel.launchNewJob(
                TransactionsStateEvent.InsertTransaction(
                    it.toTransactionEntity()
                )
            )
        } ?: viewModel.addToMessageStack(
            message = "Something went wrong \n cannot return deleted transaction back",
            uiComponentType = UIComponentType.Dialog,
            messageType = MessageType.Error
        )
        viewModel.setRecentlyDeletedTrans(null)

    }


    override fun onResume() {
        super.onResume()
        closeDrawer(false)

        //check for calendar type
        checkForCalendarTypeChange()
        //enable backStack listener when user  navigate to next fragment or rotate screen
        if (bottomSheetBehavior.state == STATE_EXPANDED || viewModel.isSearchVisible()) {
            backStackForBottomSheet.isEnabled = true
        }
        //set bottom sheet peek height
        if (bottomSheetBehavior.state == STATE_EXPANDED)
            resetIt(1f)
        else if (bottomSheetBehavior.state == STATE_COLLAPSED) {
            resetIt(0f)
        }

        binding.fragmentTransacionRoot.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                //TODO BIG BUG HERE
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    binding.fragmentTransacionRoot.viewTreeObserver.removeOnGlobalLayoutListener(
                        this
                    )
                }
                val rootHeight = binding.fragmentTransacionRoot.height
                val layoutHeight = binding.transactionFragmentView.height
                Log.d(
                    TAG,
                    "onGlobalLayout: rootHeight:$rootHeight and layoutHeight $layoutHeight "
                )
                if (bottomSheetPeekHeight < 1) {
                    bottomSheetPeekHeight = rootHeight - layoutHeight
                }
                bottomSheetBehavior.peekHeight = bottomSheetPeekHeight
            }

        })
        //reset it
//        onBottomSheetStateChanged(bottomSheetBehavior.state)
//        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
//            onBottomSheetSlide(0f)
//        }

//        bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)

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

    override fun restoreListPosition() {
    }

    private fun onBottomSheetStateChanged(newState: Int) {
        if (STATE_EXPANDED == newState) {
            if (viewModel.isSearchVisible()) {
                // If theres bug with search bar this line is dangerous and bugkhiz
                //we should use this here b/c when user click on search and bottom sheet is in collapse state
                // edit text will crash the appbar
                binding.bottomSheetSearchEdt.visibility = View.VISIBLE
                forceKeyBoardToOpenForMoneyEditText(binding.bottomSheetSearchEdt)
            }
            binding.lastTransacionAppBar.isLiftOnScroll = false
//            last_transacion_app_bar.liftOnScrollTargetViewId = R.id.transaction_recyclerView
            binding.lastTransacionAppBar.setLiftable(false)
            //enable backStack
            backStackForBottomSheet.isEnabled = true
        } else {
            binding.lastTransacionAppBar.isLiftOnScroll = true
            binding.lastTransacionAppBar.setLiftable(true)
            //disable backStack
            backStackForBottomSheet.isEnabled = false
        }

        if (STATE_DRAGGING == newState) {
            binding.addFab.hide()
        } else {
            binding.addFab.show()
        }

    }

    private var bottomSheetBackGround: GradientDrawable? = null

    private fun onBottomSheetSlide(slideOffset: Float) {
        binding.mainBottomSheetBackArrow.alpha = slideOffset

        bottomSheetBackGround =
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.bottom_sheet_bg,
                null
            ) as GradientDrawable

        val topHeight = if (slideOffset <= 1f)
            (bottomSheetRadios * (1f - slideOffset))
        else {
            0f
        }

        //change bottom sheet raidus
        bottomSheetBackGround?.cornerRadius = topHeight
        bottomSheetBackGround?.let {
            binding.mainStandardBottomSheet.background = it
            binding.lastTransacionAppBar.background = it
        }
        //change app bar

        //            last_transacion_app_bar.setPadding(topHeight.toInt(), 0, topHeight.toInt(), 0)
        //change app bar height
        val appbarViewParams = binding.lastTransacionAppBar.layoutParams
        appbarViewParams.height =
            (normalAppBarHeight + (bottomSheetRadios - topHeight.toInt()))
        binding.lastTransacionAppBar.layoutParams = appbarViewParams
        //change buttons height
        val buttonsViewParams = binding.mainBottomSheetSearchBtn.layoutParams
        buttonsViewParams.width =
            (normalAppBarHeight + (bottomSheetRadios - topHeight.toInt()))

        binding.mainBottomSheetSearchBtn.layoutParams = buttonsViewParams
//            main_standardBottomSheet.setPadding(0, topHeight.toInt(), 0, 0)
        //change top of bottomSheet height
        val viewParams = binding.viewHastam.layoutParams
        viewParams.height = topHeight.toInt()
        binding.viewHastam.layoutParams = viewParams
        binding.viewHastam2.alpha = (1f - slideOffset)

        // make the toolbar close button animation
        val closeButtonParams =
            binding.mainBottomSheetBackArrow.layoutParams as ViewGroup.LayoutParams
        closeButtonParams.width = (slideOffset * closeBottomWidth).toInt()
        binding.mainBottomSheetBackArrow.layoutParams = closeButtonParams
    }

    private fun resetIt(slideOffset: Float) {

        if (slideOffset == 1f) {//if its full screen then set it to liftable
            binding.lastTransacionAppBar.isLiftOnScroll = false
            binding.lastTransacionAppBar.setLiftable(false)
        }

        binding.mainBottomSheetBackArrow.alpha = slideOffset

        bottomSheetBackGround =
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.bottom_sheet_bg,
                null
            ) as GradientDrawable

        val topHeight = if (slideOffset <= 1f)
            (bottomSheetRadios * (1f - slideOffset))
        else {
            0f
        }

        //change bottom sheet raidus
        bottomSheetBackGround?.cornerRadius = topHeight
        bottomSheetBackGround?.let {
            binding.mainStandardBottomSheet.background = it
            binding.lastTransacionAppBar.background = it
        }
        //            last_transacion_app_bar.setPadding(topHeight.toInt(), 0, topHeight.toInt(), 0)
        //change app bar height
        val appbarViewParams = binding.lastTransacionAppBar.layoutParams
        appbarViewParams.height =
            (normalAppBarHeight + (bottomSheetRadios - topHeight.toInt()))
        binding.lastTransacionAppBar.layoutParams = appbarViewParams
        //change buttons height
        val buttonsViewParams = binding.mainBottomSheetSearchBtn.layoutParams
        buttonsViewParams.width =
            (normalAppBarHeight + (bottomSheetRadios - topHeight.toInt()))

        binding.mainBottomSheetSearchBtn.layoutParams = buttonsViewParams
//            main_standardBottomSheet.setPadding(0, topHeight.toInt(), 0, 0)
        //change top of bottomSheet height
        val viewParams = binding.viewHastam.layoutParams
        viewParams.height = topHeight.toInt()
        binding.viewHastam.layoutParams = viewParams
        binding.viewHastam2.alpha = (1f - slideOffset)

        // make the toolbar close button animation
        val closeButtonParams =
            binding.mainBottomSheetBackArrow.layoutParams as ViewGroup.LayoutParams
        closeButtonParams.width = (slideOffset * closeBottomWidth).toInt()
        binding.mainBottomSheetBackArrow.layoutParams = closeButtonParams
    }

    private fun forceKeyBoardToOpenForMoneyEditText(editText: EditText) {
        editText.requestFocus()
        val imm: InputMethodManager =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }
    //TODO SAVE SEARCH VIEW
/*    override fun onSaveInstanceState(outState: Bundle) {

        //save searchResult and search state for screen rotation
        val searchQuery =
            if (searchViewState.isVisible()) (bottom_sheet_search_edt.text.toString())
            else null

        outState.putString(SEARCH_QUERY, searchQuery)

        super.onSaveInstanceState(outState)
    }*/

    private fun checkForGuidePromote() {
        if (sharedPreferences.getBoolean(PROMOTE_FAB_TRANSACTION_FRAGMENT, true)) {
            trySafe { showFabPromote() }
        }
    }

    private fun checkForSummeryMoneyPromote() {
        if (!(sharedPreferences.getBoolean(PROMOTE_FAB_TRANSACTION_FRAGMENT, true))) {

            if (sharedPreferences.getBoolean(PROMOTE_SUMMERY_MONEY, true)) {
                trySafe { showSummeryMoneyPromote() }

            } else {

                if (sharedPreferences.getBoolean(PROMOTE_MONTH_MANGER, true)) {
                    trySafe { showMonthMangerPromote() }
                }
            }
        }
    }


    private fun showFabPromote() {
        val mFabPrompt = MaterialTapTargetPrompt.Builder(this)
            .setTarget(R.id.add_fab)
            .setPrimaryText(getString(R.string.fab_tap_target_primary))
            .setSecondaryText(getString(R.string.fab_tap_target_secondary))
            .setPromptStateChangeListener { _, state ->
                if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED || state == MaterialTapTargetPrompt.STATE_DISMISSING) {
                    sharedPrefsEditor.putBoolean(
                        PROMOTE_FAB_TRANSACTION_FRAGMENT,
                        false
                    ).apply()
                }
            }
            .create()
        mFabPrompt!!.show()
    }

    private fun showSummeryMoneyPromote() {
        val mFabPrompt = MaterialTapTargetPrompt.Builder(this)
            .setTarget(R.id.summery_money_root)
            .setPrimaryText(getString(R.string.summery_money_tap_target_primary))
            .setPromptBackground(RectanglePromptBackground().setCornerRadius(10f, 10f))
            .setPromptFocal(RectanglePromptFocal())
            .setPromptStateChangeListener { _, state ->
                if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED || state == MaterialTapTargetPrompt.STATE_DISMISSING) {
                    sharedPrefsEditor.putBoolean(
                        PROMOTE_SUMMERY_MONEY,
                        false
                    ).apply()
                    //check for month promote
                    if (sharedPreferences.getBoolean(PROMOTE_MONTH_MANGER, true)) {
                        trySafe { showMonthMangerPromote() }
                    }
                }
            }
            .create()
        mFabPrompt!!.show()
    }

    private fun showMonthMangerPromote() {
        val mFabPrompt = MaterialTapTargetPrompt.Builder(this)
            .setTarget(R.id.toolbar_month)
            .setPrimaryText(getString(R.string.month_manager_tap_target_primary))
            .setPromptStateChangeListener { _, state ->
                if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED || state == MaterialTapTargetPrompt.STATE_DISMISSING) {
                    sharedPrefsEditor.putBoolean(
                        PROMOTE_MONTH_MANGER,
                        false
                    ).apply()
                }
            }
            .create()
        mFabPrompt!!.show()
    }

    companion object {
        private const val SEARCH_QUERY = "SEARCHVIEWWSTATE VISIBLE >>>>"

    }

}