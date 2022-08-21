package com.bbj.technoreviews.data

import android.content.Context
import com.bbj.technoreviews.data.models.Sample
import com.bbj.technoreviews.data.models.Review
import com.bbj.technoreviews.domain.ReviewsRepository
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.ObservableOnSubscribe

class ReviewsRepositoryImpl(context : Context) : ReviewsRepository {

    companion object{
        private var instance : ReviewsRepositoryImpl? = null

        fun getInstance(context: Context? = null) : ReviewsRepositoryImpl?{
            if (instance != null){
                return instance
            } else if (context != null){
                instance = ReviewsRepositoryImpl(context)
                return instance
            } else throw Exception("Context not found")
        }
    }

    private val dnsParser = DNSParser()
    private val kaspiParser = KaspiParser()
    private val bvParser = BVParser(context)

    override fun getSampleList(searchRequest : String): Observable<Sample> {
        return Observable.create(object : ObservableOnSubscribe<Sample> {
            override fun subscribe(emitter: ObservableEmitter<Sample>) {
                dnsParser.getSampleStream(searchRequest,emitter)
                kaspiParser.getSampleStream(searchRequest,emitter)
                bvParser.getSampleStream(searchRequest,emitter)
            }
        })
    }

    override fun getReviewList(position : Int, shopName : Shop): Observable<Review> {
        return Observable.create(object : ObservableOnSubscribe<Review> {
            override fun subscribe(emitter: ObservableEmitter<Review>) {
                when (shopName){
                    Shop.DNS -> {
                        dnsParser.getReviewStream(position, emitter)
                        emitter.onComplete()
                    }
                    Shop.BELIY_VETER -> {
                        bvParser.getReviewStream(position, emitter)
                        emitter.onComplete()
                    }
                    Shop.KASPI -> {
                        kaspiParser.getReviewStream(position,emitter)
                        emitter.onComplete()
                    }
                    else -> throw Exception("Unknown shop name")
                }

            }
        })
    }
}