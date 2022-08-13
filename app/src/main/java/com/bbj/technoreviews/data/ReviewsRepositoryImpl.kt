package com.bbj.technoreviews.data

import com.bbj.technoreviews.data.modeks.Preview
import com.bbj.technoreviews.data.modeks.Review
import com.bbj.technoreviews.domain.ReviewsRepository
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import io.reactivex.rxjava3.schedulers.Schedulers

class ReviewsRepositoryImpl : ReviewsRepository {

    private val DNSPArser = DNSParser()

    override fun getPreviewList(searchRequest : String): Observable<Preview> {
        return Observable.create(object : ObservableOnSubscribe<Preview> {
            override fun subscribe(emitter: ObservableEmitter<Preview>) {
                DNSPArser.getPreviewStream(searchRequest,emitter)
                emitter.onComplete()
            }
        })
    }

    override fun getReviewList(): Observable<Review> {
        return Observable.create(object : ObservableOnSubscribe<Review> {
            override fun subscribe(emitter: ObservableEmitter<Review>) {
                DNSPArser.getReviewStream(emitter)
                emitter.onComplete()
            }
        })
    }

    fun initParseReviews(){
        getReviewList().observeOn(Schedulers.computation()).subscribe()
    }
}