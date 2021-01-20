package com.example.jibi.ui

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.LinearLayout
import com.example.jibi.R

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
    private val mButtonPercent: Button
    private val mButtonDivision: Button
    private val mButtonTimes: Button
    private val mButtonMines: Button
    private val mButtonPlus: Button
    private val mButtonPeriod: Button
    private val mButtonEqual: Button

    // This will map the button resource id to the String value that we want to
    // input when that button is clicked.
    private val keyValues: Map<Int, String>

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
        mButtonClearAll = findViewById(R.id.btn_ac)
        mButtonPercent = findViewById(R.id.btn_percent)
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
        mButtonPercent.setOnClickListener(this)
        mButtonDivision.setOnClickListener(this)
        mButtonTimes.setOnClickListener(this)
        mButtonMines.setOnClickListener(this)
        mButtonPlus.setOnClickListener(this)
        mButtonPeriod.setOnClickListener(this)
        mButtonEqual.setOnClickListener(this)

        // map buttons IDs to input strings
        keyValues = mapOf(
            R.id.btn_c to CLEAR,
            R.id.btn_ac to CLEAR_ALL,
            R.id.btn_percent to "%",
            R.id.btn_division to "/",
            R.id.btn_times to "*",
            R.id.btn_mines to "-",
            R.id.btn_plus to "+",
            R.id.btn_period to ".",
            R.id.btn_equal to "=",
            R.id.btn_1 to "1",
            R.id.btn_2 to "2",
            R.id.btn_3 to "3",
            R.id.btn_4 to "4",
            R.id.btn_5 to "5",
            R.id.btn_6 to "6",
            R.id.btn_7 to "7",
            R.id.btn_8 to "8",
            R.id.btn_9 to "9",
            R.id.btn_0 to "0",
            R.id.btn_00 to "00"
        )

    }

    companion object {
        private const val TAG = "CalculatorKeyboard"
        private const val CLEAR = "CLEAR"
        private const val CLEAR_ALL = "CLEAR_ALL"
    }

    override fun onClick(v: View?) {
        // do nothing if the InputConnection has not been set yet
        if (inputConnection == null || v == null) {
            Log.e(TAG, "onClick: inputConnection: $inputConnection & v: $v")
            return
        }

        // Delete text or input key value
        // All communication goes through the InputConnection
        if (v.id == R.id.btn_c) {
            val selectedText = inputConnection!!.getSelectedText(0)
            if (selectedText.isNullOrBlank()) {
                // no selection, so delete previous character
                inputConnection!!.deleteSurroundingText(1, 0)
            } else {
                // delete the selection
                inputConnection!!.commitText("", 1)
            }
        } else {
            val value = keyValues[v.id]
            inputConnection!!.commitText(value, 1)
        }
    }


}