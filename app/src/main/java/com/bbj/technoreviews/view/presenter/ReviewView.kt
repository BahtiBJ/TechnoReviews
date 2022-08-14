package com.bbj.technoreviews.view.presenter

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.*
import com.bbj.technoreviews.data.modeks.ResultType
import com.bbj.technoreviews.data.modeks.Review
import io.reactivex.rxjava3.core.Observable

interface ReviewView : MvpView {

    fun onNewSubscribe()

    fun addToList(review : Review)

    @StateStrategyType(SkipStrategy::class)
    fun showError(error : String)

    @StateStrategyType(SkipStrategy::class)
    fun onComplete()

}