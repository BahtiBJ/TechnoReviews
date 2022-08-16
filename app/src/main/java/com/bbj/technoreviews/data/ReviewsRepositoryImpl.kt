package com.bbj.technoreviews.data

import com.bbj.technoreviews.data.models.Sample
import com.bbj.technoreviews.data.models.Review
import com.bbj.technoreviews.domain.ReviewsRepository
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.ObservableOnSubscribe

object ReviewsRepositoryImpl : ReviewsRepository {

    private val dnsParser = DNSParser()
    private val bvParser = BVParser()

    override fun getSampleList(searchRequest : String): Observable<Sample> {
        return Observable.create(object : ObservableOnSubscribe<Sample> {
            override fun subscribe(emitter: ObservableEmitter<Sample>) {
                dnsParser.getSampleStream(searchRequest,emitter)
                bvParser.getSampleStream(searchRequest,emitter)
                emitter.onComplete()
            }
        })
    }

    override fun getReviewList(position : Int, shopName : Shop): Observable<Review> {
        return Observable.create(object : ObservableOnSubscribe<Review> {
            override fun subscribe(emitter: ObservableEmitter<Review>) {
                when (shopName){
                    Shop.DNS -> dnsParser.getReviewStream(position,emitter)
                    Shop.BELIY_VETER -> bvParser.getReviewStream(position,emitter)
                    Shop.MECHTA -> TODO()
                    Shop.ALSER -> TODO()
                    else -> error("Unknown shop name")
                }
                emitter.onComplete()
            }
        })
    }
}