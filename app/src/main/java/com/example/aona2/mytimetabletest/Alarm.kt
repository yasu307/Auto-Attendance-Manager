package com.example.aona2.mytimetabletest

import android.app.AlarmManager
import android.app.PendingIntent
import android.os.Build
import java.util.*
import android.content.Intent

class Alarm {
    var myCalendar: MyCalendar

    constructor(periodArray: Array<Int>){
        myCalendar = MyCalendar(periodArray)
        myCalendar.nextTimeLec()
    }

    constructor(periodArray: Array<Int>, index: Int?){
        myCalendar = MyCalendar(periodArray)
        if(index != null)
        myCalendar.nextIdLec(index)
    }

    fun setAlarm(alarmManager: AlarmManager, intent: PendingIntent){
        val alarmTimeMillis: Long? = myCalendar.nextCalendar?.timeInMillis
        if(alarmTimeMillis != null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(alarmTimeMillis, null), intent
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTimeMillis, intent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTimeMillis, intent)
            }
        }
    }
}