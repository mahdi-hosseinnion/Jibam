package com.ssmmhh.jibam.ui.main.transaction.aboutus

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.ui.main.transaction.common.BaseFragment
import com.ssmmhh.jibam.util.localizeNumber
import kotlinx.android.synthetic.main.fragment_about_us.*
import kotlinx.android.synthetic.main.layout_toolbar_with_back_btn.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview


@ExperimentalCoroutinesApi
@FlowPreview
class AboutUsFragment(
    viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager
) : BaseFragment(
    R.layout.fragment_about_us
) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setBackground()
        initUi()
    }

    private fun initUi() {
        topAppBar_normal.title = getString(R.string.about)
        version_name.text = getVersionName()?.let {
            getString(R.string.version) + ": ${it.localizeNumber(resources)}"
        }
        topAppBar_normal.setNavigationOnClickListener {
            navigateBack()
        }
    }

    override fun handleStateMessages() {}

    override fun handleLoading() {}

    private fun setBackground() {
        try {
            requestManager.load(R.drawable.about_us)
                .into(
                    object : CustomTarget<Drawable>() {
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable>?
                        ) {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                about_us_root.background = resource
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}

                    }
                )
        } catch (e: Exception) {

        }
    }

    private fun getVersionName(): String? {
        try {
            val pInfo =
                requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            return pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return null
    }

}