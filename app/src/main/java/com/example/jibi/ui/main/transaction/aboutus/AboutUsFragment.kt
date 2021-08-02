package com.example.jibi.ui.main.transaction.aboutus

import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.jibi.R
import com.example.jibi.ui.main.transaction.common.BaseFragment
import com.example.jibi.util.localizeNumber
import kotlinx.android.synthetic.main.fragment_about_us.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject


@ExperimentalCoroutinesApi
@FlowPreview
class AboutUsFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager,
    private val _resources: Resources
) : BaseFragment(
    R.layout.fragment_about_us,
    viewModelFactory,
    R.id.about_us_toolbar,
    _resources
) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setBackground()
    }

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

    override fun setTextToAllViews() {
        findNavController()
            .currentDestination?.label = _getString(R.string.about)
        developement_members.text = _getString(R.string.developement_members)
        about_app.text = _getString(R.string.about_app)
        developement_title.text = _getString(R.string.developement_title)
        version_name.text = getVersionName()?.let {
            _getString(R.string.version) + ": ${it.localizeNumber(_resources)}"
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