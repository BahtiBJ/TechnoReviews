package com.bbj.technoreviews.domain

import com.bbj.technoreviews.data.modeks.Sample
import com.bbj.technoreviews.data.modeks.Review
import io.reactivex.rxjava3.core.ObservableEmitter

interface Parser {

    fun getSampleStream(searchRequest : String, sampleEmitter: ObservableEmitter<Sample>)

    fun getReviewStream(reviewListEmitter: ObservableEmitter<Review>)

}