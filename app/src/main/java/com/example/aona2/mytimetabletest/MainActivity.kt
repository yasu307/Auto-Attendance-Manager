package com.example.aona2.mytimetabletest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setView()
    }

    private fun setView(){
        //val textLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val textView: Array<TextView?> = arrayOfNulls(7)
        for(i in 0..6) {
            textView[i] = TextView(this)
            textView[i]?.text = "test ${i}"
            //textView[i]?.layoutParams = textLayoutParams
            Timetable.addView(textView[i])
        }

    }
}
