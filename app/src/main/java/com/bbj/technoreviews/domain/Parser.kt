package com.bbj.technoreviews.domain

import com.bbj.technoreviews.data.modeks.Preview
import com.bbj.technoreviews.data.modeks.Review
import io.reactivex.rxjava3.core.ObservableEmitter

interface Parser {

    fun getPreviewStream(searchRequest : String, previewEmitter: ObservableEmitter<Preview>)

    fun getReviewStream(reviewListEmitter: ObservableEmitter<Review>)

}