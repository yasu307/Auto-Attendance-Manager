package com.example.aona2.mytimetabletest

import android.content.SharedPreferences
import android.util.Log

class Preference(private val pref: SharedPreferences) {
    private val periodNum = 6
    var periodArray = Array(periodNum) {0}
    var schLocation: Pair<String?, String?>  = Pair(null, null)
    var isAlarm: Boolean? = null

    private val lat = 35.531371
    private val lng = 139.697453

    private var editor: SharedPreferences.Editor = pref.edit()

    private val defaultPeriod1 = 900
    private val defaultPeriod2 = 1040
    private val defaultPeriod3 = 1300
    private val defaultPeriod4 = 1440
    private val defaultPeriod5 = 1615
    private val defaultPeriod6 = 1800

    init {
        if(isDefault()) putDefaultPref()
        setLocation()
        setPeriod()
        setIsAlarm()
    }

    private fun isDefault(): Boolean{
        return pref.getBoolean("isDefault", true)
    }

    //共有プリファレンスに値を保存
    private fun putDefaultPref() {
        editor.putInt("period1", defaultPeriod1)
            .putInt("period2", defaultPeriod2)
            .putInt("period3", defaultPeriod3)
            .putInt("period4", defaultPeriod4)
            .putInt("period5", defaultPeriod5)
            .putInt("period6", defaultPeriod6)
            .putString("lat", lat.toString())
            .putString("lng", lng.toString())
            .putBoolean("isDefault", false)
            .putBoolean("isAlarm", true)
            .apply()
        //Log.d("preference","putDefaultPref")
    }

    fun periodInit(){
        editor.putInt("period1", defaultPeriod1)
            .putInt("period2", defaultPeriod2)
            .putInt("period3", defaultPeriod3)
            .putInt("period4", defaultPeriod4)
            .putInt("period5", defaultPeriod5)
            .putInt("period6", defaultPeriod6)
            .apply()
    }

    fun locationInit(){
        editor.putString("lat", lat.toString())
            .putString("lng", lng.toString())
            .apply()
    }

    fun putLocation(location:Pair<String, String>){
        editor.putString("lat", location.first)
            .putString("lng", location.first)
            .apply()
    }

    fun putPeriod(periodIndex: Int, hour: Int, minute: Int){
        val time = hour * 100 + minute
        val periodString = "period" + (periodIndex+1).toString()
        editor.putInt(periodString, time)
            .apply()
        periodArray[periodIndex] = time
    }

    fun putIsAlarm(IsAlarm: Boolean){
        isAlarm = IsAlarm
        editor.putBoolean("isAlarm", IsAlarm)
            .apply()
    }

    //共有プリファレンスに保存されている値を取得する
    private fun setLocation() {
        val lat = pref.getString("lat", "0")
        val lng = pref.getString("lng", "0")
        schLocation = Pair(lat, lng)
    }

    private fun setPeriod(){
        for(i in 0 until periodNum){
            val string = "period" + (i+1).toString()
            periodArray[i] = pref.getInt(string, 0)
            Log.d("period", periodArray[i].toString())
        }
    }

    private fun setIsAlarm(){
        isAlarm = pref.getBoolean("isAlarm", true)
    }
}