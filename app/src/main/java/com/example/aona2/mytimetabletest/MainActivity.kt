package com.example.aona2.mytimetabletest

import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceManager
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

inline fun <T : View> T.afterMeasured(crossinline f: T.() -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (measuredWidth > 0 && measuredHeight > 0) {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                f()
            }
        }
    })
}

class MainActivity : AppCompatActivity(), TimePickerDialog.OnTimeSetListener{
    private val lecText = Array(6) { arrayOfNulls<TextView?>(5)}
    private val lecLinear = Array(6) { arrayOfNulls<LinearLayout?>(5)}
    private val periodNumText : Array<TextView?> = arrayOfNulls(6)
    private val periodTimeText : Array<TextView?> = arrayOfNulls(6)
    private val periodLinear : Array<LinearLayout?> = arrayOfNulls(6)
    private val daysText : Array<TextView?> = arrayOfNulls(5)
    private val linearArray: Array<LinearLayout?> = arrayOfNulls(6)
    private var textView: TextView? = null

    private lateinit var realm: Realm
    private lateinit var preference: Preference
    private lateinit var alarm: Alarm

    private var timePickerIndex = -1

    private val drawable = GradientDrawable()

    private val MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 1

    private val MIN_TEXT_SIZE = 10f


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        preference = Preference(pref)

        realm = Realm.getDefaultInstance()
        setView()

