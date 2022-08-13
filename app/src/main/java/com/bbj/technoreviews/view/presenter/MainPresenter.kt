package com.bbj.technoreviews.view.presenter

import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.bbj.technoreviews.data.ReviewsRepositoryImpl
import com.bbj.technoreviews.data.modeks.Preview
import com.bbj.technoreviews.data.modeks.Review
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

@InjectViewState
class MainPresenter : MvpPresenter<MainViewState>() {

    val TAG = "PRESENTER"

    private val repository = ReviewsRepositoryImpl()

    fun getObservablePreviews(searchRequest: String) {
        Log.d(TAG, "get observable preview $searchRequest")
        repository.getPreviewList(searchRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<Preview> {
                override fun onSubscribe(d: Disposable) {}

                override fun onNext(preview: Preview) {
                    viewState.addToList(preview)
                }

                override fun onError(e: Throwable) {
                    throw e
                }

                override fun onComplete() {
                    viewState.onComplete()
                }
            })

    }

    fun getObservableReviews() {
        Log.d(TAG, "get observable review")
        repository.getReviewList()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<Review> {
                override fun onSubscribe(d: Disposable) {}

                override fun onNext(review: Review) {
                    viewState.addToList(review)
                }

                override fun onError(e: Throwable) {
//                    viewState.showError(e.localizedMessage)
                    throw e

                }

                override fun onComplete() {
                    viewState.onComplete()
                }
            })

    }

}