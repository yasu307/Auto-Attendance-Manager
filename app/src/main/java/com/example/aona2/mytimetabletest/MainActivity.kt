package com.example.aona2.mytimetabletest

import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.format.DateFormat
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

class MainActivity : AppCompatActivity(), TimePickerDialog.OnTimeSetListener{
    private val lecText = Array(6) { arrayOfNulls<TextView?>(5)}
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //preferenceに保存してある数値にアクセスする際に使用する
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        preference = Preference(pref)

        realm = Realm.getDefaultInstance()

        setView()

        checkAlarm()
    }

    private fun setView(){
        //レイアウトパラメータの宣言
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f)
        val halfParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 0.5f)
        val linearParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f)
        val linearHalfParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.5f)
        //private val linearWrapPrams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 0.5f)

        drawable.setStroke(1, Color.BLACK)
        drawable.setColor(Color.WHITE)

        //LinearLayoutの設定　最初の縦6列
        //時限コマの列
        linearArray[0] = LinearLayout(this)
        linearArray[0]?.orientation = LinearLayout.VERTICAL
        linearArray[0]?.layoutParams = linearHalfParams
        horizonLinear?.addView(linearArray[0])
        //曜日、授業コマの列
        for(i in 1..5) {
            linearArray[i] = LinearLayout(this)
            linearArray[i]?.orientation = LinearLayout.VERTICAL
            linearArray[i]?.layoutParams = linearParams
            horizonLinear?.addView(linearArray[i])
        }

        //1列目：時限と時間の表示
        //一番左上の空白のコマ
        textView = TextView(ContextThemeWrapper(this, R.style.TextViewStroke))
        textView?.layoutParams = halfParams
        linearArray[0]?.addView(textView)
        //時限コマ 時限の数字と時限の開始時間を格納するためここでもLinearLayoutを作成する
        for(i in 0..5) {
            periodLinear[i] = LinearLayout(this)
            periodLinear[i]?.orientation = LinearLayout.VERTICAL
            periodLinear[i]?.layoutParams = params
            periodLinear[i]?.tag = i
            periodLinear[i]?.setOnClickListener {//時限コマをクリックしたときに時限の開始時間を設定するダイアログを表示
                val timePickerFragment = TimePickerFragment(this, preference.getPeriodArray(), i)
                timePickerIndex = it.tag.toString().toInt()
                timePickerFragment.show(supportFragmentManager, "timePicker")
            }
            linearArray[0]?.addView(periodLinear[i])
            //何時限目かの数字
            periodNumText[i] = TextView(ContextThemeWrapper(this, R.style.TextViewTrans))
            periodNumText[i]?.text = (i+1).toString()
            periodNumText[i]?.layoutParams = params
            periodLinear[i]?.addView(periodNumText[i])
            //時限の開始時間
            periodTimeText[i] = TextView(ContextThemeWrapper(this, R.style.TextViewTrans))
            val time = preference.getPeriodArray()[i]
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, time / 100)
            cal.set(Calendar.MINUTE, time % 100)
            val dateFormat = DateFormat.format("HH:mm", cal)
            periodTimeText[i]?.text = dateFormat
            periodTimeText[i]?.layoutParams = halfParams
            periodLinear[i]?.addView(periodTimeText[i])
            periodLinear[i]?.background = drawable
        }

        //1行目:曜日の表示
        val daysString = resources?.getStringArray(R.array.Days)
        for(i in 0..4){
            daysText[i] = TextView(ContextThemeWrapper(this, R.style.TextViewStroke))
            if(daysString != null) daysText[i]?.text = daysString[i]
            daysText[i]?.layoutParams = halfParams
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
                if(lecture != null) {                //授業が存在している場合にテキストを表示する
                    lecText[j][i]?.text = lecture.name + "\n" +
                            "授業数:" + lecture.lectureNum.toString() + "\n" +
                            "出席数:" + lecture.attend.toString()
                        /*
                    val trans = resources.getColor(R.color.TRANS)
                    periodTimeText[i]?.setBackgroundColor(trans)

                         */
                    //授業が存在しているコマは色を付ける
                    if(colorList != null) drawable.setColor(colorList[colorIndex])
                    colorIndex += 1
                    if(colorIndex == colorList?.size) colorIndex = 0
                }
                lecText[j][i]?.setPadding(10,10,10,10)
                lecText[j][i]?.layoutParams = params
                lecText[j][i]?.background = drawable
                linearArray[i+1]?.addView(lecText[j][i])

                //授業コマをクリックした場合の処理
                lecText[j][i]?.setOnClickListener{
                    val intent = Intent(it.context, LecEditActivity::class.java)
                    intent.putExtra("period", j)
                    intent.putExtra("day", i)
                    startActivity(intent)
                }
            }
        }
    }

    //isAlarmを確認しtrueならsetAlarm()を実行
    private fun checkAlarm(){
        if(preference.getIsAlarm())
            if(CheckPermission(this).checkPermission()) setAlarm()
    }

    //アラームをセットする
    private fun setAlarm(){
        alarm = Alarm(preference.getPeriodArray(), this)
        //デバッグ用　n分後にアラームを設定する
        //alarm.minAfter(1)
        Toast.makeText(applicationContext, "出席管理をセットしました", Toast.LENGTH_SHORT).show()
    }

    //アラームをキャンセルする
    private fun cancelAlarm(){
        alarm.cancelAlarm()
        Toast.makeText(applicationContext, "出席管理を解除しました", Toast.LENGTH_SHORT).show()
    }

    //リセットの際reload()を実行
    override fun onRestart() {
        super.onRestart()
        reload()
    }

    //リロード
    //画面を表示しなおしたい時に使用する
    private fun reload(){
        val intent = intent
        overridePendingTransition(0, 0)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        finish()

        overridePendingTransition(0, 0)
        startActivity(intent)
    }

    //終了時に呼び出す
    //realmも終了する
    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    //時計アイコンのアラームメニューを作成
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.setting, menu)
        setAlarmMenu(menu)
        return true
    }
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        setAlarmMenu(menu)
        return super.onPrepareOptionsMenu(menu)
    }

    //アラームメニューの作成
    //アラームの状態によってアイコンを変更する
    private fun setAlarmMenu(menu: Menu?){
        val item = menu?.getItem(0)
        if(preference.getIsAlarm()) {
            item?.title = "アラームを解除する"
            item?.icon = ResourcesCompat.getDrawable(resources, android.R.drawable.ic_lock_idle_alarm, null)
        }
        else{
            item?.title = "アラームをセットする"
            item?.icon = ResourcesCompat.getDrawable(resources, android.R.drawable.ic_delete, null)
        }
    }

    //三点メニューが選択された場合の動作
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.alarmMenu -> {//アラームメニューが選択されたとき
                if(preference.getIsAlarm()){
                    preference.setIsAlarm(false)
                    cancelAlarm()
                    invalidateOptionsMenu()
                }else{
                    preference.setIsAlarm(true)
                    setAlarm()
                    invalidateOptionsMenu()
                }
                return true
            }
            R.id.schoolLocationMenu -> {//「学校の位置を変更」メニューが選択されたとき
                //別アクティビティの入力画面を表示する
                val intent = Intent(this, SchoolLocationActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.periodMenu -> {//「時限の時間を初期化」メニューが選択されたとき
                preference.periodInit()
                for(i in 0..5) updatePeriodText(i)
                Toast.makeText(applicationContext, "授業時間を初期化しました", Toast.LENGTH_SHORT).show()
                checkAlarm()
                return true
            }
            R.id.lectureMenu-> {//「すべての授業を削除」メニューが選択されたとき
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
    //時限の時間を変更した場合に呼び出される
    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        if(timePickerIndex != -1){
            preference.setPeriod(timePickerIndex, hourOfDay, minute)
            updatePeriodText(timePickerIndex)
            checkAlarm()
        }
    }

    //時限の時間表示を変更する
    private fun updatePeriodText(index:Int){
        val time = preference.getPeriodArray()[index]
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, time / 100)
        cal.set(Calendar.MINUTE, time % 100)
        val dateFormat = DateFormat.format("HH:mm", cal)
        periodTimeText[index]?.text = dateFormat
    }

    //requestLocationPermissionのlistener
    //位置情報の使用をリクエストした結果
    override fun onRequestPermissionsResult(requestCode: Int, permissions:
    Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION->{
                if (permissions.isNotEmpty() && grantResults[0] == PackageManager.
                    PERMISSION_GRANTED) {
                    // 許可されればアラームをセットする
                    setAlarm()
                } else {//許可されなかった場合
                    Toast.makeText(applicationContext, "アラームはセット出来ませんでした", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
