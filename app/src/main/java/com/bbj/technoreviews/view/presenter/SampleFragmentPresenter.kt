package com.bbj.technoreviews.view.presenter

import android.content.Context
import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.bbj.technoreviews.data.ReviewsRepositoryImpl
import com.bbj.technoreviews.data.models.Sample
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

@InjectViewState
class SampleFragmentPresenter : MvpPresenter<SampleView>() {

    private val TAG = "PRESENTER"

    private val repository = ReviewsRepositoryImpl.getInstance()!!

    fun getObservablePreviews(searchRequest: String) {
        repository.getSampleList(searchRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<Sample> {
                override fun onSubscribe(d: Disposable) {
                    viewState.onNewSubscribe()
                }

                override fun onNext(sample: Sample) {
                    viewState.addToList(sample)
                }

                override fun onError(e: Throwable) {
                    viewState.showError()
                }

                override fun onComplete() {
                    viewState.onComplete()
                }
            })

    }

}