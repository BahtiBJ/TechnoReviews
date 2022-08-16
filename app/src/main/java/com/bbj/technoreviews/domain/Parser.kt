package com.bbj.technoreviews.domain

import com.bbj.technoreviews.data.models.Sample
import com.bbj.technoreviews.data.models.Review
import io.reactivex.rxjava3.core.ObservableEmitter

interface Parser {

    fun getSampleStream(searchRequest : String, sampleEmitter: ObservableEmitter<Sample>)

    fun getReviewStream(position: Int, reviewListEmitter: ObservableEmitter<Review>)

}