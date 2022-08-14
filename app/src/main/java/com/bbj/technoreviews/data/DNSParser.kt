package com.bbj.technoreviews.data

import android.util.Log
import com.bbj.technoreviews.data.modeks.Sample
import com.bbj.technoreviews.data.modeks.Review
import com.bbj.technoreviews.domain.Parser
import io.reactivex.rxjava3.core.ObservableEmitter
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

class DNSParser : Parser {

    private val TAG = "DNSParser"
    private val baseUrl = "https://www.dns-shop.kz/search/?order=4&q="

    private lateinit var sample: Sample
    private val reviewList: ArrayList<Sample> = arrayListOf()

    private var productList: Elements? = null

    override fun getSampleStream(
        searchRequest: String,
        sampleEmitter: ObservableEmitter<Sample>
    ) {
        try {
            productList = parseProductList(searchRequest)
            Log.d(TAG, "get previews")
            sample = getSample(productList!![0])
            sampleEmitter.onNext(sample)
        } catch (e: Exception) {
            sampleEmitter.onError(e)
        }
    }

    override fun getReviewStream(reviewListEmitter: ObservableEmitter<Review>) {
        productList?.let {
            try {
                getReviews(it, reviewListEmitter)
            } catch (e : Exception){
                reviewListEmitter.onError(e)
            }
        }
    }

    private fun parseProductList(searchRequest: String): Elements {
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
        return document.select("div[data-id=\"product\"]")

    }


    private fun getSample(element: Element): Sample {
        Log.d(TAG, "get preview")
        val previewImage = element.getElementsByTag("img").attr("data-src")
        val productName =
            element.select("a.catalog-product__name").text()
        val ratingElement = element.select("a[data-rating]")
        val reviewCount =
            try {
                element.select("a[data-rating]")
                    .text().toInt().let {
                        if (it == 0)
                            return Sample(Shop.DNS,"","",404f)
                    }
            } catch (e: Exception) {
                Log.d(TAG, "ClassCastException")
                404
            }
        val rating =
            try {
                ratingElement.attr("data-rating").toFloat()
            } catch (e: Exception) {
                Log.d(TAG, "ClassCastException")
                0.0.toFloat()
            }
        return Sample(Shop.DNS, previewImage, productName, rating)
    }

    private fun getReviews(
        elements: Elements, reviewListEmitter: ObservableEmitter<Review>
    ): ArrayList<Review> {
        Log.d(TAG, "get Reviews")
        val reviewList: ArrayList<Review> = arrayListOf()
        var exemplars: ArrayList<Pair<Int, String>> = arrayListOf()
        for (element in elements) {
            val reviewCount =
                try {
                    element.select("a[data-rating]")
                        .text().toInt()
                } catch (e: Exception) {
                    Log.d(TAG, "ClassCastException")
                    0
                }
            if (reviewCount > 0) {
                val url = element.select("a[href]")
                    .attr("abs:href")
                exemplars.add(Pair(reviewCount, url))
            }
        }
        if (exemplars.size > 0){
            if (exemplars.size > 3) {
                exemplars.sortByDescending { it.first }
                exemplars = arrayListOf(exemplars[0], exemplars[1], exemplars[2])
            }
            for (exemplar in exemplars) {
                getReviewsFromCurrentSite(exemplar.second, reviewListEmitter)
            }
        }
        return reviewList
    }

    private fun getReviewsFromCurrentSite(
        url: String,
        reviewListEmitter: ObservableEmitter<Review>
    ) {
        Log.d(TAG, "getReviewsFromCurrentSite $url")
        val reviewList: ArrayList<Review> = arrayListOf()
        val reviewDocument = Jsoup.connect("$url/opinion/").get()
        val reviewElements = reviewDocument.select("div.ow-opinion.ow-opinions__item")
        var starCount: Int
        var reviewText: String
        var review: Review
        for (reviewElement in reviewElements.subList(1,reviewElements.size)) {
            reviewText = reviewElement.select("div.ow-opinion__texts").text()
            if (reviewText.trim().isEmpty())
                continue
            starCount = getStarCount(reviewElement)
            review = Review(starCount, reviewText)
            reviewList.add(review)
            reviewListEmitter.onNext(review)
        }
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