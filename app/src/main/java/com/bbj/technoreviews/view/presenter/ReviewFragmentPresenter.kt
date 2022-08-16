package com.bbj.technoreviews.view.presenter

import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.bbj.technoreviews.data.ReviewsRepositoryImpl
import com.bbj.technoreviews.data.Shop
import com.bbj.technoreviews.data.models.Review
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

@InjectViewState
class ReviewFragmentPresenter : MvpPresenter<ReviewView>() {

    val TAG = "PRESENTER"

    private val savedReviews = hashMapOf<Int,Review>()

    fun getObservableReviews(position : Int,shopName : Shop) {
        Log.d(TAG, "get observable review")
        ReviewsRepositoryImpl.getReviewList(position,shopName)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<Review> {
                override fun onSubscribe(d: Disposable) {
                    savedReviews.clear()
                    viewState.onNewSubscribe()
                }

                override fun onNext(review: Review) {
                    viewState.addToList(review)
                }

                override fun onError(e: Throwable) {
                    viewState.showError(e.localizedMessage!!)
                    throw e

                }

                override fun onComplete() {
                    viewState.onComplete()
                }
            })
    }

}