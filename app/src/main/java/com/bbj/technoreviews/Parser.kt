package com.bbj.technoreviews

import com.bbj.technoreviews.data.modeks.ResultStates
import com.bbj.technoreviews.data.modeks.ResultType
import io.reactivex.rxjava3.core.ObservableEmitter

interface Parser {

    fun getResult(searchRequest : String, emitter: ObservableEmitter<ResultType)

}