package com.example.aona2.mytimetabletest

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class Alarm {
    var myCalendar: MyCalendar
    private var context: Context

    private lateinit var alarmManager: AlarmManager
    private lateinit var notifyPendingIntent: PendingIntent

    constructor(periodArray: Array<Int>, Context: Context){
        myCalendar = MyCalendar(periodArray)
        myCalendar.findFromTime()
        context = Context
        setAlarm()
    }

    constructor(periodArray: Array<Int>, index: Int, Context: Context){
        myCalendar = MyCalendar(periodArray)
        myCalendar.findFromId(index)
        context = Context
        setAlarm()
    }

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

            /* LOLIPOP以下のバージョンをサポートする場合
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(alarmTimeMillis, null), intent
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTimeMillis, intent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTimeMillis, intent)
            }

             */
        }
        myCalendar.logCalendar(myCalendar.nextCalendar, "nextCalendar")
    }

    fun cancelAlarm(){
        alarmManager.cancel(notifyPendingIntent)
    }

    fun minAfter(minutes: Int){
        myCalendar.minAfter(minutes)
    }
}