package com.example.aona2.mytimetabletest

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    val lecText = Array(6, { arrayOfNulls<TextView?>(5)})
    private var periodText : Array<TextView?> = arrayOfNulls(6)
    val daysText : Array<TextView?> = arrayOfNulls(5)
    private var LinearArray: Array<LinearLayout?> = arrayOfNulls(6)
    var textView: TextView? = null

    private lateinit var realm: Realm

    private val periodArray = arrayOf(900, 1040, 1300, 1440, 1615, 1800)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        realm = Realm.getDefaultInstance()

        setView()
    }

    private fun setView(){
        //レイアウトパラメータの宣言
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f)
        val halfParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 0.5f)
        val linearParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f)
        val linearHalfParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.5f)
        val linearWrapPrams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 0.5f)

        //外枠の設定
        val drawable = GradientDrawable()
        drawable.setStroke(1, Color.BLACK)

        //LinearLayoutの設定
        LinearArray[0] = LinearLayout(this)
        LinearArray[0]?.orientation = LinearLayout.VERTICAL
        LinearArray[0]?.layoutParams = linearHalfParams
        horizonLinear?.addView(LinearArray[0])
        for(i in 1..5) {
            LinearArray[i] = LinearLayout(this)
            LinearArray[i]?.orientation = LinearLayout.VERTICAL
            LinearArray[i]?.layoutParams = linearParams
            horizonLinear?.addView(LinearArray[i])
        }

        //1列目：時限と時間の表示
        textView = TextView(this)
        textView?.setBackground(drawable)
        textView?.layoutParams = halfParams
        LinearArray[0]?.addView(textView)
        for(i in 0..5) {
            periodText[i] = TextView(this)
            val time = periodArray[i]
            periodText[i]?.text = (i+1).toString() + "限\n" + (time/100).toString() + "時\n" + (time%100).toString() + "分"
            periodText[i]?.layoutParams = params
            periodText[i]?.setBackground(drawable)
            LinearArray[0]?.addView(periodText[i])
        }

        //1行目:曜日の表示
        val days_string = resources?.getStringArray(R.array.Days)
        for(i in 0..4){
            daysText[i] = TextView(this)
            if(days_string != null) daysText[i]?.text = days_string[i]
            daysText[i]?.layoutParams = halfParams
            daysText[i]?.setBackground(drawable)
            LinearArray[i+1]?.addView(daysText[i])
        }

        //授業コマの表示
        for(i in 0..4){
            for(j in 0..5){
                lecText[j][i] = TextView(this)
                realm = Realm.getDefaultInstance()
                val lecture = realm.where<Lecture>().equalTo("youbi", dayToYoubi(i))
                    .equalTo("period", j).findFirst()
                if(lecture != null) {
                    lecText[j][i]?.text = lecture?.name + "\n" +
                            "授業数:" + lecture?.lectureNum.toString() + "\n" +
                            "出席数:" + lecture?.attend.toString()
                }
                lecText[j][i]?.setBackground(drawable)
                lecText[j][i]?.layoutParams = params
                LinearArray[i+1]?.addView(lecText[j][i])
                //クリックの処理
                lecText[j][i]?.setOnClickListener{
                    val intent = Intent(it.context, LecEditActivity::class.java)
                    intent.putExtra("period", j)
                    intent.putExtra("day", i)
                    startActivity(intent)
                }
            }
        }
    }

    private fun dayToYoubi(day: Int): Int{
        if(day == 6) return 0
        else return day+ 1
    }

    override fun onRestart() {
        super.onRestart()
        reload()
    }

    private fun reload(){
        val intent = intent
        overridePendingTransition(0, 0)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        finish()

        overridePendingTransition(0, 0)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}
