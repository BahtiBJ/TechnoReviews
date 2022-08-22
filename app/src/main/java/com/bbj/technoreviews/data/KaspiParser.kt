package com.bbj.technoreviews.data

import com.bbj.technoreviews.data.models.Review
import com.bbj.technoreviews.data.models.Sample
import com.bbj.technoreviews.domain.Parser
import io.reactivex.rxjava3.core.ObservableEmitter
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

class KaspiParser(private val jsPageAssistant: JSPageAssistant) : Parser {

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
            for (i in 0 until productList!!.size) {
                getSample(productList!![i]).let {
                    sampleEmitter.onNext(it)
                }
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
                val url = it.select("a[href]")
                    .attr("abs:href")
                jsPageAssistant.onReceive = {html ->
                    getReviews(position,html, reviewListEmitter)
                }
                jsPageAssistant.requestHTML(url)
            } catch (e: Exception) {
                reviewListEmitter.onError(e)
            }
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
            return false
        }
        return true
    }


    private fun getSample(element: Element): Sample {
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
                0.0.toFloat()
            }
        return Sample(Shop.KASPI, previewImage, productName, rating, reviewCount)
    }


    private fun getReviews(
        position: Int,
        html : String,
        reviewListEmitter: ObservableEmitter<Review>
    ) {
        val reviewList: ArrayList<Review> = arrayListOf()
        val reviewDocument = Jsoup.parse(html)
        val reviewElements = reviewDocument.select("a.reviews__review.g-bb-thin")
        if (reviewElements.size != 0) {
            var starCount: Int
            var reviewText: String
            var review: Review
            for (reviewElement in reviewElements.subList(1, reviewElements.size)) {
                reviewText = getText(reviewElement.select("p.reviews__review-text_paragraph"))
                starCount = reviewElement
                    .select("div.rating")
                    .attr("class")
                    .filter { it.isDigit() }
                    .toInt() / 2
                review = Review(starCount, reviewText)
                reviewList.add(review)
                reviewListEmitter.onNext(review)
            }
            reviewMap.put(position,reviewList)
        }
        reviewListEmitter.onComplete()
    }

    private fun getText(elements: Elements) : String{
        var reviewText = ""
        for (i in 0 until elements.size){
            reviewText += elements[i].text() +
                    if (i != elements.size - 1) "\n\n" else ""
        }
        return reviewText
    }
}