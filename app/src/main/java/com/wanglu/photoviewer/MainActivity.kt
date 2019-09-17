package com.wanglu.photoviewer

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wanglu.photoviewerlibrary.OnLongClickListener
import com.wanglu.photoviewerlibrary.PhotoViewer
import com.wanglu.photoviewerlibrary.Utils
import com.yqritc.recyclerviewflexibledivider.VerticalDividerItemDecoration
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recycler_view.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        recycler_view.addItemDecoration(VerticalDividerItemDecoration.Builder(this)
                .size(Utils.dp2px(applicationContext, 10))
                .colorResId(android.R.color.transparent)
                .build())

        val picData = arrayListOf(
                "https://c-ssl.duitang.com/uploads/item/201703/21/20170321153911_E8YCa.jpeg",
                "https://c-ssl.duitang.com/uploads/item/201611/11/20161111233359_Lx5fG.jpeg",
                "https://c-ssl.duitang.com/uploads/item/201711/30/20171130220751_MYhtu.jpeg",
                "https://c-ssl.duitang.com/uploads/item/201711/08/20171108153135_8wtXj.jpeg",
                "https://c-ssl.duitang.com/uploads/item/201809/25/20180925004012_yfaji.jpeg",
                "https://a-ssl.duitang.com/uploads/item/201809/27/20180927183552_h3dhE.jpeg",
                "https://c-ssl.duitang.com/uploads/item/201508/19/20150819220930_tLUwW.jpeg",
                "https://c-ssl.duitang.com/uploads/item/201508/16/20150816140103_tCw5X.jpeg",
                "https://c-ssl.duitang.com/uploads/item/201508/15/20150815193834_FLEYN.jpeg",
                "https://c-ssl.duitang.com/uploads/item/201509/29/20150929104020_kvuQh.jpeg",
                "https://c-ssl.duitang.com/uploads/item/201408/11/20140811100757_8GzTV.jpeg",
                "https://c-ssl.duitang.com/uploads/item/201504/18/20150418H2815_cYQFw.png",
                "https://c-ssl.duitang.com/uploads/item/201509/27/20150927141827_JtEYW.jpeg",
                "https://c-ssl.duitang.com/uploads/item/201510/03/20151003191934_WzHJN.jpeg"
        )
        for (i in 1..1000) {
            picData.add("https://c-ssl.duitang.com/uploads/item/201703/21/20170321153911_E8YCa.jpeg")
        }
        val adapter = object : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_img, picData) {
            override fun convert(helper: BaseViewHolder?, item: String?) {
                Glide.with(applicationContext).load(item).into(helper!!.itemView as ImageView)
            }
        }
        adapter.setOnItemClickListener{_, _, position ->
            PhotoViewer
                    .setData(picData)
                    .setImgContainer(recycler_view)
                    .setOnPhotoViewerCreatedListener{

                        Toast.makeText(applicationContext, "created", Toast.LENGTH_LONG).show()
                    }
                    .setOnLongClickListener(object : OnLongClickListener {
                        override fun onLongClick(view: View) {
                            Toast.makeText(view.context, "haha", Toast.LENGTH_LONG).show()
                        }
                    })
                    .setCurrentPage(position)
                    .setShowImageViewInterface(object : PhotoViewer.ShowImageViewInterface {
                        override fun show(iv: ImageView, loading: View, url: String) {
                            Glide.with(iv.context)
                                    .load(url)
                                    .listener(object: RequestListener<Drawable> {
                                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {

                                            loading.visibility = View.GONE
                                            return false
                                        }

                                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {

                                            loading.visibility = View.GONE
                                            return false
                                        }
                                    })
                                    .into(iv)
                        }
                    })
                    .start( this)
        }
        recycler_view.adapter = adapter
    }
}
