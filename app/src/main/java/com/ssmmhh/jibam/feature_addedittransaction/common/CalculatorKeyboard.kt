package com.ssmmhh.jibam.feature_addedittransaction.common

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.LinearLayout
import androidx.annotation.StringRes
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.util.NUMBER_SEPARATOR
import com.ssmmhh.jibam.util.localizeNumber
import com.ssmmhh.jibam.util.remove3By3Separators

//copy from this
// https://stackoverflow.com/a/45005691/10362460
//and
//http://www.fampennings.nl/maarten/android/09keyboard/index.htm
//TODO add slide up animation
//https://stackoverflow.com/a/46644736/10362460
class CalculatorKeyboard(
    context: Context,
    attributeSet: AttributeSet? = null
//    defStyleAttr: Int = 0
) : LinearLayout(
    context,
    attributeSet
//    defStyleAttr
), View.OnClickListener {

    var text = StringBuilder("")

    var calculatorInteraction: CalculatorInteraction? = null

    var _resources: Resources? = null

    private fun _getString(@StringRes resId: Int): String {
        return _resources?.getString(resId) ?: resources.getString(resId)
    }

    private fun listOfNumbers(): CharArray =
        charArrayOf(
            _getString(R.string._1)[0],
            _getString(R.string._2)[0],
            _getString(R.string._3)[0],
            _getString(R.string._4)[0],
            _getString(R.string._5)[0],
            _getString(R.string._6)[0],
            _getString(R.string._7)[0],
            _getString(R.string._8)[0],
            _getString(R.string._9)[0]
        )


    // keyboard keys (buttons)
    private val mButton1: Button
    private val mButton2: Button
    private val mButton3: Button
    private val mButton4: Button
    private val mButton5: Button
    private val mButton6: Button
    private val mButton7: Button
    private val mButton8: Button
    private val mButton9: Button
    private val mButton0: Button
    private val mButton00: Button

    private val mButtonClear: Button
    private val mButtonClearAll: Button
    private val mButtonDivision: Button
    private val mButtonTimes: Button
    private val mButtonMines: Button
    private val mButtonPlus: Button
    private val mButtonPeriod: Button
    private val mButtonEqual: Button

    // This will map the button resource id to the String value that we want to
    // input when that button is clicked.
    private var keyValues: Map<Int, String>

    // Our communication link to the EditText
    var inputConnection: InputConnection? = null

    // The activity (or some parent or controller) must give us
    // a reference to the current EditText's InputConnection
    //NO NEED FOR SETTER
    init {
        //initialize buttons
        LayoutInflater.from(context).inflate(R.layout.keyboard_add_transaction, this, true)
        //not number
        mButtonClear = findViewById(R.id.btn_c)
        mButtonClearAll = findViewById(R.id.btn_clearAll)
        mButtonDivision = findViewById(R.id.btn_division)
        mButtonTimes = findViewById(R.id.btn_times)
        mButtonMines = findViewById(R.id.btn_mines)
        mButtonPlus = findViewById(R.id.btn_plus)
        mButtonPeriod = findViewById(R.id.btn_period)
        mButtonEqual = findViewById(R.id.btn_equal)
        //number buttons
        mButton1 = findViewById(R.id.btn_1)
        mButton2 = findViewById(R.id.btn_2)
        mButton3 = findViewById(R.id.btn_3)
        mButton4 = findViewById(R.id.btn_4)
        mButton5 = findViewById(R.id.btn_5)
        mButton6 = findViewById(R.id.btn_6)
        mButton7 = findViewById(R.id.btn_7)
        mButton8 = findViewById(R.id.btn_8)
        mButton9 = findViewById(R.id.btn_9)
        mButton0 = findViewById(R.id.btn_0)
        mButton00 = findViewById(R.id.btn_00)
        //onclick
        mButton1.setOnClickListener(this)
        mButton2.setOnClickListener(this)
        mButton3.setOnClickListener(this)
        mButton4.setOnClickListener(this)
        mButton5.setOnClickListener(this)
        mButton6.setOnClickListener(this)
        mButton7.setOnClickListener(this)
        mButton8.setOnClickListener(this)
        mButton9.setOnClickListener(this)
        mButton0.setOnClickListener(this)
        mButton00.setOnClickListener(this)

        mButtonClear.setOnClickListener(this)
        mButtonClearAll.setOnClickListener(this)
        mButtonDivision.setOnClickListener(this)
        mButtonTimes.setOnClickListener(this)
        mButtonMines.setOnClickListener(this)
        mButtonPlus.setOnClickListener(this)
        mButtonPeriod.setOnClickListener(this)
        mButtonEqual.setOnClickListener(this)

        keyValues = mapOf()

    }

    fun setTextToAllViews() {
        //text
        mButton1.text = _getString(R.string._1)
        mButton2.text = _getString(R.string._2)
        mButton3.text = _getString(R.string._3)
        mButton4.text = _getString(R.string._4)
        mButton5.text = _getString(R.string._5)
        mButton6.text = _getString(R.string._6)
        mButton7.text = _getString(R.string._7)
        mButton8.text = _getString(R.string._8)
        mButton9.text = _getString(R.string._9)
        mButton0.text = _getString(R.string._0)
        mButton00.text = _getString(R.string._00)
        // map buttons IDs to input strings
        keyValues = mapOf(
            R.id.btn_c to CLEAR,
            R.id.btn_clearAll to CLEAR_ALL,
            R.id.btn_division to DIVISION,
            R.id.btn_times to TIMES,
            R.id.btn_mines to MINES,
            R.id.btn_plus to PLUS,
            R.id.btn_period to PERIOD,
            R.id.btn_equal to "=",
            R.id.btn_1 to _getString(R.string._1),
            R.id.btn_2 to _getString(R.string._2),
            R.id.btn_3 to _getString(R.string._3),
            R.id.btn_4 to _getString(R.string._4),
            R.id.btn_5 to _getString(R.string._5),
            R.id.btn_6 to _getString(R.string._6),
            R.id.btn_7 to _getString(R.string._7),
            R.id.btn_8 to _getString(R.string._8),
            R.id.btn_9 to _getString(R.string._9),
            R.id.btn_0 to _getString(R.string._0),
            R.id.btn_00 to _getString(R.string._00)
        )
    }

    companion object {
        private const val TAG = "CalculatorKeyboard"
        private const val CLEAR = "CLEAR"
        private const val CLEAR_ALL = "CLEAR_ALL"
        const val TIMES = "×"
        const val DIVISION = "÷"
        const val PLUS = "+"
        const val MINES = "-"
        const val PERIOD = "."

        val listOfSigns = listOf(
            DIVISION,
            TIMES,
            MINES,
            PLUS,
            PERIOD
        )
        val listOfOperationsSigns = listOf(
            DIVISION,
            TIMES,
            MINES,
            PLUS,
        )
    }

    //TODO BUG CANNOT RESULV WHEN CORSUR MOVE
    // TODO this method need real refactoring
    override fun onClick(v: View?) {
        // do nothing if the InputConnection has not been set yet
        if (inputConnection == null || v == null) {
            Log.e(TAG, "onClick: inputConnection: $inputConnection & v: $v")
            return
        }
        if (v.id == R.id.btn_equal) {
            calculatorInteraction?.onEqualClicked()
            return
        }
        // Delete text or input key value
        // All communication goes through the InputConnection
        if (v.id == R.id.btn_c) {
            if (text.isBlank()) {
                return
            }
            val selectedText = inputConnection!!.getSelectedText(0)
            if (selectedText.isNullOrBlank()) {
                // no selection, so delete previous character
                if (inputConnection!!.getTextBeforeCursor(1, 0)!![0] == NUMBER_SEPARATOR) {
                    //get text before Cursor if it's  'NUMBER_SEPARATOR' remove 2
                    //(remove NUMBER_SEPARATOR and text before number separator)
                    inputConnection!!.deleteSurroundingText(2, 0)
                } else {
                    inputConnection!!.deleteSurroundingText(1, 0)
                }
                //TODO REMOVE THIS IT BASICLY MAKE NO SENSE AND IT's pretty buggy
                text = text.removeLastIndex()

                printText()
            } else {
                // delete the selection
                val selectedText =
                    inputConnection!!.getSelectedText(InputConnection.GET_TEXT_WITH_STYLES)

                inputConnection!!.commitText("", 1)

                text = text.deleteString(selectedText.toString().remove3By3Separators())
                printText()
            }
        } else if (v.id == R.id.btn_clearAll) {
            //clear everything
            inputConnection!!.clearText()
            text.clear()

        } else {
            val value = keyValues[v.id]

            if (!value.isNullOrBlank()) {

                if (text.isBlank()) {
                    //nothing inserted yet
                    if (value.indexOfAny(listOfNumbers()) >= 0 || value == _getString(R.string._0)) {
                        //contain number
                        inputConnection!!.commitText(value, 1)
                        text.append(value)
                    }

                } else {

                    //TODO SHOULD SUPPORT CURSOR CHANGE IF ADD SING IN DIFFERENT PLACE
                    //user should not be able to insert two math sign in row
                    if (text.substring(text.lastIndex)
                            .lastIndexOfAny(listOfSigns) >= 0 //last index of current text is math sign
                        &&
                        value.lastIndexOfAny(listOfSigns) >= 0 //value is math sign too
                    ) {
                        if (text.substring(text.lastIndex) != value) { //if its different sign
                            // delete the last one aka->0
                            inputConnection!!.deleteSurroundingText(1, 0)
                            text = text.removeLastIndex()

                            text.append(value)
                            inputConnection!!.commitText(value, 1)

                        }
                        return
                    }

                    //something inserted
                    if (text.toString() == _getString(R.string._0)) {
                        if (v.id == R.id.btn_period) {
                            //only . allowed after 0
                            inputConnection!!.commitText(value, 1)
                            text.append(value)
                        } else {
                            //if it's number (not 0) remove 0
                            if (value.indexOfAny(listOfNumbers()) >= 0) {
                                // delete the last one aka->0
                                inputConnection!!.deleteSurroundingText(1, 0)
                                inputConnection!!.commitText(value, 1)
                                text = StringBuilder(value)
                            }
                        }
                    } else {
                        inputConnection!!.commitText(value, 1)
                        text.append(value)
                    }
                }
            }
        }
    }

    fun preloadKeyboard(value: String) {
        try {
            //remove scientific notion like 'E' in double
            val bigDecimal = value.toBigDecimalOrNull()?.toPlainString() ?: return
            val localizedValue = bigDecimal.localizeNumber(resources)
            //clear last text
            inputConnection!!.clearText()
            text.clear()
            //add current text
            inputConnection!!.commitText(localizedValue, 1)
            text.append(bigDecimal)
        } catch (e: NullPointerException) {
            Log.e(TAG, "preloadKeyboard : cannot set text to inputConnection", e)
        } catch (e: Exception) {
            Log.e(TAG, "preloadKeyboard : cannot set text to inputConnection", e)
        }
    }

    private fun InputConnection.clearText() {
        val currentText = getExtractedText(ExtractedTextRequest(), 0).text;
        val beforeCursorText = getTextBeforeCursor(currentText.length, 0)
        val afterCursorText = getTextAfterCursor(currentText.length, 0)
        deleteSurroundingText(beforeCursorText!!.length, afterCursorText!!.length)
//        CharSequence currentText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0).text;
//        CharSequence beforCursorText = inputConnection.getTextBeforeCursor(currentText.length(), 0);
//        CharSequence afterCursorText = inputConnection.getTextAfterCursor(currentText.length(), 0);
//        inputConnection.deleteSurroundingText(beforCursorText.length(), afterCursorText.length());
    }

    private fun StringBuilder.removeLastIndex(): StringBuilder =
        StringBuilder(this.substring(0, text.lastIndex))

    private fun StringBuilder.deleteString(s: String): StringBuilder {
        val indexOfSelectedText = this.indexOf(s)
        return StringBuilder(
            this.removeRange(
                indexOfSelectedText,
                indexOfSelectedText.plus(s.length)
            )
        )
    }

    private fun printText() {
        Log.d(TAG, "onClick: text: -${text.toString()}-")
    }

    interface CalculatorInteraction {
        fun onEqualClicked()
    }
}