package com.bbj.technoreviews.data.modeks

import android.net.Uri
import com.bbj.technoreviews.data.Shop

data class Preview (
    val shopName: Shop,
    val previewImageUrl: String,
    val productName : String,
    val reviewCount : Int) : ResultType