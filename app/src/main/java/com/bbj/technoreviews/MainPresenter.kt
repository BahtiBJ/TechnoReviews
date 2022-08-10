package com.bbj.technoreviews

import com.arellomobile.mvp.MvpPresenter
import com.bbj.technoreviews.data.DNSParser
import com.bbj.technoreviews.data.modeks.ResultType
import io.reactivex.rxjava3.core.Observable

class MainPresenter : MvpPresenter<MainViewState>() {

    fun getObservableDNSReviews(searchRequest : String) : Observable<ResultType>{
        return Observable.create {
            DNSParser.getResult(searchRequest,it)
        }
    }


}