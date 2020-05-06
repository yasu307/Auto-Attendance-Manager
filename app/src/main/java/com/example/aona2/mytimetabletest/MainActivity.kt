package com.example.aona2.mytimetabletest

import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
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

class MainActivity : AppCompatActivity(), TimePickerDialog.OnTimeSetListener{
    private val lecText = Array(6) { arrayOfNulls<TextView?>(5)}
    private val periodText : Array<TextView?> = arrayOfNulls(6)
    private val daysText : Array<TextView?> = arrayOfNulls(5)
    private val linearArray: Array<LinearLayout?> = arrayOfNulls(6)
    private var textView: TextView? = null

    private lateinit var realm: Realm
    private lateinit var preference: Preference
    private lateinit var alarm: Alarm

    private var timePickerIndex = -1

    private val drawable = GradientDrawable()

    private val MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        preference = Preference(pref)

        realm = Realm.getDefaultInstance()
        setView()

        if(preference.isAlarm != false) {
            if(Build.VERSION.SDK_INT >= 23){
                if(CheckPermission(this).checkPermission()) setAlarm()
            }
            else setAlarm();
        }
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
            periodText[i] = TextView(this)
            val time = preference.periodArray[i]
            periodText[i]?.text = (i+1).toString() + "限\n" + (time/100).toString() + "時\n" + (time%100).toString() + "分"
            periodText[i]?.setPadding(10,10,10,10)
            periodText[i]?.layoutParams = params
            periodText[i]?.background = drawable
            periodText[i]?.tag = i
            periodText[i]?.setOnClickListener {
                val timePickerFragment = TimePickerFragment(this, preference, i)
                timePickerIndex = it.tag.toString().toInt()
                timePickerFragment.show(supportFragmentManager, "timePicker")
            }
            linearArray[0]?.addView(periodText[i])
        }

        //1行目:曜日の表示
        val daysString = resources?.getStringArray(R.array.Days)
        for(i in 0..4){
            daysText[i] = TextView(this)
            if(daysString != null) daysText[i]?.text = daysString[i]
            daysText[i]?.setPadding(10,10,10,10)
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
        val item = menu?.getItem(0)
        if(preference.isAlarm != false){
            item?.title = "アラームを解除する"
            item?.icon = ResourcesCompat.getDrawable(resources, android.R.drawable.ic_lock_idle_alarm, null)
        }
        else{
            item?.title = "アラームをセットする"
            item?.icon = ResourcesCompat.getDrawable(resources, android.R.drawable.ic_delete, null)
        }
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val item = menu?.getItem(0)
        if(preference.isAlarm != false) {
            item?.title = "アラームを解除する"
            item?.icon = ResourcesCompat.getDrawable(resources, android.R.drawable.ic_lock_idle_alarm, null)
        }
        else{
            item?.title = "アラームをセットする"
            item?.icon = ResourcesCompat.getDrawable(resources, android.R.drawable.ic_delete, null)
        }
        return super.onPrepareOptionsMenu(menu)
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
