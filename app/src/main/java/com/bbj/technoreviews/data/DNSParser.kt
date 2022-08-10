package com.bbj.technoreviews.data

import android.util.Log
import com.bbj.technoreviews.Parser
import com.bbj.technoreviews.data.modeks.Preview
import com.bbj.technoreviews.data.modeks.ResultType
import com.bbj.technoreviews.data.modeks.Review
import io.reactivex.rxjava3.core.ObservableEmitter
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

object DNSParser : Parser {

    private val TAG = "DNSParser"
    private val baseUrl = "https://www.dns-shop.kz/search/?order=4&q="

    private lateinit var preview : Preview
    private val reviewList : ArrayList<Preview> = arrayListOf()

    private lateinit var emitter: ObservableEmitter<ResultType>

    override fun getResult(searchRequest: String, emitter: ObservableEmitter<ResultType>) {
        this.emitter = emitter
        try {
            val document =
                Jsoup.connect(baseUrl +
                        searchRequest.apply {
                            trim()
                            replace(" ", "+")
                        })
                    //            .userAgent("Mozilla")
                    //                .timeout(3000)
                    .get()
            Log.d(TAG, "get result")
            val productList = document.select("div[data-id=\"product\"]")
            preview = getPreview(productList[0])
            emitter.onNext(preview)
            Log.d(TAG, "preview getted")
            getReviews(productList)
            Log.d(TAG, "reviews getted")
        } catch (e: Exception) {
            emitter.onError(e)
        }
        emitter.onComplete()
    }

    private fun getPreview(element: Element): Preview {
        Log.d(TAG, "get preview")
        val previewImage = element.getElementsByTag("img").attr("data-src")
        val productName =
            element.select("a.catalog-product__name").text()
        val reviewCount =
            try {
                element.select("a[data-rating]")
                    .text().toInt()
            } catch (e: ClassCastException) {
                0
                Log.d(TAG, "ClassCastException")
            }
        return Preview(Shop.DNS, previewImage, productName, reviewCount)
    }

    private fun getReviews(
        elements: Elements,
    ): ArrayList<Review> {
        Log.d(TAG, "get Reviews")
        val reviewList: ArrayList<Review> = arrayListOf()
        var exemplars: ArrayList<Pair<Int, String>> = arrayListOf()
        for (element in elements) {
            val reviewCount =
                try {
                    element.select("a[data-rating]")
                        .text().toInt()
                } catch (e: ClassCastException) {
                    Log.d(TAG, "ClassCastException")
                    0
                }
            if (reviewCount > 0) {
                val url = element.select("a[href]")
                    .attr("abs:href")
                exemplars.add(Pair(reviewCount, url))
            }
        }
        if (exemplars.size > 3) {
            exemplars.sortByDescending { it.first }
            exemplars = exemplars.subList(0, 3) as ArrayList<Pair<Int, String>>
        }
        for (exemplar in exemplars) {
            getReviewsFromCurrentSite(exemplar.second)
        }
        return reviewList
    }

    private fun getReviewsFromCurrentSite(url: String) {
        Log.d(TAG, "getReviewsFromCurrentSite $url")
        val reviewList: ArrayList<Review> = arrayListOf()
        val reviewDocument = Jsoup.connect("$url/opinion/").get()
        val reviewElements = reviewDocument.select("div.ow-opinion.ow-opinions__item")
        var starCount: Int
        var reviewText: String
        var review : Review
        for (reviewElement in reviewElements) {
            reviewText = reviewElement.select("div.ow-opinion__texts").text()
            starCount = getStarCount(reviewElement)
            review = Review(starCount, reviewText)
            reviewList.add(review)
            emitter.onNext(review)
        }
//        Log.d(TAG, "Review getted = ${reviewList.size}")
    }

    private fun getStarCount(element: Element): Int {
        val stars = element.select("span[data-state]")
        var count = 0
        var starCount = 0
        for (star in stars) {
            count++
            val state = star.attr("data-state").toString()
            Log.d(TAG, "star state = ${state} stars size = ${stars.size}")
            if (state.contains("s"))
                starCount++
            if (count >= 5) {
                Log.d(TAG, "Star count = $starCount")
                return starCount
            }
        }
        return starCount
    }


}