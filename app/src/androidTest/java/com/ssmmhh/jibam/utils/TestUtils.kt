package com.ssmmhh.jibam.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.ssmmhh.jibam.TestBaseApplication
import com.ssmmhh.jibam.util.PreferenceKeys
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
fun getTestBaseApplication(): TestBaseApplication = ApplicationProvider.getApplicationContext()
