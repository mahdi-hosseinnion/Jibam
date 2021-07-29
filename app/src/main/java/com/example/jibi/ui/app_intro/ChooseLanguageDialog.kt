package com.example.jibi.ui.app_intro

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import com.example.jibi.R
import com.example.jibi.ui.main.transaction.common.MonthPickerBottomSheet
import kotlinx.android.synthetic.main.choose_language_dialog.*

class ChooseLanguageDialog
constructor(
    context: Context,
    private val interaction: Interaction,
    private var selectedLanguage: LANGUAGE
) : Dialog(context), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_language_dialog)

        if (selectedLanguage == LANGUAGE.PERSIAN) {
            txt_persian_lang.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_check_green_24dp,
                0
            )
            txt_english_lang.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)

        } else if (selectedLanguage == LANGUAGE.ENGLISH) {
            txt_english_lang.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_check_green_24dp,
                0
            )
            txt_persian_lang.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }
        txt_persian_lang.setOnClickListener {
            selectedLanguage = LANGUAGE.PERSIAN
            txt_persian_lang.setCompoundDrawablesWithIntrinsicBounds(
               0,
                0,
                R.drawable.ic_check_green_24dp,
                0
            )
            txt_english_lang.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)

        }
        txt_english_lang.setOnClickListener {
            selectedLanguage = LANGUAGE.ENGLISH
            txt_english_lang.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_check_green_24dp,
                0
            )
            txt_persian_lang.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }
        btn_dialog_ok.setOnClickListener {
            interaction.onOkClicked(selectedLanguage)
            dismiss()
        }
    }

    override fun onClick(p0: View?) {

    }

    interface Interaction {
        fun onOkClicked(language: LANGUAGE)
    }

    enum class LANGUAGE {
        PERSIAN, ENGLISH
    }
}