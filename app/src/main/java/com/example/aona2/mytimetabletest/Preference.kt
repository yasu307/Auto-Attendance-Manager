package com.example.aona2.mytimetabletest

import android.content.SharedPreferences
import android.util.Log

class Preference(private val pref: SharedPreferences) {
    private val periodNum = 5
    var periodArray = Array(periodNum + 1) {0}
    var schLocation: Pair<String?, String?>  = Pair(null, null)

    private var editor: SharedPreferences.Editor = pref.edit()

    init {
        if(isDefault()) putDefaultPref()
        setLocation()
        setPeriod()
    }

    private fun isDefault(): Boolean{
        return pref.getBoolean("isDefault", true)
    }

    //共有プリファレンスに値を保存
    private fun putDefaultPref() {
        val lat = 35.531371
        val lng = 139.697453

        editor.putInt("period1", 900)
            .putInt("period2", 1830)
            .putInt("period3", 1832)
            .putInt("period4", 1834)
            .putInt("period5", 2159)
            .putString("lat", lat.toString())
            .putString("lng", lng.toString())
            .putBoolean("isDefault", false)
            .apply()
        //Log.d("preference","putDefaultPref")
    }

    fun putLocation(location:Pair<String, String>){
        editor.putString("lat", location.first)
            .putString("lng", location.first)
            .apply()
    }

    //共有プリファレンスに保存されている値を取得する
    private fun setLocation() {
        val lat = pref.getString("lat", "0")
        val lng = pref.getString("lng", "0")
        schLocation = Pair(lat, lng)
    }

    private fun setPeriod(): Array<Int>{
        for(i in 0 until periodNum){
            val string = "period" + (i+1).toString()
            periodArray[i] = pref.getInt(string, 0)
            Log.d("period", periodArray[i].toString())
        }
        return periodArray
    }

}