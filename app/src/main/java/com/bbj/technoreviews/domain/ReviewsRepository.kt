package com.bbj.technoreviews.domain

import com.bbj.technoreviews.data.Shop
import com.bbj.technoreviews.data.models.Sample
import com.bbj.technoreviews.data.models.Review
import io.reactivex.rxjava3.core.Observable

interface ReviewsRepository {

    fun getSampleList(searchRequest : String) : Observable<Sample>

    fun getReviewList(position : Int, shopName : Shop) : Observable<Review>

}