package com.ssmmhh.jibam

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

/**
 * we gonna use this class as our test runner instead of androidJUnit4Runner
 * and then i will use the TestBaseApplication. basically forcing it to use TestBaseApplication
 * instead of BaseApplication that we passed to the manifest
 */
@ExperimentalCoroutinesApi
@FlowPreview
class MockTestRunner : AndroidJUnitRunner() {

    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(cl, TestBaseApplication::class.java.name, context)
    }
}