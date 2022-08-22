package com.bbj.technoreviews.view.presenter

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.*
import com.bbj.technoreviews.data.models.Sample

interface SampleView : MvpView {

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onNewSubscribe()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun addToList(sample : Sample)

    @StateStrategyType(SkipStrategy::class)
    fun showError()

    fun onComplete()

}