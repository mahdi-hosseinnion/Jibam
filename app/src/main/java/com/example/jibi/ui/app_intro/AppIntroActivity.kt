package com.example.jibi.ui.app_intro

import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.jibi.BaseApplication
import com.example.jibi.R
import com.example.jibi.util.PreferenceKeys
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
class AppIntroActivity : AppIntro() {

    @Inject
    lateinit var sharedPrefsEditor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        super.onCreate(savedInstanceState)
        // Call addSlide passing your Fragments.
        // You can use AppIntroFragment to use a pre-built fragment
        addSlide(
            AppIntroFragment.newInstance(
                title = "Welcome...",
                description = "This is the first slide of the example",
                imageDrawable = R.drawable.ic_cat_dried_fruits,
                backgroundDrawable = R.drawable.ic_cat_awards,
                titleColor = Color.YELLOW,
                descriptionColor = Color.RED,
                backgroundColor = Color.BLUE
            )
        )
        addSlide(
            AppIntroFragment.newInstance(
                title = "...Let's get started!",
                description = "This is the last slide, I won't annoy you more :)",
                imageDrawable = R.drawable.ic_cat_clothing,
                backgroundDrawable = R.drawable.ic_cat_car,
                titleColor = Color.YELLOW,
                descriptionColor = Color.RED,
                backgroundColor = Color.BLUE
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
}

