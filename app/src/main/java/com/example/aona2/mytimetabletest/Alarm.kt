package com.example.aona2.mytimetabletest

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

//次の授業の時間にアラームを設定し、アラームで設定された時間にAttendService（出欠確認をするサービス)を実行
class Alarm {
    var myCalendar: MyCalendar
    private var context: Context

    private lateinit var alarmManager: AlarmManager
    private lateinit var notifyPendingIntent: PendingIntent

    //現在の時間から次の授業の時間にアラームを設定する場合に使用する
    constructor(periodArray: Array<Int>, Context: Context){
        myCalendar = MyCalendar(periodArray)
        myCalendar.findFromTime()
        context = Context
        setAlarm()
    }

    //現在の授業から次の授業の時間にアラームを設定する場合に使用する
    constructor(periodArray: Array<Int>, index: Int, Context: Context){
        myCalendar = MyCalendar(periodArray)
        myCalendar.findFromId(index)
        context = Context
        setAlarm()
    }

    //アラームを設定する
    private fun setAlarm(){
        val notifyIntent = Intent(context, AttendService::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        notifyIntent.putExtra("index", myCalendar.nextIndex)
        notifyPendingIntent = PendingIntent.getForegroundService(
            context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmManager = context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager

        val alarmTimeMillis: Long? = myCalendar.nextCalendar?.timeInMillis
        if(alarmTimeMillis != null){
            Log.d("Alarm", "setAlarm")
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(alarmTimeMillis, null), notifyPendingIntent
            )
        }
        myCalendar.logCalendar(myCalendar.nextCalendar, "nextCalendar")
    }

    //アラームをキャンセルする
    fun cancelAlarm(){
        alarmManager.cancel(notifyPendingIntent)
    }

    //デバッグ用
    //現在から指定した時間後にアラームを設定する
    fun minAfter(minutes: Int){
        myCalendar.minAfter(minutes)
    }
}