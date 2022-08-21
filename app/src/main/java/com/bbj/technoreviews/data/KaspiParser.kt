package com.bbj.technoreviews.data

import android.util.Log
import com.bbj.technoreviews.data.models.Review
import com.bbj.technoreviews.data.models.Sample
import com.bbj.technoreviews.domain.Parser
import io.reactivex.rxjava3.core.ObservableEmitter
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

class KaspiParser : Parser {

    private val TAG = "KaspiParser"
    private val baseUrl = "https://kaspi.kz/shop/search/?text="

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
            Log.d(TAG, "get previews productList size = ${productList!!.size}")
            for (i in 0 until productList!!.size) {
                getSample(productList!![i]).let {
                    Log.d(TAG,"Sample = $it")
                    sampleEmitter.onNext(it)
                }
            }
        } catch (e: Exception) {
            Log.d(TAG,"On sample error")
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
                reviewMap.put(position, getReviews(it, reviewListEmitter))
            } catch (e: Exception) {
                reviewListEmitter.onError(e)
            }
            reviewListEmitter.onComplete()
        }
    }

    private fun parseProductList(searchRequest: String): Elements {
        val document =
            Jsoup.connect(
                baseUrl +
                        searchRequest
                            .trim()
                            .replace(" ", "%20")
            )
                .get()
        Log.d(TAG, "get result")
        val parsedElements = document.select("div[data-product-id]")
        val result = Elements()
        for (element in parsedElements) {
            if (haveReviews(element))
                result.add(element)
        }
        return result

    }

    private fun haveReviews(element: Element): Boolean {
        val ratingElement = element.select("div.item-card__rating")
        try {
            ratingElement.select("span.rating")
                .attr("class")
                .filter { it.isDigit() }
                .toInt().let {
                    if (it == 0)
                        return false
                }
        } catch (e: Exception) {
            Log.d(TAG, "ClassCastException")
            return false
        }
        return true
    }


    private fun getSample(element: Element): Sample {
        Log.d(TAG, "get preview")
        val previewImage = element.select("img.item-card__image").attr("src")
        val productName =
            element.select("a[title]").attr("title")
        val ratingElement = element.select("div.item-card__rating")
        val reviewCount = ratingElement
            .select("a[href]")
            .text()
            .filter { it.isDigit() }
            .toInt()
        val rating =
            try {
                ratingElement.select("span.rating")
                    .attr("class")
                    .filter { it.isDigit() }
                    .toFloat()/2
            } catch (e: Exception) {
                Log.d(TAG, "ClassCastException")
                0.0.toFloat()
            }
        return Sample(Shop.KASPI, previewImage, productName, rating, reviewCount)
    }


    private fun getReviews(
        element: Element,
        reviewListEmitter: ObservableEmitter<Review>
    ): ArrayList<Review> {
        val url = element.select("a[href]")
            .attr("abs:href")
        Log.d(TAG, "getReviewsFromCurrentSite $url")
        val reviewList: ArrayList<Review> = arrayListOf()
        val reviewDocument = Jsoup.connect("$url").get()
        val reviewElements = reviewDocument.select("div.reviews__review")
        if (reviewElements.size != 0) {
            var starCount: Int
            var reviewText: String
            var review: Review
            for (reviewElement in reviewElements.subList(1, reviewElements.size)) {
                reviewText = reviewElement.select("div.reviews__review-text").text()
                    .replace("Недостатки", "\n\nНедостатки")
                    .replace("Комментарии", "\n\nКомментарий")

                starCount = reviewElement
                    .select("div.rating")
                    .attr("class")
                    .filter { it.isDigit() }
                    .toInt() / 2
                review = Review(starCount, reviewText)
                reviewList.add(review)
                reviewListEmitter.onNext(review)
            }
        }
        return reviewList
    }
}