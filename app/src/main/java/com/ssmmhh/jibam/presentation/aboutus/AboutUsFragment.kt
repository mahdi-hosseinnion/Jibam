package com.ssmmhh.jibam.presentation.aboutus

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.databinding.FragmentAboutUsBinding
import com.ssmmhh.jibam.presentation.common.BaseFragment
import com.ssmmhh.jibam.presentation.util.ToolbarLayoutListener
import com.ssmmhh.jibam.util.localizeNumber
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview


@ExperimentalCoroutinesApi
@FlowPreview
class AboutUsFragment() : BaseFragment(), ToolbarLayoutListener {

    private lateinit var binding: FragmentAboutUsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAboutUsBinding.inflate(inflater, container, false).apply {
            listener = this@AboutUsFragment
            versionName = this@AboutUsFragment.getVersionName(this@AboutUsFragment.requireContext())
                //TODO("Use a different method to localize version number
                .localizeNumber(resources)
        }
        return binding.root
    }

    override fun handleStateMessages() {}

    override fun handleLoading() {}

    private fun getVersionName(context: Context): String {
        return try {
            context.run {
                packageManager.getPackageInfo(packageName, 0).versionName
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            getString(R.string.unknown)
        }
    }

    override fun onClickOnNavigation(view: View) {
        navigateBack()
    }

    override fun onClickOnMenuButton(view: View) {}

}