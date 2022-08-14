package com.bbj.technoreviews.view.presenter

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.*
import com.bbj.technoreviews.data.modeks.ResultType
import io.reactivex.rxjava3.core.Observable

interface SampleView : MvpView {

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onNewSubscribe()

//    @StateStrategyType(OneExecutionStateStrategy::class)
    fun addToList(result : ResultType)

    @StateStrategyType(SkipStrategy::class)
    fun showError(error : String)

    @StateStrategyType(SkipStrategy::class)
    fun onComplete()

}