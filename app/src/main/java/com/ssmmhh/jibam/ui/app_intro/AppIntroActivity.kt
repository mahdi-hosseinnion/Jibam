package com.ssmmhh.jibam.ui.app_intro

import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.ssmmhh.jibam.BaseApplication
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.util.PreferenceKeys
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
class AppIntroActivity : AppIntro() {

    private val TAG = "AppIntroActivity"

    @Inject
    lateinit var sharedPrefsEditor: SharedPreferences.Editor

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var _resources: Resources


    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        super.onCreate(savedInstanceState)
        // Call addSlide passing your Fragments.
        // You can use AppIntroFragment to use a pre-built fragment
        this.setSkipText(_getString(R.string.skip))
        this.setDoneText(_getString(R.string.done))

        addSlide(
            AppIntroFragment.newInstance(
                title = _getString(R.string.app_intro_slide_0_title),
                description = _getString(R.string.app_intro_slide_0_description),
                imageDrawable = R.drawable.ic_app_logo_svg,
                backgroundDrawable = R.drawable.app_intro_back_slide_5,
            )
        )
        addSlide(
            AppIntroFragment.newInstance(
                title = _getString(R.string.app_intro_slide_1_title),
                description = _getString(R.string.app_intro_slide_1_description),
                imageDrawable = R.drawable.ic_app_intro_transaction_list,
                backgroundDrawable = R.drawable.app_intro_back_slide_3,
            )
        )
        addSlide(
            AppIntroFragment.newInstance(
                title = _getString(R.string.app_intro_slide_2_title),
                description = _getString(R.string.app_intro_slide_2_description),
                imageDrawable = R.drawable.ic_app_intro_graph,
                backgroundDrawable = R.drawable.app_intro_back_slide_2,
            )
        )
    }

    private fun inject() {
        (application as BaseApplication).mainComponent()
            .inject(this)
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)

        setIsFirstRunToFalse()
        finish()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)

        setIsFirstRunToFalse()
        finish()
    }

    private fun setIsFirstRunToFalse() {
        sharedPrefsEditor.putBoolean(
            PreferenceKeys.APP_INTRO_PREFERENCE,
            false
        ).apply()
    }

    private fun _getString(@StringRes resId: Int): String {
        return try {
            _resources.getString(resId)
        } catch (e: Exception) {
            Log.e(TAG, "_getString: resourceId: $resId & _resources: $_resources ", e)
            getString(resId)
        }
    }
}

