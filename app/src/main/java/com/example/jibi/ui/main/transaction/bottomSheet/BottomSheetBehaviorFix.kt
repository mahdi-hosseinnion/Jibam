package com.example.jibi.ui.main.transaction.bottomSheet

//import android.view.View
//import androidx.annotation.VisibleForTesting
//import androidx.viewpager.widget.ViewPager
//import com.google.android.material.bottomsheet.BottomSheetBehavior
//import java.lang.ref.WeakReference
//
//
//class BottomSheetBehaviorFix<V : View> : BottomSheetBehavior<V>(), ViewPager.OnPageChangeListener {
//
//    override fun onPageScrollStateChanged(state: Int) {}
//
//    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
//
//    override fun onPageSelected(position: Int) {
//        val container = viewRef?.get() ?: return
//        nestedScrollingChildRef = WeakReference(findScrollingChild(container))
//    }
//
//    @VisibleForTesting
//    override fun findScrollingChild(view: View): View? {
//        return if (view is ViewPager) {
//            view.focusedChild?.let { findScrollingChild(it) }
//        } else {
//            super.findScrollingChild(view)
//        }
//    }
//}
//
//import android.view.View
//import android.view.ViewGroup
//import androidx.coordinatorlayout.widget.CoordinatorLayout
//import androidx.core.view.ViewCompat
//import androidx.viewpager.widget.ViewPager
//import com.google.android.material.bottomsheet.BottomSheetBehavior
//import java.lang.ref.WeakReference
//
//
///**
// * Override [.findScrollingChild] to support [ViewPager]'s nested scrolling.
// *
// * By the way, In order to override package level method and field.
// * This class put in the same package path where [BottomSheetBehavior] located.
// */
//class ViewPagerBottomSheetBehavior<V : View?> :
//    BottomSheetBehavior<V>() {
//    fun findScrollingChild(view: View?): View? {
//        if (ViewCompat.isNestedScrollingEnabled(view!!)) {
//            return view
//        }
//        if (view is ViewPager) {
//            val viewPager = view
//            val currentViewPagerChild = viewPager.getChildAt(viewPager.currentItem)
//            val scrollingChild = findScrollingChild(currentViewPagerChild)
//            if (scrollingChild != null) {
//                return scrollingChild
//            }
//        } else if (view is ViewGroup) {
//            val group = view
//            var i = 0
//            val count = group.childCount
//            while (i < count) {
//                val scrollingChild = findScrollingChild(group.getChildAt(i))
//                if (scrollingChild != null) {
//                    return scrollingChild
//                }
//                i++
//            }
//        }
//        return null
//    }
//
//    fun updateScrollingChild() {
//        val scrollingChild = findScrollingChild(mViewRef.get())
//        mNestedScrollingChildRef = WeakReference(scrollingChild)
//        val container = viewRef?.get() ?: return
//        nestedScrollingChildRef = WeakReference(findScrollingChild(container))
//    }
//
//    companion object {
//        /**
//         * A utility function to get the [ViewPagerBottomSheetBehavior] associated with the `view`.
//         *
//         * @param view The [View] with [ViewPagerBottomSheetBehavior].
//         * @return The [ViewPagerBottomSheetBehavior] associated with the `view`.
//         */
//        fun <V : View?> from(view: V): ViewPagerBottomSheetBehavior<V> {
//            val params = view!!.layoutParams
//            require(params is CoordinatorLayout.LayoutParams) { "The view is not a child of CoordinatorLayout" }
//            val behavior = params.behavior
//            require(behavior is ViewPagerBottomSheetBehavior<*>) { "The view is not associated with ViewPagerBottomSheetBehavior" }
//            return behavior as ViewPagerBottomSheetBehavior<V>
//        }
//    }
//}