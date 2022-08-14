package com.bbj.technoreviews.data.modeks

import com.bbj.technoreviews.data.Shop

data class Sample (
    val shopName: Shop,
    val previewImageUrl: String,
    val productName : String,
    val rating : Float) : ResultType