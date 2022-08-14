package com.bbj.technoreviews.data

import com.bbj.technoreviews.data.modeks.Sample
import com.bbj.technoreviews.data.modeks.Review
import com.bbj.technoreviews.domain.ReviewsRepository
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.ObservableOnSubscribe

object ReviewsRepositoryImpl : ReviewsRepository {

    private val DNSPArser = DNSParser()

    override fun getSampleList(searchRequest : String): Observable<Sample> {
        return Observable.create(object : ObservableOnSubscribe<Sample> {
            override fun subscribe(emitter: ObservableEmitter<Sample>) {
                DNSPArser.getSampleStream(searchRequest,emitter)
                emitter.onComplete()
            }
        })
    }

    override fun getReviewList(position : Int, shopName : Shop): Observable<Review> {
        return Observable.create(object : ObservableOnSubscribe<Review> {
            override fun subscribe(emitter: ObservableEmitter<Review>) {
                when (shopName){
                    Shop.DNS -> DNSPArser.getReviewStream(position,emitter)
                    Shop.BELIY_VETER -> TODO()
                    Shop.MECHTA -> TODO()
                    Shop.ALSER -> TODO()
                    else -> error("Unknown shop name")
                }
                emitter.onComplete()
            }
        })
    }
}