package com.bbj.technoreviews

import com.bbj.technoreviews.data.modeks.ResultStates

interface Parser {

    fun getResult(searchRequest : String) : ResultStates


}