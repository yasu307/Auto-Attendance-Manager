package com.example.aona2.mytimetabletest

import android.content.SharedPreferences
import android.util.Log

class Preference(private val pref: SharedPreferences) {
    private val periodNum = 5
    var periodArray = Array(periodNum + 1) {0}
    var schLocation: Pair<String?, String?>  = Pair(null, null)
    var isAlarm: Boolean? = null

    private val lat = 35.531371
    private val lng = 139.697453

    private var editor: SharedPreferences.Editor = pref.edit()

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
        editor.putInt("period1", 900)
            .putInt("period2", 1040)
            .putInt("period3", 1300)
            .putInt("period4", 1440)
            .putInt("period5", 1615)
            .putInt("period6", 1800)
            .putString("lat", lat.toString())
            .putString("lng", lng.toString())
            .putBoolean("isDefault", false)
            .putBoolean("isAlarm", true)
            .apply()
        //Log.d("preference","putDefaultPref")
    }

    fun periodInit(){
        editor.putInt("period1", 900)
            .putInt("period2", 1040)
            .putInt("period3", 1300)
            .putInt("period4", 1440)
            .putInt("period5", 1615)
            .putInt("period6", 1800)
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