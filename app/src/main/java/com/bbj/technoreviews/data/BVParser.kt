package com.bbj.technoreviews.data

import android.util.Log
import com.bbj.technoreviews.data.models.Review
import com.bbj.technoreviews.data.models.Sample
import com.bbj.technoreviews.domain.Parser
import io.reactivex.rxjava3.core.ObservableEmitter
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

class BVParser : Parser {

    private val TAG = "BVParser"
    private val baseUrl = "https://shop.kz/search/?q="

    private lateinit var sample: Sample
    private val reviewMap = hashMapOf<Int, ArrayList<Review>>()

    private var productList: Elements? = null
    private var currentSearchRequest = ""


    override fun getSampleStream(
        searchRequest: String,
        sampleEmitter: ObservableEmitter<Sample>
    ) {
        if (searchRequest != currentSearchRequest) {
            currentSearchRequest = searchRequest
            reviewMap.clear()
        }
        try {
            productList = parseProductList(searchRequest)
            Log.d(TAG, "get previews")
            for (i in 0 until productList!!.size) {
                sampleEmitter.onNext(getSample(productList!![i]))
            }
        } catch (e: Exception) {
            sampleEmitter.onError(e)
        }
    }

    override fun getReviewStream(position: Int, reviewListEmitter: ObservableEmitter<Review>) {
        reviewMap.get(position)?.let { reviews ->
            for (review in reviews)
                reviewListEmitter.onNext(review)
            return
        }
        productList?.get(position)?.let {
            try {
                reviewMap.put(position,getReviews(it, reviewListEmitter))
            } catch (e: Exception) {
                reviewListEmitter.onError(e)
            }
        }
    }

    private fun parseProductList(searchRequest: String): Elements {
        val document =
            Jsoup.connect(baseUrl +
                    searchRequest.apply {
                        trim()
                        replace(" ", "%20")
                    })
                .timeout(30000)
                .get()
        Log.d(TAG, "get result")
        val parsedElements = document.select("div.multisearch-page__product gtm-impression-product")
        return parsedElements

    }

    private fun getSample(element: Element): Sample {
        Log.d(TAG, "get preview")
        val previewImage = element.getElementsByTag("img").attr("data-src")
        val productName =
            element.select("a[href]").text()
        val reviewCount = 0
        val rating = 0.0f
        return Sample(Shop.BELIY_VETER, previewImage, productName, rating, reviewCount)
    }


    private fun getReviews(
        element: Element,
        reviewListEmitter: ObservableEmitter<Review>
    ) : ArrayList<Review> {
        val url = element.select("a[href]")
            .attr("abs:href")
        Log.d(TAG, "getReviewsFromCurrentSite $url")
        val reviewList: ArrayList<Review> = arrayListOf()
        val reviewDocument = Jsoup.connect("$url").get()
        val reviewElements = reviewDocument.select("div.bx_review_item")
        var starCount: Int
        var reviewText: String
        var review: Review
        //.subList(1, reviewElements.size
        for (reviewElement in reviewElements) {
            reviewText = getText(reviewElement)
            starCount = 5 -  element.select("a.fa fa-star-o").size
            review = Review(starCount, reviewText)
            reviewList.add(review)
            reviewListEmitter.onNext(review)
        }
        return reviewList
    }

    private fun getText(element: Element) : String{
        val paragraphs = element.select("div.bx_review_text_i")
        var reviewText = ""
        for (paragraph in paragraphs){
            reviewText += paragraph.text() + "\n"
        }
        return reviewText
    }

}