package com.bbj.technoreviews.data.modeks

sealed class ResultStates {

    class Success(val preview : Preview, val reviews : ArrayList<Review>) : ResultStates()

    object Error : ResultStates()

}