package com.wanglu.photoviewerlibrary

import android.graphics.Rect
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

class PhotoViewerPagerAdapter(private var mData: ArrayList<String>, fragmentManager: FragmentManager) : FragmentStatePagerAdapter2(fragmentManager) {

    lateinit var listener: OnExitListener

    var initPosition: Int = 0 // 首次点击进入时位置

    var current: Int = 0 // 当前位置

    lateinit var firstSourceBounds: Rect

    var enterAnimConsumed = false // 首次进入时执行动画，后面就再也不会执行

    override fun getItem(position: Int): Fragment {
        val f = PhotoViewerFragment()
        f.exitListener = object : PhotoViewerFragment.OnExitListener {
            override fun exit() {
                listener.exit()
            }

        }
        var enterAnim = false
        if (initPosition == position && !enterAnimConsumed) {
            enterAnim = true
            enterAnimConsumed = true
            f.setSourceBounds(firstSourceBounds)
        }

        f.setData(mData[position], enterAnim)
        return f
    }

    override fun getCount(): Int {
        return mData.size
    }

    interface OnExitListener {

        fun exit()
    }

    // 更新当前大图对应缩略图的边框
    fun updateSourceBounds(bounds: Rect) {

        val fragment = getInstantiateItem(current) as PhotoViewerFragment?
        fragment?.setSourceBounds(bounds)
    }
}
