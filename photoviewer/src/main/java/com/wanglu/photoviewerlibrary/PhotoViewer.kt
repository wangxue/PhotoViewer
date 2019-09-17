package com.wanglu.photoviewerlibrary

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Rect
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import java.lang.RuntimeException
import java.lang.ref.WeakReference
import java.util.*


@SuppressLint("StaticFieldLeak")
/**
 * Created by WangLu on 2018/7/15.
 */
object PhotoViewer {

    const val INDICATOR_TYPE_DOT = "INDICATOR_TYPE_DOT"
    const val INDICATOR_TYPE_TEXT = "INDICATOR_TYPE_TEXT"


    internal var mInterface: ShowImageViewInterface? = null
    private var mCreatedInterface: OnPhotoViewerCreatedListener? = null
    private var mDestroyInterface: OnPhotoViewerDestroyListener? = null

    private lateinit var imgData: ArrayList<String> // 图片数据
    private lateinit var container: WeakReference<ViewGroup>   // 存放图片的容器， ListView/GridView/RecyclerView
    private var currentPage = 0    // 当前页

    private var longClickListener: OnLongClickListener? = null

    private var indicatorType = INDICATOR_TYPE_DOT   // 默认type为小圆点

    interface OnPhotoViewerCreatedListener {
        fun onCreated()
    }


    interface OnPhotoViewerDestroyListener {
        fun onDestroy()
    }

    fun setOnPhotoViewerCreatedListener(l: () -> Unit): PhotoViewer {

        mCreatedInterface = object : OnPhotoViewerCreatedListener {
            override fun onCreated() {
                l()
            }

        }
        return this
    }

    fun setOnPhotoViewerDestroyListener(l: () -> Unit): PhotoViewer {
        mDestroyInterface = object : OnPhotoViewerDestroyListener {
            override fun onDestroy() {
                l()
            }
        }
        return this
    }

    /**
     * 小圆点的drawable
     * 下标0的为没有被选中的
     * 下标1的为已经被选中的
     */
    private val mDot = intArrayOf(R.drawable.no_selected_dot, R.drawable.selected_dot)


    interface ShowImageViewInterface {
        fun show(iv: ImageView, url: String)
    }

    /**
     * 设置显示ImageView的接口
     */
    fun setShowImageViewInterface(i: ShowImageViewInterface): PhotoViewer {
        mInterface = i
        return this
    }

    /**
     * 设置图片数据
     */
    fun setData(data: ArrayList<String>): PhotoViewer {
        imgData = data
        return this
    }


    fun setImgContainer(container: AbsListView): PhotoViewer {
        this.container = WeakReference(container)
        return this
    }

    fun setImgContainer(container: RecyclerView): PhotoViewer {
        this.container = WeakReference(container)
        return this
    }

    /**
     * 获取itemView
     */
    private fun getItemView(): View {
        val itemView = if (container.get() is AbsListView) {
            val absListView = container.get() as AbsListView
            absListView.getChildAt(currentPage - absListView.firstVisiblePosition)
        } else {
            (container.get() as RecyclerView).layoutManager!!.findViewByPosition(currentPage)
        }

        return if (itemView is ViewGroup) {
            findImageView(itemView)!!
        } else {
            itemView as ImageView
        }
    }

    private fun findImageView(group: ViewGroup): ImageView? {
        for (i in 0 until group.childCount) {
            return when {
                group.getChildAt(i) is ImageView -> group.getChildAt(i) as ImageView
                group.getChildAt(i) is ViewGroup -> findImageView(group.getChildAt(i) as ViewGroup)
                else -> throw RuntimeException("未找到ImageView")
            }
        }
        return null
    }

    /**
     * 获取图片的位置
     */
    private fun getCurrentViewLocation(): Rect {
        val result = IntArray(2)
        getItemView().getLocationInWindow(result)
        return Rect(result[0], result[1], result[0] + getItemView().measuredWidth, result[1] +
                getItemView().measuredHeight)
    }


    /**
     * 设置当前页， 从0开始
     */
    fun setCurrentPage(page: Int): PhotoViewer {
        currentPage = page
        return this
    }

    fun start(activity: AppCompatActivity) {
        show(activity)
    }

    fun setOnLongClickListener(longClickListener: OnLongClickListener): PhotoViewer {
        this.longClickListener = longClickListener
        return this
    }


    /**
     * 设置指示器的样式，但是如果图片大于9张，则默认设置为文字样式
     */
    fun setIndicatorType(type: String): PhotoViewer {
        this.indicatorType = type
        return this
    }

