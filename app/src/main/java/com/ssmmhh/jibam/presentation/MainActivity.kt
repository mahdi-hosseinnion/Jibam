package com.ssmmhh.jibam.presentation

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.google.android.material.snackbar.Snackbar
import com.ssmmhh.jibam.BaseApplication
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.data.util.*
import com.ssmmhh.jibam.databinding.ActivityMainBinding
import com.ssmmhh.jibam.di.factory.MainFragmentFactory
import com.ssmmhh.jibam.presentation.intro.AppIntroActivity
import com.ssmmhh.jibam.util.ActivityCommunicationListener
import com.ssmmhh.jibam.util.PreferenceKeys
import com.ssmmhh.jibam.util.displayToast
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity(), ActivityCommunicationListener {

    private val TAG = "MainActivity"

    @Inject
    lateinit var fragmentFactory: MainFragmentFactory

    @Inject
    lateinit var currentLocale: Locale

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private lateinit var navController: NavController

    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        setFragmentFactory()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initializeNavControllerAndAppBarConfiguration()
    }

    override fun onResume() {
        super.onResume()
        checkForAppIntro()
    }

    override fun hideSoftKeyboard() {
        val imm: InputMethodManager =
            this.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view: View? = this.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(this)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
    }

    /**
     * Used to add 'debounce like' functionality to progressBar
     * with help of coroutines it will filter out the progress that took less than
     * [transitionAnimTime] milliseconds.
     */
    var loadingJob: Job? = null
    override fun showProgressBar(isLoading: Boolean) {
        if (isLoading) {
            val transitionAnimTime = resources.getInteger(R.integer.transitionAnimationDuration)
            loadingJob?.cancel()
            loadingJob = null
            loadingJob = lifecycleScope.launch(Main) {
                delay(transitionAnimTime.toLong())
                ensureActive()
                binding.progressBar.visibility = View.VISIBLE
            }
        } else {
            loadingJob?.cancel()
            loadingJob = null
            binding.progressBar.visibility = View.INVISIBLE
        }
    }

    override fun onResponseReceived(
        response: Response,
        stateMessageCallback: StateMessageCallback
    ) {

        when (response.uiComponentType) {

            is UIComponentType.AreYouSureDialog -> {

                response.message?.let {
                    areYouSureDialog(
                        message = it,
                        callback = response.uiComponentType.callback,
                        stateMessageCallback = stateMessageCallback
                    )
                }
            }

            is UIComponentType.DiscardOrSaveDialog -> {

                response.message?.let {
                    discardOrSaveDialog(
                        message = it,
                        callback = response.uiComponentType.callback,
                        stateMessageCallback = stateMessageCallback
                    )
                }
            }

            is UIComponentType.Toast -> {
                response.message?.let {
                    displayToast(
                        message = it,
                        stateMessageCallback = stateMessageCallback
                    )
                }
            }

            is UIComponentType.Dialog -> {
                displayDialog(
                    response = response,
                    stateMessageCallback = stateMessageCallback
                )
            }

            is UIComponentType.UndoSnackBar -> {
                displayUndoSnackBar(
                    message = response.message,
                    undoCallback = response.uiComponentType.callback,
                    parentView = response.uiComponentType.parentView,
                    stateMessageCallback = stateMessageCallback

                )
            }

            is UIComponentType.None -> {
                // This would be a good place to send to your Error Reporting
                // software of choice (ex: Firebase crash reporting)
                Log.i(TAG, "onResponseReceived: ${response.message}")
                stateMessageCallback.removeMessageFromStack()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        hideSoftKeyboard()
        return navController.navigateUp(appBarConfiguration) ||
                super.onSupportNavigateUp()
    }

    private fun inject() {
        (application as BaseApplication).appComponent.inject(this)
    }

    private fun setFragmentFactory() {
        supportFragmentManager.fragmentFactory = fragmentFactory
    }

    private fun initializeNavControllerAndAppBarConfiguration() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(setOf(R.id.transactionFragment))

    }

    private fun checkForAppIntro() {
        val isFirstRun = sharedPreferences.getBoolean(
            PreferenceKeys.APP_INTRO_PREFERENCE,
            true
        )
        if (isFirstRun) {
            val intent = Intent(this, AppIntroActivity::class.java)
            startActivity(intent)
        }

    }

    private fun displayDialog(
        response: Response,
        stateMessageCallback: StateMessageCallback
    ) {
        Log.d(TAG, "displayDialog: ")
        response.message?.let { message ->

            when (response.messageType) {

                is MessageType.Error -> {
                    displayErrorDialog(
                        message = message,
                        stateMessageCallback = stateMessageCallback
                    )
                }

                is MessageType.Success -> {
                    displaySuccessDialog(
                        message = message,
                        stateMessageCallback = stateMessageCallback
                    )
                }

                is MessageType.Info -> {
                    displayInfoDialog(
                        message = message,
                        stateMessageCallback = stateMessageCallback
                    )
                }

                else -> {
                    // do nothing
                    stateMessageCallback.removeMessageFromStack()
                    null
                }
            }
        } ?: stateMessageCallback.removeMessageFromStack()
    }

    private fun displaySuccessDialog(
        message: String?,
        stateMessageCallback: StateMessageCallback
    ) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.text_success))
            .setMessage(message)
            .setPositiveButton(getString(R.string.text_ok)) { dialog, id ->
                stateMessageCallback.removeMessageFromStack()
            }
            .setCancelable(false)
            .create()
            .show()
    }

    private fun displayErrorDialog(
        message: String?,
        stateMessageCallback: StateMessageCallback
    ) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.text_error))
            .setMessage(message)
            .setPositiveButton(getString(R.string.text_ok)) { dialog, id ->
                stateMessageCallback.removeMessageFromStack()
            }
            .setCancelable(false)
            .create()
            .show()
    }

    private fun displayInfoDialog(
        message: String?,
        stateMessageCallback: StateMessageCallback
    ) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.text_info))
            .setMessage(message)
            .setPositiveButton(getString(R.string.text_ok)) { dialog, id ->
                stateMessageCallback.removeMessageFromStack()
            }
            .setCancelable(false)
            .create()
            .show()
    }

    private fun areYouSureDialog(
        message: String,
        callback: AreYouSureCallback,
        stateMessageCallback: StateMessageCallback
    ) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.are_you_sure))
            .setMessage(message)
            .setPositiveButton(getString(R.string.text_yes)) { dialog, id ->
                callback.proceed()
                stateMessageCallback.removeMessageFromStack()
            }
            .setNegativeButton(getString(R.string.text_cancel)) { _, _ ->
                callback.cancel()
                stateMessageCallback.removeMessageFromStack()
            }
            .setCancelable(false)
            .create()
            .show()
    }

    private fun discardOrSaveDialog(
        message: String,
        callback: DiscardOrSaveCallback,
        stateMessageCallback: StateMessageCallback
    ) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                callback.save()
                stateMessageCallback.removeMessageFromStack()
            }
            .setNegativeButton(getString(R.string.discard)) { _, _ ->
                callback.discard()
                stateMessageCallback.removeMessageFromStack()
            }
            .setCancelable(true)
            .setOnCancelListener {
                callback.cancel()
                stateMessageCallback.removeMessageFromStack()
            }
            .create()
            .show()
    }

    private fun displayUndoSnackBar(
        message: String?,
        undoCallback: UndoCallback,
        stateMessageCallback: StateMessageCallback,
        parentView: View
    ) {
        val snackbar: Snackbar = Snackbar.make(
            parentView, message ?: "No Message",
            Snackbar.LENGTH_LONG
        )
        snackbar.setAction(getString(R.string.snack_bar_undo)) { v ->
            undoCallback.undo()

        }
        snackbar.addCallback(object : Snackbar.Callback() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                super.onDismissed(transientBottomBar, event)
                undoCallback.onDismiss()
            }

        })
        snackbar.show()
        stateMessageCallback.removeMessageFromStack()
    }
}