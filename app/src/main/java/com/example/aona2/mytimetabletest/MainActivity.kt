package com.example.aona2.mytimetabletest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    val lecText = Array(6, { arrayOfNulls<TextView?>(5)})
    private var periodText : Array<TextView?> = arrayOfNulls(6)
    val daysText : Array<TextView?> = arrayOfNulls(5)
    var textView: TextView? = null

    private val periodArray = arrayOf(900, 1040, 1300, 1440, 1615, 1800)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setView()
    }

    private fun setView(){
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f)
        val halfParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 0.5f)
        textView = TextView(this)
        textView?.layoutParams = halfParams
        periodLinear.addView(textView)
        for(i in 0..5) {
            periodText[i] = TextView(this)
            val time = periodArray[i]
            //periodText[i]?.text = "test"
            periodText[i]?.text = (i+1).toString() + "限\n" + (time/10).toString() + ":" + (time%10).toString()
            periodText[i]?.layoutParams = params
            periodLinear.addView(periodText[i])
        }



        /*
        val textLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f)
        val textView: Array<TextView?> = arrayOfNulls(7)
        for(i in 0..6) {
            textView[i] = TextView(this)
            textView[i]?.text = "test ${i}"
            textView[i]?.layoutParams = textLayoutParams
            Timetable.addView(textView[i])


         */


    }
}
