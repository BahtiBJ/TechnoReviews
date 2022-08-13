package com.bbj.technoreviews.view.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bbj.technoreviews.R
import com.bbj.technoreviews.data.Shop
import com.bbj.technoreviews.data.modeks.Preview
import com.squareup.picasso.Picasso

class PreviewListAdapter(context: Context, val onProductClick: OnProductClick) :
    RecyclerView.Adapter<PreviewListAdapter.ViewHolder>() {

    val inflater = LayoutInflater.from(context)
    val logoArray = arrayListOf<Drawable>(context.resources.getDrawable(R.drawable.dns_logo, null))
    private var previewList = arrayListOf<Preview>()

    fun addElement(preview: Preview) {
        previewList.add(preview)
        notifyItemChanged(previewList.lastIndex)
    }

    fun addElements(previewList: ArrayList<Preview>) {
        previewList.addAll(previewList)
        notifyDataSetChanged()
    }

    fun clearElements() {
        previewList.clear()
        notifyDataSetChanged()
    }

    interface OnProductClick {
        fun click(productName: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.preview_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (previewList[position].rating.toInt() != 404) {
            holder.run {
                previewList[position].let { item ->
                    shopIcon.setImageDrawable(selectShopImage(item.shopName))

                    productCard.setOnClickListener {
                        onProductClick.click(previewList[position].productName)
                    }

                    productName.text = item.productName
                    productRating.setRating(item.rating)

                    Picasso.get()
                        .load(item.previewImageUrl)
                        .placeholder(R.drawable.place_holder)
                        .error(R.drawable.error)
                        .fit()
                        .into(productImage)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return previewList.size
    }

    private fun selectShopImage(shop: Shop): Drawable {
        return when (shop) {
            Shop.DNS -> logoArray[0]
            Shop.ALSER -> TODO()
            Shop.BELIY_VETER -> TODO()
            Shop.MECHTA -> TODO()
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val enterAnim = AnimationUtils.loadAnimation(itemView.context, R.anim.product_enter_anim)
        val exitAnim = AnimationUtils.loadAnimation(itemView.context, R.anim.product_exit_anim)
        val shopArea = itemView.findViewById<LinearLayoutCompat>(R.id.shop_area).apply {
            setOnTouchListener(object : View.OnTouchListener {
                override fun onTouch(view: View?, event: MotionEvent?): Boolean {
                    when (event?.action) {
                        MotionEvent.ACTION_DOWN -> {
                            onClick(view)
                        }
                        MotionEvent.ACTION_UP -> {
                            view?.performClick()
                        }

                    }
                    return true
                }
            })
        }

        val shopIcon = itemView.findViewById<ImageView>(R.id.shop_icon)
        val shopShowButton = itemView.findViewById<ImageView>(R.id.shop_arrow_button)

        val productCard = itemView.findViewById<CardView>(R.id.product_card)
        val productImage = itemView.findViewById<ImageView>(R.id.product_image)
        val productName = itemView.findViewById<TextView>(R.id.product_name)
        val productRating = itemView.findViewById<RatingBar>(R.id.rating)

        fun onClick(view: View?) {
            Log.d("ADAPTER", "On click")
            if (productCard.visibility != View.VISIBLE) {
                productCard.startAnimation(enterAnim)
                productCard.animation.setAnimationListener(object : Animation.AnimationListener{
                    override fun onAnimationStart(p0: Animation?) {
                        productCard.visibility = View.VISIBLE
                    }
                    override fun onAnimationEnd(p0: Animation?) {}
                    override fun onAnimationRepeat(p0: Animation?) {}
                })
            } else {
                productCard.startAnimation(exitAnim)
                productCard.animation.setAnimationListener(object : Animation.AnimationListener{
                    override fun onAnimationStart(p0: Animation?) {}
                    override fun onAnimationEnd(p0: Animation?) {
                        productCard.visibility = View.GONE
                    }
                    override fun onAnimationRepeat(p0: Animation?) {}
                })

            }
        }
    }

}