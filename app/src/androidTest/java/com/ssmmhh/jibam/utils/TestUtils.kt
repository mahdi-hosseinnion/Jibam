package com.ssmmhh.jibam.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.test.platform.app.InstrumentationRegistry
import com.ssmmhh.jibam.util.PreferenceKeys


fun getAppContext(): Context =
    InstrumentationRegistry.getInstrumentation().targetContext

fun getSharedPreferences(): SharedPreferences =
    getAppContext()
        .getSharedPreferences(
            PreferenceKeys.APP_MAIN_PREFERENCES,
            Context.MODE_PRIVATE
        )


fun getSharedPreferencesEditor(): SharedPreferences.Editor =
    getSharedPreferences().edit()
