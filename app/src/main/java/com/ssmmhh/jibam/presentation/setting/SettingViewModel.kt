package com.ssmmhh.jibam.presentation.setting

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import java.util.*
import javax.inject.Inject

class SettingViewModel
@Inject
constructor(
    private val locale: Locale,
    private val sharedPreferences: SharedPreferences,
    private val sharedPrefEditor: SharedPreferences.Editor
) : ViewModel() {

}