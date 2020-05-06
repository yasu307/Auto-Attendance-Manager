package com.example.aona2.mytimetabletest

import android.util.Log
import io.realm.Realm
import io.realm.RealmResults
import java.util.*
import kotlin.math.min

class MyCalendar{
    private val infTime: Long = 1000000000000000
    private val infId = 100

    private var realm: Realm = Realm.getDefaultInstance()
    private var realmResults: RealmResults<Lecture>? = null

    var nextCalendar: Calendar? = null //次にある授業の日程
    var nextIndex: Int? = null //次にある授業のrealm上のindex

    private lateinit var periodArray: Array<Int>

    constructor()

    constructor(array : Array<Int>){
        periodArray = array
        realmResults = realm.where(Lecture::class.java)
            .findAll()
            .sort("period")
            .sort("youbi")
    }

    //現在時間から次の授業を探す
    fun findFromTime() {
        val now: Calendar = Calendar.getInstance()

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
                    minIndex = i
                    minCal = calendar
                    minTime = min(diffTime, minTime)
                }
            }
        }
        nextCalendar = minCal
        nextIndex = minIndex
    }

    //授業のindexから次の授業を探す
    fun findFromId(index: Int){
        val calendar: Calendar = Calendar.getInstance()
        var tmpIndex = index + 1

        val rResults = realmResults

        if(rResults != null) {
            if (tmpIndex == rResults.size) tmpIndex = 0
            val lecture = rResults[tmpIndex]
            if (lecture != null) {
                nextCalendar = lecToCal(lecture, calendar)
            }
        }
        nextIndex = tmpIndex
    }

    //ある授業の情報からその授業が次にいつあるかを返す
    private fun lecToCal(lecture: Lecture, now: Calendar): Calendar {
        val calendar: Calendar = Calendar.getInstance()

        val hourMinute = now.get(Calendar.HOUR_OF_DAY) * 100 + now.get(Calendar.MINUTE)
        val youbi = now.get(Calendar.DAY_OF_WEEK)

        //日にちの設定　少し複雑
        //今日よりも授業のほうが後の曜日
        if (lecture.youbi > youbi)
            calendar.add(Calendar.DAY_OF_MONTH, (lecture.youbi - youbi))
        //今日よりも授業のほうが前の曜日
        else if (lecture.youbi < youbi)
            calendar.add(Calendar.DAY_OF_MONTH, (lecture.youbi - youbi + 7))
        //今日と授業が同じ曜日
        //今の時間より授業が前の時間なら来週なので7日増やす
        else {
            if (periodArray[(lecture.period)] < hourMinute)
                calendar.add(Calendar.DAY_OF_MONTH, 7)
        }
        //時間を設定する　登録してある内容をそのまま設定する
        calendar.set(Calendar.HOUR_OF_DAY, periodArray[lecture.period] / 100)
        calendar.set(Calendar.MINUTE, periodArray[lecture.period] % 100)
        calendar.set(Calendar.SECOND, 0)

        return calendar
    }

    //Calendarのlogを出力する
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

    //現在時間からn分後のカレンダーをセットする
    fun minAfter(minutes: Int){
        val calendar: Calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, minutes)
        nextCalendar = calendar
    }

    //実際に使用する
    fun dayToYoubi(day: Int): Int{
        val youbi = day + 2
        if(youbi == 8) return 1
        else return youbi
    }


/*
       //土曜日日曜日にデバッグしたいときに使う
       fun dayToYoubi(day: Int): Int{
           return day + 1
       }
 */


}