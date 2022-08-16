package com.bbj.technoreviews.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bbj.technoreviews.R
import com.bbj.technoreviews.data.Shop
import com.bbj.technoreviews.data.models.Sample
import com.squareup.picasso.Picasso

class SampleListAdapter(context: Context, val onProductClick: OnProductClick) :
    RecyclerView.Adapter<SampleListAdapter.ViewHolder>() {

    private val inflater = LayoutInflater.from(context)
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
        fun click(productName: String, shop : Shop, position : Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.sample_inner_product_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.run {
            samplesList[position].let { item ->
                productCard.apply {
                    setOnTouchListener(object : View.OnTouchListener {
                        override fun onTouch(view: View?, event: MotionEvent?): Boolean {
                            when (event?.action) {
                                MotionEvent.ACTION_UP -> {
                                    onProductClick.click(item.productName,
                                    item.shopName,
                                    position)
                                    view?.performClick()
                                }
                            }
                            return true
                        }
                    })
                }

                productName.text = item.productName
                productRating.setRating(item.rating)
                productReviewCount.text = "(${item.reviewCount})"

                Picasso.get()
                    .load(item.previewImageUrl)
                    .placeholder(R.drawable.place_holder)
                    .error(R.drawable.error)
                    .fit()
                    .into(productImage)
            }
        }
    }

    override fun getItemCount(): Int {
        return samplesList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productCard = itemView.findViewById<CardView>(R.id.product_card)
        val productImage = itemView.findViewById<ImageView>(R.id.product_image)
        val productName = itemView.findViewById<TextView>(R.id.product_name)
        val productRating = itemView.findViewById<RatingBar>(R.id.rating)
        val productReviewCount = itemView.findViewById<TextView>(R.id.product_review_count)
    }

}