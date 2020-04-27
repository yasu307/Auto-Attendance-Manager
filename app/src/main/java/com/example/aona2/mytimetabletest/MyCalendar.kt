package com.example.aona2.mytimetabletest

import android.util.Log
import io.realm.Realm
import io.realm.RealmResults
import java.util.*
import kotlin.math.min

class MyCalendar(private val periodArray: Array<Int>) {
    private val infTime: Long = 1000000000000000
    private val infId = 100

    private var realm: Realm = Realm.getDefaultInstance()
    private var realmResults: RealmResults<Lecture>? = null

    var nextCalendar: Calendar? = null
    var nextIndex: Int? = null

    init{
        realmResults = realm.where(Lecture::class.java)
            .findAll()
            .sort("period")
            .sort("youbi")
    }

    fun nextTimeLec() {
        val now: Calendar = Calendar.getInstance()
        logCalendar(now, "now")

        var minTime: Long = infTime
        var minIndex: Int = infId
        var minCal: Calendar? = null

        val rResults = realmResults
        if(rResults != null){
            for (i in 0 until rResults.size) {
                var calendar: Calendar = Calendar.getInstance()
                val lecture = rResults[i]

                if (lecture != null) {
                    calendar = lecToCal(lecture, now)
                }

                val diffTime = calendar.timeInMillis - now.timeInMillis
                if (diffTime < minTime) {
                    if (lecture != null) minIndex = i
                    minCal = calendar
                    minTime = min(diffTime, minTime)
                }
            }
        }
        nextCalendar = minCal
        nextIndex = minIndex
    }

    fun nextIdLec(index: Int){
        val calendar: Calendar = Calendar.getInstance()
        nextIndex = index + 1

        val rResults = realmResults

        if(rResults != null) {
            if (nextIndex == rResults.size) nextIndex = 0
            val lecture = rResults[nextIndex?:0]
            if (lecture != null) {
                nextCalendar = lecToCal(lecture, calendar)
            }
        }
    }

    private fun lecToCal(lecture: Lecture, now: Calendar): Calendar {
        val calendar: Calendar = Calendar.getInstance()

        val hourMinute = now.get(Calendar.HOUR_OF_DAY) * 100 + now.get(Calendar.MINUTE)
        val youbi = now.get(Calendar.DAY_OF_WEEK)

        if (lecture.youbi > youbi)
            calendar.add(Calendar.DAY_OF_MONTH, (lecture.youbi - youbi))
        else if (lecture.youbi < youbi)
            calendar.add(Calendar.DAY_OF_MONTH, (lecture.youbi - youbi + 7))
        else {
            if (periodArray[(lecture.period)] < hourMinute)
                calendar.add(Calendar.DAY_OF_MONTH, 7)
        }
        calendar.set(Calendar.HOUR_OF_DAY, periodArray[lecture.period] / 100)
        calendar.set(Calendar.MINUTE, periodArray[lecture.period] % 100)
        calendar.set(Calendar.SECOND, 0)

        return calendar
    }

    fun logCalendar(calendar: Calendar?, string: String){
        if(calendar != null) {
            Log.d("Calendar name", string)
            Log.d("Calendar YEAR", calendar.get(Calendar.YEAR).toString())
            Log.d("Calendar MONTH", calendar.get(Calendar.MONTH).toString())
            Log.d("Calendar DAYOFMONTH", calendar.get(Calendar.DAY_OF_MONTH).toString())
            Log.d("Calendar HOUROFDAY", calendar.get(Calendar.HOUR_OF_DAY).toString())
            Log.d("Calendar MINUTE", calendar.get(Calendar.MINUTE).toString())
            Log.d("Calendar SECOND", calendar.get(Calendar.SECOND).toString())
            Log.d("Calendar DAYOFWEEK", calendar.get(Calendar.DAY_OF_WEEK).toString())
        }
    }

    fun minAfter(minutes: Int){
        val calendar: Calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, minutes)
        nextCalendar = calendar
    }
}