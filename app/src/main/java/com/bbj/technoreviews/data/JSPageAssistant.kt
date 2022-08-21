package com.bbj.technoreviews.data

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.webkit.*


class JSPageAssistant(
    context: Context
) {

    var onReceive: ((String) -> Unit)? = null

    private val browser = WebView(context).apply {
        visibility = View.INVISIBLE
        setLayerType(View.LAYER_TYPE_NONE, null)
        settings.javaScriptEnabled = true
        settings.blockNetworkImage = true
        settings.domStorageEnabled = true
        settings.cacheMode = WebSettings.LOAD_NO_CACHE
        settings.loadsImagesAutomatically = false
        settings.setGeolocationEnabled(false)
        settings.setSupportZoom(false)

        addJavascriptInterface(JSHandler(), "JSHandler")

        webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                view?.scrollTo(0,(view.contentHeight*(0.8).toInt()))
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView, url: String) {
                Handler(Looper.getMainLooper()).post {
                    if (view.progress > 90) {
                            Log.d("JSHANDLER","PROGRESS = ${view.progress}")
                            Handler(Looper.getMainLooper()).postDelayed({
                                loadUrl("javascript:window.JSHandler.getPage('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');")
                            },3000)
                    }
                }
            }
        }
    }

    fun requestHTML(url: String) {
        Handler(Looper.getMainLooper()).post {
            browser.loadUrl(url)
        }
    }

    inner class JSHandler {
        @JavascriptInterface
        fun getPage(html: String) {
            onReceive?.let { it(html) }
        }
    }
}