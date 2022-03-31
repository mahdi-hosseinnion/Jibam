package com.ssmmhh.jibam.feature_aboutus

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
import com.ssmmhh.jibam.feature_common.BaseFragment
import com.ssmmhh.jibam.util.localizeNumber
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview


@ExperimentalCoroutinesApi
@FlowPreview
class AboutUsFragment(
    viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager
) : BaseFragment() {

    private var _binding: FragmentAboutUsBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAboutUsBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setBackground()
        initUi()
    }

    private fun initUi() {
        binding.toolbar.topAppBarNormal.title = getString(R.string.about)
        binding.versionName.text = getVersionName()?.let {
            getString(R.string.version) + ": ${it.localizeNumber(resources)}"
        }
        binding.toolbar.topAppBarNormal.setNavigationOnClickListener {
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
                                binding.aboutUsRoot.background = resource
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