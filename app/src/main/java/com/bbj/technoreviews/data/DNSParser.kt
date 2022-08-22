package com.bbj.technoreviews.data

import com.bbj.technoreviews.data.models.Review
import com.bbj.technoreviews.data.models.Sample
import com.bbj.technoreviews.domain.Parser
import io.reactivex.rxjava3.core.ObservableEmitter
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

class DNSParser : Parser {

    private val TAG = "DNSParser"
    private val baseUrl = "https://www.dns-shop.kz/search/?order=4&q="

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
                            .replace(" ", "+")
            )
                .get()
        val parsedElements = document.select("div[data-id=\"product\"]")
        val result = Elements()
        for (element in parsedElements) {
            if (haveReviews(element))
                result.add(element)
        }
        return result

    }

    private fun haveReviews(element: Element): Boolean {
        val ratingElement = element.select("a[data-rating]")
        try {
            ratingElement
                .text().toInt().let {
                    if (it == 0)
                        return false
                }
        } catch (e: Exception) {
            return false
        }
        return true
    }


    private fun getSample(element: Element): Sample {
        val previewImage = element.getElementsByTag("img").attr("data-src")
        val productName =
            element.select("a.catalog-product__name").text()
        val ratingElement = element.select("a[data-rating]")
        val reviewCount = ratingElement.text().toInt()
        val rating =
            try {
                ratingElement.attr("data-rating").toFloat()
            } catch (e: Exception) {
                0.0.toFloat()
            }
        return Sample(Shop.DNS, previewImage, productName, rating, reviewCount)
    }


    private fun getReviews(
        element: Element,
        reviewListEmitter: ObservableEmitter<Review>
    ): ArrayList<Review> {
        val url = element.select("a[href]")
            .attr("abs:href")
        val reviewList: ArrayList<Review> = arrayListOf()
        val reviewDocument = Jsoup.connect("$url/opinion/").get()
        val reviewElements = reviewDocument.select("div.ow-opinion.ow-opinions__item")
        var starCount: Int
        var reviewText: String
        var review: Review
        for (reviewElement in reviewElements.subList(1, reviewElements.size)) {
            reviewText = reviewElement.select("div.ow-opinion__texts").text()
                .replace("\\WНедостатки".toRegex(), "\n\nНедостатки")
                .replace("\\WКомментари\\w".toRegex(), "\n\nКомментарий")

            starCount = getStarCount(reviewElement)
            review = Review(starCount, reviewText)
            reviewList.add(review)
            reviewListEmitter.onNext(review)
        }
        return reviewList
    }

    private fun getStarCount(element: Element): Int {
        val stars = element.select("span[data-state]")
        var count = 0
        var starCount = 0
        for (star in stars) {
            count++
            val state = star.attr("data-state").toString()
            if (state.contains("s"))
                starCount++
            if (count >= 5) {
                return starCount
            }
        }
        return starCount
    }


}