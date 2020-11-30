package com.example.jibi.fragments.main

import android.content.Context
import android.os.Bundle
import androidx.annotation.NavigationRes
import androidx.navigation.fragment.NavHostFragment
import com.example.jibi.ui.main.MainActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi

//article https://medium.com/@zawadz88/androidx-navigation-with-dagger-2-fragmentfactory-789b01b43214
//and https://github.com/mitchtabian/Open-API-Android-App
class MainNavHostFragment : NavHostFragment() {

    @ExperimentalCoroutinesApi
    override fun onAttach(context: Context) {
        childFragmentManager.fragmentFactory =
            (activity as MainActivity).fragmentFactory
        super.onAttach(context)
    }

    companion object {

        const val KEY_GRAPH_ID = "android-support-nav:fragment:graphId"

        @JvmStatic
        fun create(
            @NavigationRes graphId: Int = 0
        ): MainNavHostFragment {
            var bundle: Bundle? = null
            if (graphId != 0) {
                bundle = Bundle()
                bundle.putInt(KEY_GRAPH_ID, graphId)
            }
            val result =
                MainNavHostFragment()
            if (bundle != null) {
                result.arguments = bundle
            }
            return result
        }
    }
}