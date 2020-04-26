package com.example.aona2.mytimetabletest

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val lecText = Array(6) { arrayOfNulls<TextView?>(5)}
    private val periodText : Array<TextView?> = arrayOfNulls(6)
    private val daysText : Array<TextView?> = arrayOfNulls(5)
    private val linearArray: Array<LinearLayout?> = arrayOfNulls(6)
    private var textView: TextView? = null

    private lateinit var realm: Realm

    private var isAlarm = true

    private lateinit var preference: Preference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        preference = Preference(pref)

        realm = Realm.getDefaultInstance()
        setView()

        if(isAlarm) {
            setAlarm()
            //setAlarm(myCalendar.minAfter(2), 0)
        }
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
        linearArray[0] = LinearLayout(this)
        linearArray[0]?.orientation = LinearLayout.VERTICAL
        linearArray[0]?.layoutParams = linearHalfParams
        horizonLinear?.addView(linearArray[0])
        for(i in 1..5) {
            linearArray[i] = LinearLayout(this)
            linearArray[i]?.orientation = LinearLayout.VERTICAL
            linearArray[i]?.layoutParams = linearParams
            horizonLinear?.addView(linearArray[i])
        }

        //1列目：時限と時間の表示
        textView = TextView(this)
        textView?.background = drawable
        textView?.setPadding(10,10,10,10)
        textView?.layoutParams = halfParams
        linearArray[0]?.addView(textView)
        for(i in 0..5) {
            periodText[i] = TextView(this)
            val time = preference.periodArray[i] ?: 0
            periodText[i]?.text = (i+1).toString() + "限\n" + (time/100).toString() + "時\n" + (time%100).toString() + "分"
            periodText[i]?.setPadding(10,10,10,10)
            periodText[i]?.layoutParams = params
            periodText[i]?.background = drawable
            linearArray[0]?.addView(periodText[i])
        }

        //1行目:曜日の表示
        val days_string = resources?.getStringArray(R.array.Days)
        for(i in 0..4){
            daysText[i] = TextView(this)
            if(days_string != null) daysText[i]?.text = days_string[i]
            daysText[i]?.setPadding(10,10,10,10)
            daysText[i]?.layoutParams = halfParams
            daysText[i]?.background = drawable
            linearArray[i+1]?.addView(daysText[i])
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
                lecText[j][i]?.background = drawable
                lecText[j][i]?.setPadding(10,10,10,10)
                lecText[j][i]?.layoutParams = params
                linearArray[i+1]?.addView(lecText[j][i])
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

/*
//実際に使用する
    private fun dayToYoubi(day: Int): Int{
        val youbi = day + 2
        if(youbi == 8) return 1
        else return youbi
    }

 */
    //デバッグ用
    private fun dayToYoubi(day: Int): Int{
        return day + 1
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

    //アラームをセットする
    private fun setAlarm(){
        val alarm = Alarm(preference.periodArray)

        val notifyIntent = Intent(this, AttendActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        notifyIntent.putExtra("index", alarm.myCalendar.nextIndex)
        val notifyPendingIntent = PendingIntent.getActivity(
            this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager : AlarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        alarm.setAlarm(alarmManager, notifyPendingIntent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.setting, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.alarmMenu -> {
                if(isAlarm){
                    isAlarm = false
                    Toast.makeText(applicationContext, "出席管理を解除しました", Toast.LENGTH_SHORT).show()
                }else{
                    isAlarm = true
                    Toast.makeText(applicationContext, "出席管理を設定しました", Toast.LENGTH_SHORT).show()
                }
                return true
            }
            R.id.schoolLocationMenu -> {
                val intent = Intent(this, SchoolLocationActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.periodMenu -> {
                val intent = Intent(this, PeriodActivity::class.java)
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
