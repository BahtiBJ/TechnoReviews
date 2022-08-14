package com.bbj.technoreviews.view.presenter

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.*
import com.bbj.technoreviews.data.modeks.ResultType
import io.reactivex.rxjava3.core.Observable

interface ReviewView : MvpView {

    fun onNewSubscribe()

    fun addToList(result : ResultType)

    @StateStrategyType(SkipStrategy::class)
    fun showError(error : String)

    @StateStrategyType(SkipStrategy::class)
    fun onComplete()

}