        if(preference.isAlarm != false)
            if(CheckPermission(this).checkPermission()) setAlarm()
    }

    private fun setView(){
        //レイアウトパラメータの宣言
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f)
        val halfParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 0.5f)
        val linearParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f)
        val linearHalfParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.5f)
        //private val linearWrapPrams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 0.5f)

        //外枠の設定
        drawable.setStroke(1, Color.BLACK)
        drawable.setColor(Color.WHITE)

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
            periodLinear[i] = LinearLayout(this)
            periodLinear[i]?.orientation = LinearLayout.VERTICAL
            periodLinear[i]?.layoutParams = params
            periodLinear[i]?.background = drawable
            periodLinear[i]?.tag = i
            periodLinear[i]?.setOnClickListener {
                val timePickerFragment = TimePickerFragment(this, preference, i)
                timePickerIndex = it.tag.toString().toInt()
                timePickerFragment.show(supportFragmentManager, "timePicker")
            }
            linearArray[0]?.addView(periodLinear[i])

            periodNumText[i] = TextView(this)
            periodNumText[i]?.text = (i+1).toString()
            periodNumText[i]?.textSize = 28.0f
            periodNumText[i]?.gravity = Gravity.CENTER
            periodNumText[i]?.setPadding(10,10,10,10)
            periodNumText[i]?.layoutParams = params
            periodLinear[i]?.addView(periodNumText[i])

            periodTimeText[i] = TextView(this)
            val time = preference.periodArray[i]
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, time / 100)
            cal.set(Calendar.MINUTE, time % 100)
            val dateFormat = DateFormat.format("HH:mm", cal)
            periodTimeText[i]?.text = dateFormat
            periodTimeText[i]?.textSize = 12.0f
            periodTimeText[i]?.gravity = Gravity.CENTER
            periodTimeText[i]?.setPadding(10,10,10,10)
            periodTimeText[i]?.layoutParams = halfParams
            periodLinear[i]?.addView(periodTimeText[i])
        }

        //1行目:曜日の表示
        val daysString = resources?.getStringArray(R.array.Days)
        for(i in 0..4){
            daysText[i] = TextView(this)
            if(daysString != null) daysText[i]?.text = daysString[i]
            daysText[i]?.textSize = 20.0f
            daysText[i]?.gravity = Gravity.CENTER
            //daysText[i]?.setPadding(10,10,10,10)
            daysText[i]?.layoutParams = halfParams
            daysText[i]?.background = drawable
            linearArray[i+1]?.addView(daysText[i])
        }

        //授業コマの表示
        val colorList = resources?.getIntArray(R.array.color_list)
        var colorIndex = 0
        for(i in 0..4){
            for(j in 0..5){
                val drawable = GradientDrawable()
                drawable.setStroke(1, Color.BLACK)
                drawable.setColor(Color.WHITE)
                lecText[j][i] = TextView(this)
                realm = Realm.getDefaultInstance()
                val lecture = realm.where<Lecture>().equalTo("youbi", MyCalendar().dayToYoubi(i))
                    .equalTo("period", j).findFirst()
                if(lecture != null) {                //授業が存在している場合の処理
                    lecText[j][i]?.text = lecture.name + "\n" +
                            "授業数:" + lecture.lectureNum.toString() + "\n" +
                            "出席数:" + lecture.attend.toString()
                    if(colorList != null)
                    drawable.setColor(colorList[colorIndex])
                    colorIndex += 1
                    if(colorIndex == colorList?.size) colorIndex = 0
                }
                lecText[j][i]?.textSize = 13.5f
                lecText[j][i]?.gravity = Gravity.CENTER

                lecText[j][i]?.background = drawable
                //lecText[j][i]?.setPadding(10,10,10,10)

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

        for(i in 0..5) {
            periodTimeText[i]?.afterMeasured {
                changeTextSize(periodTimeText[i])
            }
        }
    }

    private fun changeTextSize(textView: TextView?){
        if(textView != null){
            val viewWidth = textView.width
            Log.d("viewWidth", viewWidth.toString())
            val paint = Paint()
            var textSize = textView.textSize
            paint.textSize = textSize
            var textWidth = paint.measureText(textView.text.toString())
            while(viewWidth < textWidth){
                if(MIN_TEXT_SIZE >= textSize){
                    textSize = MIN_TEXT_SIZE
                    break
                }
                textSize -= 0.5f
                paint.textSize = textSize
                textWidth = paint.measureText(textView.text.toString())
            }
            Log.d("textSize", textSize.toString())
            textView.textSize = textSize
        }
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
        alarm = Alarm(preference.periodArray, this)
        //デバッグ用　n分後にアラームを設定する
        //alarm.minAfter(1)
        Toast.makeText(applicationContext, "出席管理をセットしました", Toast.LENGTH_SHORT).show()
    }

    private fun cancelAlarm(){
        alarm.cancelAlarm()
        Toast.makeText(applicationContext, "出席管理を解除しました", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.setting, menu)
        setAlarmMenu(menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        setAlarmMenu(menu)
        return super.onPrepareOptionsMenu(menu)
    }

    private fun setAlarmMenu(menu: Menu?){
        val item = menu?.getItem(0)
        if(preference.isAlarm != false) {
            item?.title = "アラームを解除する"
            item?.icon = ResourcesCompat.getDrawable(resources, android.R.drawable.ic_lock_idle_alarm, null)
        }
        else{
            item?.title = "アラームをセットする"
            item?.icon = ResourcesCompat.getDrawable(resources, android.R.drawable.ic_delete, null)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.alarmMenu -> {
                if(preference.isAlarm != false){
                    preference.putIsAlarm(false)
                    cancelAlarm()
                    invalidateOptionsMenu()
                }else{
                    preference.putIsAlarm(true)
                    setAlarm()
                    invalidateOptionsMenu()
                }
                return true
            }
            R.id.schoolLocationMenu -> {
                val intent = Intent(this, SchoolLocationActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.periodMenu -> {
                preference.periodInit()
                reload()
                Toast.makeText(applicationContext, "授業時間を初期化しました", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.lectureMenu-> {
                val realmResults = realm.where(Lecture::class.java)
                    .findAll()
                realm.executeTransaction {
                    realmResults.deleteAllFromRealm()
                }
                reload()
                Toast.makeText(applicationContext, "すべての授業を削除しました", Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //timePickerFragmentのlistener
    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        if(timePickerIndex != -1){
            preference.putPeriod(timePickerIndex, hourOfDay, minute)
            reload()
        }
    }

    //requestLocationPermissionのlistener
    override fun onRequestPermissionsResult(requestCode: Int, permissions:
    Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION->{
                if (permissions.isNotEmpty() && grantResults[0] == PackageManager.
                    PERMISSION_GRANTED) {
                    // 許可された
                    setAlarm()
                } else {
                    Toast.makeText(applicationContext, "アラームはセット出来ませんでした", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
