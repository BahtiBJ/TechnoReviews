package com.bbj.technoreviews

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bbj.technoreviews.data.DNSParser
import com.bbj.technoreviews.data.DNSParserAnotherTags
import com.bbj.technoreviews.data.modeks.ResultStates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val textView = findViewById<TextView>(R.id.textView)
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.Default) {
            val timeStart = System.currentTimeMillis()
            val result2 = DNSParser.getResult("vivo hp2033") as ResultStates.Success
            val timeEnd = System.currentTimeMillis() - timeStart
            Log.d("MAIN","Time tag = $timeEnd")
            val timeStart1 = System.currentTimeMillis()
            val result = DNSParserAnotherTags.getResult("vivo hp2033") as ResultStates.Success
            val timeEnd1 = System.currentTimeMillis() - timeStart1
            Log.d("MAIN","Time selector = $timeEnd1")
            withContext(Dispatchers.Main) {
                textView.setText(result.preview.toString() + " " + result.reviews[1].toString())
            }
        }

    }
}