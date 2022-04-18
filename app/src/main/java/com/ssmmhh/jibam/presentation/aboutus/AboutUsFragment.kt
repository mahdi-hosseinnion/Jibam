package com.ssmmhh.jibam.presentation.aboutus

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.databinding.FragmentAboutUsBinding
import com.ssmmhh.jibam.presentation.common.BaseFragment
import com.ssmmhh.jibam.util.localizeNumber
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview


@ExperimentalCoroutinesApi
@FlowPreview
class AboutUsFragment(
    viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager
) : BaseFragment() {

    private lateinit var binding: FragmentAboutUsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAboutUsBinding.inflate(inflater, container, false).apply {
            versionName = this@AboutUsFragment.getVersionName()
                //TODO("Use a different method to localize version number
                .localizeNumber(resources)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
    }

    private fun initUi() {
        binding.toolbar.topAppBarNormal.title = getString(R.string.about)
        binding.toolbar.topAppBarNormal.setNavigationOnClickListener {
            navigateBack()
        }
    }

    override fun handleStateMessages() {}

    override fun handleLoading() {}

    private fun getVersionName(): String {
        return try {
            requireContext().run {
                val pInfo = packageManager.getPackageInfo(packageName, 0)
                pInfo.versionName
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            getString(R.string.unknown)
        }
    }

}