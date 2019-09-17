package com.wanglu.photoviewerlibrary

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Rect
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.item_picture.*

class PhotoViewerFragment : Fragment() {


    var exitListener: OnExitListener? = null
    var longClickListener: OnLongClickListener? = null

    private var mImgSize = IntArray(2)
    private var mExitLocation = IntArray(2)
    private var mInAnim = true
    private var mPicData = ""
    /**
     * 每次选中图片后设置图片信息
     */
    fun setData(picData: String, inAnim: Boolean) {
        mInAnim = inAnim
        mPicData = picData
    }

    fun setSourceBounds(bounds: Rect) {
        mImgSize = intArrayOf(bounds.right - bounds.left, bounds.bottom - bounds.top)
        mExitLocation = intArrayOf((bounds.left + bounds.right)/2, (bounds.top + bounds.bottom)/2)
        mIv?.setExitLocation(mExitLocation)
        mIv?.setImgSize(mImgSize)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.item_picture, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        onLazyLoad()
    }

    interface OnExitListener {
        fun exit()
    }



    fun onLazyLoad() {


        if (PhotoViewer.mInterface != null) {
            PhotoViewer.mInterface!!.show(mIv, mPicData)
        } else {
            throw RuntimeException("请设置图片加载回调 ShowImageViewInterface")
        }

        var alpha = 1f  // 透明度
        mIv.setExitLocation(mExitLocation)
        mIv.setImgSize(mImgSize)
        mIv.setOnLongClickListener {
            if (longClickListener != null) {
                longClickListener!!.onLongClick(it)
            }
            true
        }

        // 循环查看是否添加上了图片
        Thread(Runnable {
            while (true) {
                if (mIv.drawable != null) {
                    activity!!.runOnUiThread {
                        loading.visibility = View.GONE
                    }
                    break
                }
                Thread.sleep(300)
            }
        }).start()

        var intAlpha = 255
        root.background.alpha = intAlpha
        mIv.rootView = root
        mIv.setOnViewFingerUpListener {
            alpha = 1f
            intAlpha = 255
        }

        // 注册退出Activity 滑动大于一定距离后退出
        mIv.setExitListener {
            if (exitListener != null) {
                exitListener!!.exit()
            }
        }

        // 添加点击进入时的动画
        if (mInAnim) {

            // 动画必须在draw之前设置，否则会出现屏幕闪烁的问题
            mIv.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener{
                override fun onPreDraw(): Boolean {
                    mIv.viewTreeObserver.removeOnPreDrawListener(this)

                    val scaleOa = ObjectAnimator.ofFloat(mIv, "scale", mImgSize[0].toFloat() / mIv.width, 1f)
                    val xOa = ObjectAnimator.ofFloat(mIv, "translationX", mExitLocation[0].toFloat() - mIv.width / 2, 0f)
                    val yOa = ObjectAnimator.ofFloat(mIv, "translationY", mExitLocation[1].toFloat() - mIv.height / 2, 0f)
                    val alphaOa = ObjectAnimator.ofInt(root.background, "alpha", 0, intAlpha)

                    val set = AnimatorSet()
                    set.duration = 250
                    set.playTogether(scaleOa, xOa, yOa, alphaOa)
                    set.start()

                    return true
                }
            })
        }

        root.isFocusableInTouchMode = true
        root.requestFocus()
        root.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {

                mIv.exit()

                return@OnKeyListener true
            }
            false
        })

        mIv.setOnViewDragListener { dx, dy ->

            mIv.scrollBy((-dx).toInt(), (-dy).toInt())  // 移动图像
            alpha -= dy * 0.001f
            intAlpha -= (dy * 0.5).toInt()
            if (alpha > 1) alpha = 1f
            else if (alpha < 0) alpha = 0f
            if (intAlpha < 0) intAlpha = 0
            else if (intAlpha > 255) intAlpha = 255
            root.background.alpha = intAlpha    // 更改透明度

            if (alpha >= 0.6)
                mIv.attacher.scale = alpha   // 更改大小


        }


        mIv.setOnClickListener {

            mIv.exit()
        }

    }

}