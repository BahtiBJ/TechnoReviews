package com.bbj.technoreviews.data

import android.content.Context
import android.os.Handler
import android.os.Looper
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

    private val jsPageAssistant = JSPageAssistant(context)

    private val dnsParser = DNSParser()
    private val kaspiParser = KaspiParser(jsPageAssistant)
    private val bvParser = BVParser(jsPageAssistant)

    override fun getSampleList(searchRequest : String): Observable<Sample> {
        return Observable.create(object : ObservableOnSubscribe<Sample> {
            override fun subscribe(emitter: ObservableEmitter<Sample>) {
                try {
                    dnsParser.getSampleStream(searchRequest, emitter)
                    kaspiParser.getSampleStream(searchRequest, emitter)
                    bvParser.getSampleStream(searchRequest, emitter)
                    Handler(Looper.getMainLooper()).postDelayed({
                        emitter.onComplete()
                    }, 25000)
                } catch (e : java.lang.Exception){
                    emitter.onError(e)
                }
            }
        })
    }

    override fun getReviewList(position : Int, shopName : Shop): Observable<Review> {
        return Observable.create(object : ObservableOnSubscribe<Review> {
            override fun subscribe(emitter: ObservableEmitter<Review>) {
                try {
                    when (shopName) {
                        Shop.DNS -> {
                            dnsParser.getReviewStream(position, emitter)
                        }
                        Shop.BELIY_VETER -> {
                            bvParser.getReviewStream(position, emitter)
                        }
                        Shop.KASPI -> {
                            kaspiParser.getReviewStream(position, emitter)
                        }
                        else -> throw Exception("Unknown shop name")
                    }
                    Handler(Looper.getMainLooper()).postDelayed({
                        emitter.onComplete()
                    }, 20000)
                } catch (e : java.lang.Exception){
                    emitter.onError(e)
                }
            }
        })
    }
}