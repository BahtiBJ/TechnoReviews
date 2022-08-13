package com.bbj.technoreviews.view.presenter

import com.arellomobile.mvp.MvpView
import com.bbj.technoreviews.data.modeks.ResultType
import io.reactivex.rxjava3.core.Observable

interface MainViewState : MvpView {

    fun onNewSubscribe()

    fun addToList(result : ResultType)

    fun showError(error : String)

    fun onComplete()

}