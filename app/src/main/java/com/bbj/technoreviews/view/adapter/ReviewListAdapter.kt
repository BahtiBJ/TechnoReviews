package com.bbj.technoreviews.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bbj.technoreviews.R
import com.bbj.technoreviews.data.modeks.Review

class ReviewListAdapter(context :Context) : RecyclerView.Adapter<ReviewListAdapter.ViewHolder>() {

    val inflater = LayoutInflater.from(context)
    private val reviewList = arrayListOf<Review>()

    fun addElement(review : Review){
        reviewList.add(review)
        notifyItemChanged(reviewList.lastIndex)
    }

    fun addElements(reviewList : ArrayList<Review>){
        this.reviewList.addAll(reviewList)
        notifyDataSetChanged()
    }

    fun clearElements(){
        reviewList.clear()
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.review_list_item,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.reviewRating.setRating(reviewList[position].starCount.toFloat())
        holder.reviewText.text = reviewList[position].text
    }

    override fun getItemCount(): Int {
        return reviewList.size
    }

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val reviewText = itemView.findViewById<TextView>(R.id.review_text)
        val reviewRating = itemView.findViewById<RatingBar>(R.id.review_rating)
    }

}