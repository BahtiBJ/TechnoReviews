package com.bbj.technoreviews.data

import android.util.Log
import com.bbj.technoreviews.Parser
import com.bbj.technoreviews.data.modeks.Preview
import com.bbj.technoreviews.data.modeks.ResultStates
import com.bbj.technoreviews.data.modeks.Review
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

object DNSParserAnotherTags : Parser {

    private val TAG = "DNSParser"

    private val baseUrl = "https://www.dns-shop.kz/search/?order=4&q="

    override fun getResult(searchRequest: String): ResultStates {
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
        val preview = getPreview(productList[0])
        val reviews = getReviews(productList)
        Log.d(TAG, "result getted")
        return ResultStates.Success(preview, reviews)
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

    private fun getReviews(elements: Elements): ArrayList<Review> {
        Log.d(TAG, "get Reviews")
        val reviewList: ArrayList<Review> = arrayListOf()
        var url = ""
        for (element in elements) {
            val reviewCount =
                try {
                    element.select("a[data-rating]")
                        .text().toInt()
                } catch (e: ClassCastException) {
                    Log.d(TAG, "ClassCastException")
                    0
                }
//            Log.d(TAG, "review count = $reviewCount elements size = ${elements.size}")
            if (reviewCount > 0) {
                url = element.select("a[href]")
                    .attr("abs:href")
                reviewList.addAll(getReviewsFromCurrentSite(url))
            }
        }
        return reviewList
    }

    private fun getReviewsFromCurrentSite(url: String): ArrayList<Review> {
        Log.d(TAG, "getReviewsFromCurrentSite $url")
        val reviewList: ArrayList<Review> = arrayListOf()
        val reviewDocument = Jsoup.connect("$url/opinion/").get()
        val reviewElements = reviewDocument.select("div.ow-opinion.ow-opinions__item")
        var starCount: Int
        var reviewText: String
        for (reviewElement in reviewElements) {
            reviewText = reviewElement.select("div.ow-opinion__texts").text()
            starCount = getStarCount(reviewElement)
            reviewList.add(Review(starCount, reviewText))
        }
//        Log.d(TAG, "Review getted = ${reviewList.size}")
        return reviewList
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
                Log.d(TAG,"Star count = $starCount")
                return starCount
            }
        }
        return starCount
    }


}