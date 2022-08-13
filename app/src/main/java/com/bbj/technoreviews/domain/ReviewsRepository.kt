package com.bbj.technoreviews.domain

import com.bbj.technoreviews.data.modeks.Preview
import com.bbj.technoreviews.data.modeks.Review
import io.reactivex.rxjava3.core.Observable

interface ReviewsRepository {

    fun getPreviewList(searchRequest : String) : Observable<Preview>

    fun getReviewList() : Observable<Review>

}