    private fun show(activity: AppCompatActivity) {

        // 检查数据
        if (imgData.size == 0) return
        if (currentPage < 0 || currentPage >= imgData.size) currentPage = 0

        val decorView = activity.window.decorView as ViewGroup

        val frameLayout = FrameLayout(activity)
        LayoutInflater.from(activity).inflate(R.layout.activity_photoviewer, frameLayout)
        val viewPager = frameLayout.findViewById<ViewPager>(R.id.mLookPicVP)

        var mDotGroup: LinearLayout? = null  // 存放小圆点的Group
        var mSelectedDot: View? = null // 选中的小圆点
        var tv: TextView? = null // 文字版当前页

        val adapter = PhotoViewerPagerAdapter(imgData, activity.supportFragmentManager)
        adapter.listener = object : PhotoViewerPagerAdapter.OnExitListener {
            override fun exit() {
                activity.runOnUiThread{
                    decorView.removeView(frameLayout)
                }
            }
        }
        adapter.initPosition = currentPage
        adapter.current = currentPage
        adapter.firstSourceBounds = getCurrentViewLocation()

        viewPager.adapter = adapter
        viewPager.currentItem = currentPage
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

                if (mSelectedDot != null && imgData.size > 1) {
                    val dx = mDotGroup!!.getChildAt(1).x - mDotGroup!!.getChildAt(0).x
                    mSelectedDot!!.translationX = (position * dx) + positionOffset * dx
                }
            }

            override fun onPageSelected(position: Int) {
                currentPage = position

                // 设置文字版本当前页的值
                if (tv != null) {
                    tv!!.text = "${position + 1}/${imgData.size}"
                }

                val view = container.get()

                if (view == null) return

                // 如果滑到的view不在当前页面显示，那么则滑动到那个position，再获取itemView
                if (view !is AbsListView) {
                    val layoutManager = (container.get() as androidx.recyclerview.widget.RecyclerView).layoutManager
                    if (layoutManager is androidx.recyclerview.widget.LinearLayoutManager) {
                        if (currentPage < layoutManager.findFirstVisibleItemPosition() || currentPage > layoutManager.findLastVisibleItemPosition()) {
                            layoutManager.scrollToPosition(currentPage)
                        }
                    } else if (layoutManager is androidx.recyclerview.widget.GridLayoutManager) {
                        if (currentPage < layoutManager.findFirstVisibleItemPosition() || currentPage > layoutManager.findLastVisibleItemPosition()) {
                            layoutManager.scrollToPosition(currentPage)
                        }
                    }
                }

                // recyclerview调用scrollToPosition后需要在下一次layout后才会滚动到实际位置
                view.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver
                .OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        view.viewTreeObserver.removeOnPreDrawListener(this)
                        adapter.current = position
                        adapter.updateSourceBounds(getCurrentViewLocation())
                        return true
                    }
                })
            }

        })

        val mFrameLayout = FrameLayout(activity)
        if (imgData.size in 2..9 && indicatorType == INDICATOR_TYPE_DOT) {

            LayoutInflater.from(activity).inflate(R.layout.layout_indicator_dot, mFrameLayout)

            // 添加未选中的小圆点
            mDotGroup = mFrameLayout.findViewById(R.id.layout_unselected)
            for (i in 0 until imgData.size) {
                val iv = ImageView(activity)
                iv.setImageDrawable(activity.resources.getDrawable(mDot[0]))
                val dotParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT)
                dotParams.rightMargin = Utils.dp2px(activity, 12)
                iv.layoutParams = dotParams
                mDotGroup!!.addView(iv)
            }

            // 设置已选中的小圆点位置
            mSelectedDot = mFrameLayout.findViewById(R.id.dot_selected)
            mSelectedDot!!.translationX = (Utils.dp2px(activity, 12) * currentPage
                    + mDotGroup!!.getChildAt(0).width * currentPage).toFloat()

        } else {
            LayoutInflater.from(activity).inflate(R.layout.layout_indicator_text, mFrameLayout)
            tv = mFrameLayout.findViewById(R.id.text)
            tv!!.text = "${currentPage + 1}/${imgData.size}"
        }

        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        params.bottomMargin = Utils.dp2px(activity, 80)
        frameLayout.addView(mFrameLayout, params)

        decorView.addView(frameLayout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        if (mCreatedInterface != null) {
            mCreatedInterface!!.onCreated()
        }
    }
}