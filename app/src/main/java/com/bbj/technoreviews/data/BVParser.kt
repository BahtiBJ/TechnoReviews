package com.bbj.technoreviews.data

import android.content.Context
import android.util.Log
import com.bbj.technoreviews.data.models.Review
import com.bbj.technoreviews.data.models.Sample
import com.bbj.technoreviews.domain.Parser
import io.reactivex.rxjava3.core.ObservableEmitter
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

class BVParser(context: Context) : Parser {

    private val TAG = "BVParser"
    private val baseUrl = "https://shop.kz/search/?q="

    private lateinit var sample: Sample
    private val reviewMap = hashMapOf<Int, ArrayList<Review>>()

    private var productList: Elements? = null
    private var currentSearchRequest = ""

    private val jsPageAssistant = JSPageAssistant(context)

    override fun getSampleStream(
        searchRequest: String,
        sampleEmitter: ObservableEmitter<Sample>
    ) {
        if (searchRequest != currentSearchRequest) {
            currentSearchRequest = searchRequest
            reviewMap.clear()
        }
        var sample: Sample
        try {
            jsPageAssistant.onReceive = { html ->
                productList = parseProductList(html)
                for (i in 0 until productList!!.size) {
                    Log.d(TAG, "get previews")
                    sample = getSample(productList!![i])
                    Log.d(TAG, "get sample stream $sample")
                    sampleEmitter.onNext(sample)
                }
                sampleEmitter.onComplete()
            }
            jsPageAssistant.requestHTML(baseUrl + searchRequest.trim().replace(" ", "%20"))
        } catch (e: Exception) {
            sampleEmitter.onError(e)
        }
    }

    override fun getReviewStream(position: Int, reviewListEmitter: ObservableEmitter<Review>) {
        reviewMap.get(position)?.let { reviews ->
            for (review in reviews)
                reviewListEmitter.onNext(review)
            reviewListEmitter.onComplete()
            return
        }
        productList?.get(position)?.let {
            try {
                getReviews(position,it, reviewListEmitter)
            } catch (e: Exception) {
                reviewListEmitter.onError(e)
            }
        }
    }

    private fun parseProductList(html: String): Elements {
        val document = Jsoup.parse(html)
        val parsedElements = document.select("div.multisearch-page__product__wrapper")
        Log.d(TAG, "Parse product list size = ${parsedElements.size}")
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
        position: Int,
        element: Element,
        reviewListEmitter: ObservableEmitter<Review>) {
        val url = element.select("a[href]")
            .attr("abs:href")
        Log.d(TAG, "getReviewsFromCurrentSite $url")
        jsPageAssistant.onReceive = {html ->
            val reviewList: ArrayList<Review> = arrayListOf()
            val reviewDocument = Jsoup.parse(html)
            val reviewElements = reviewDocument.select("div.bx_review_item")
            Log.d(TAG, "Review elements size = ${reviewElements.size} ")
            var starCount: Int
            var reviewText: String
            var review: Review
            for (reviewElement in reviewElements) {
                reviewText = getText(reviewElement)
                starCount = getStarCount(reviewElement)
                review = Review(starCount, reviewText)
                reviewList.add(review)
                reviewListEmitter.onNext(review)
            }
            Log.d(TAG, "get Reviews = $reviewList ")
            reviewMap.put(position, reviewList)
            reviewListEmitter.onComplete()
        }
        jsPageAssistant.requestHTML(url)
    }

    private fun getText(element: Element): String {
        val paragraphs = element.select("div.bx_review_text_i")
        var reviewText = ""
        for (paragraph in paragraphs) {
            reviewText += paragraph.text() + "\n\n"
        }
        return reviewText
    }

    private fun getStarCount(element: Element) : Int{
        val title = element.select("div.col-md-3.bx_stars").attr("title")
        return when (title){
            "Отлично" -> 5
            "Хорошо" -> 4
            "Нормально" -> 3
            "Плохо" -> 2
            "Ужасно" -> 1
            else -> 0
        }
    }

}