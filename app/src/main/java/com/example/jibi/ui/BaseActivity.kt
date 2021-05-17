package com.example.jibi.ui

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.example.jibi.BaseApplication
import com.example.jibi.R
import com.example.jibi.util.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject


abstract class BaseActivity : AppCompatActivity(),
    UICommunicationListener {

    private val TAG = "BaseActivity"

    private var dialogInView: MaterialDialog? = null


    abstract fun inject()

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        (application as BaseApplication).appComponent
            .inject(this)
        super.onCreate(savedInstanceState)

        val appLanguage = sharedPreferences.getString(
            PreferenceKeys.APP_LANGUAGE_PREF,
            Constants.PERSIAN_LANG_CODE
        )

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (appLanguage == Constants.PERSIAN_LANG_CODE) {
                window.decorView.layoutDirection = View.LAYOUT_DIRECTION_RTL
            }
            if (appLanguage == Constants.ENGLISH_LANG_CODE) {
                window.decorView.layoutDirection = View.LAYOUT_DIRECTION_LTR
            }
        }
    }

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var _resources: Resources

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    private fun _getString(@StringRes resId: Int): String {
        return _resources.getString(resId)
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

    private fun displayDialog(
        response: Response,
        stateMessageCallback: StateMessageCallback
    ) {
        Log.d(TAG, "displayDialog: ")
        response.message?.let { message ->

            dialogInView = when (response.messageType) {

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
    ): MaterialDialog {
        return MaterialDialog(this)
            .show {
                title = _getString(R.string.text_success)
                message(text = message)
                positiveButton(text = _getString(R.string.text_ok)) {
                    stateMessageCallback.removeMessageFromStack()
                    dismiss()
                }
                onDismiss {
                    dialogInView = null
                }
                cancelable(false)
            }
    }

    private fun displayErrorDialog(
        message: String?,
        stateMessageCallback: StateMessageCallback
    ): MaterialDialog {
        return MaterialDialog(this)
            .show {
                title = _getString(R.string.text_error)
                message(text = message)
                positiveButton(text = _getString(R.string.text_ok)) {
                    stateMessageCallback.removeMessageFromStack()
                    dismiss()
                }
                onDismiss {
                    dialogInView = null
                }
                cancelable(false)
            }
    }

    private fun displayInfoDialog(
        message: String?,
        stateMessageCallback: StateMessageCallback
    ): MaterialDialog {
        return MaterialDialog(this)
            .show {
                title = _getString(R.string.text_info)
                message(text = message)
                positiveButton(text = _getString(R.string.text_ok)) {
                    stateMessageCallback.removeMessageFromStack()
                    dismiss()
                }
                onDismiss {
                    dialogInView = null
                }
                cancelable(false)
            }
    }

    private fun areYouSureDialog(
        message: String,
        callback: AreYouSureCallback,
        stateMessageCallback: StateMessageCallback
    ): MaterialDialog {
        return MaterialDialog(this)
            .show {
                title = _getString(R.string.are_you_sure)
                message(text = message)
                negativeButton(text = _getString(R.string.text_cancel)) {
                    callback.cancel()
                    stateMessageCallback.removeMessageFromStack()
                    dismiss()
                }
                positiveButton(text = _getString(R.string.text_yes)) {
                    callback.proceed()
                    stateMessageCallback.removeMessageFromStack()
                    dismiss()
                }
                onDismiss {
                    dialogInView = null
                }
                cancelable(false)
            }
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
        snackbar.setAction(_getString(R.string.snack_bar_undo)) { v ->
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

    override fun onPause() {
        super.onPause()
        if (dialogInView != null) {
            (dialogInView as MaterialDialog).dismiss()
            dialogInView = null
        }
    }

}