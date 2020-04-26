package com.example.aona2.mytimetabletest

import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import io.realm.Realm
import io.realm.RealmResults
import java.util.*
import kotlin.math.min

class MyCalendar(pref: SharedPreferences) {
    val pref = pref

    private val INF_TIME: Long = 1000000000000000
    private val INF_ID = 100

    private lateinit var realm: Realm
    private var realmResults: RealmResults<Lecture>? = null

    private val preference = Preference(pref)

    init{
        realm = Realm.getDefaultInstance()
        realmResults = realm.where(Lecture::class.java)
            .findAll()
            .sort("period")
            .sort("youbi")
    }

    fun nextTimeLec(): Pair<Calendar?, Int> {
        val now: Calendar = Calendar.getInstance()
        logCalendar(now, "now")

        var minTime: Long = INF_TIME
        var minIndex: Int = INF_ID
        var minCal: Calendar? = null

        val rResults = realmResults
        if(rResults != null){
            //if(rResults.size == 0) return Pair(minCal?,-1)
            for (i in 0..(rResults.size-1)) {
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
        return Pair(minCal, minIndex)
    }

    fun nextIdLec(index: Int): Pair<Calendar?, Int> {
        val now: Calendar = Calendar.getInstance()
        val calendar: Calendar = Calendar.getInstance()

        var nextIndex = index + 1

        var nextCal: Calendar? = null

        val rResults = realmResults

        if(rResults != null) {
            if (nextIndex == rResults.size) nextIndex = 0
            val lecture = rResults[nextIndex]
            if (lecture != null) {
                nextCal = lecToCal(lecture, calendar)
            }
        }
        return Pair(nextCal, nextIndex)
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
            if (preference.periodArray[(lecture.period)] < hourMinute)
                calendar.add(Calendar.DAY_OF_MONTH, 7)
        }
        calendar.set(Calendar.HOUR_OF_DAY, preference.periodArray[lecture.period] / 100)
        calendar.set(Calendar.MINUTE, preference.periodArray[lecture.period] % 100)
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

    fun minAfter(minutes: Int): Calendar {
        val calendar: Calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, minutes)
        return calendar
    }
}