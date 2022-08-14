package com.bbj.technoreviews.view.adapter

import android.animation.Animator
import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bbj.technoreviews.R
import com.bbj.technoreviews.data.Shop
import com.bbj.technoreviews.data.modeks.Sample
import com.squareup.picasso.Picasso
import kotlin.collections.ArrayList

class SampleListAdapter(context: Context, val onProductClick: OnProductClick) :
    RecyclerView.Adapter<SampleListAdapter.ViewHolder>() {

    val inflater = LayoutInflater.from(context)
    val logoArray = arrayListOf<Drawable>(context.resources.getDrawable(R.drawable.dns_logo, null))
    private var samplesList = arrayListOf<Sample>()

    fun addElement(sample: Sample) {
        samplesList.add(sample)
        notifyItemChanged(samplesList.lastIndex)
    }

    fun addElements(sampleList: ArrayList<Sample>) {
        sampleList.addAll(sampleList)
        notifyDataSetChanged()
    }

    fun clearElements() {
        samplesList.clear()
        notifyDataSetChanged()
    }

    interface OnProductClick {
        fun click(productName: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.sample_list_item, parent, false)
        (view.findViewById<ViewGroup>(R.id.shop_parent)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        if (samplesList[position].rating.toInt() != 404) {
            holder.run {
                samplesList[position].let { item ->
                    Handler(Looper.getMainLooper()).postDelayed({ productCard.visibility = View.GONE },50)
                    shopIcon.setImageDrawable(selectShopImage(item.shopName))
                    productCard.apply {
                        setOnTouchListener(object : View.OnTouchListener {
                            override fun onTouch(view: View?, event: MotionEvent?): Boolean {
                                when (event?.action) {
                                    MotionEvent.ACTION_UP -> {
                                        onProductClick.click(samplesList[position].productName)
                                        view?.performClick()
                                    }
                                }
                                return true
                            }
                        })
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
        return samplesList.size
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
        val shopArea = itemView.findViewById<LinearLayoutCompat>(R.id.shop_area).apply {
            setOnTouchListener(object : View.OnTouchListener {
                override fun onTouch(view: View?, event: MotionEvent?): Boolean {
                    when (event?.action) {
                        MotionEvent.ACTION_UP -> {
                            onClick(view)
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
                Log.d("ADAPTER", "Gone")
                shopShowButton
                    .animate().rotationBy(180f).apply {
                        duration = 300
                        interpolator = AccelerateInterpolator()
                        start()
                    }
                productCard.translationY = -400f
                productCard.visibility = View.VISIBLE
                productCard.animate().apply {
                    duration = 300
                    translationY(0f)
                    interpolator = AccelerateInterpolator()
                }.setListener(object : Animator.AnimatorListener{
                    override fun onAnimationStart(p0: Animator?) {
                        shopArea.isEnabled = false
                    }

                    override fun onAnimationEnd(p0: Animator?) {
                        shopArea.isEnabled = true
                    }
                    override fun onAnimationCancel(p0: Animator?) {}
                    override fun onAnimationRepeat(p0: Animator?) { }
                }).start()
                productCard.visibility = View.VISIBLE
            } else {
                Log.d("ADAPTER", "Visble")
                shopShowButton
                    .animate().rotationBy(180f).apply {
                        duration = 300
                        interpolator = AccelerateInterpolator()
                        start()
                    }
                productCard.animate().apply {
                    duration = 300
                    translationY(-400f)
                    interpolator = AccelerateInterpolator()
                }.start()
                Handler(Looper.getMainLooper()).postDelayed({productCard.visibility = View.GONE},300)
            }
        }
    }

}