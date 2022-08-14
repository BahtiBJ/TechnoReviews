package com.bbj.technoreviews.domain

import com.bbj.technoreviews.data.Shop
import com.bbj.technoreviews.data.modeks.Sample
import com.bbj.technoreviews.data.modeks.Review
import io.reactivex.rxjava3.core.Observable

interface ReviewsRepository {

    fun getSampleList(searchRequest : String) : Observable<Sample>

    fun getReviewList(position : Int, shopName : Shop) : Observable<Review>

}