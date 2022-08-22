package com.bbj.technoreviews.view.presenter

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.*
import com.bbj.technoreviews.data.models.Review

interface ReviewView : MvpView {

    fun onNewSubscribe()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun addToList(review : Review)

    @StateStrategyType(SkipStrategy::class)
    fun showError()

    @StateStrategyType(SkipStrategy::class)
    fun onComplete()

}