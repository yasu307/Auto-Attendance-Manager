package com.example.aona2.mytimetabletest

import android.content.SharedPreferences
import android.util.Log

class Preference(pref: SharedPreferences) {
    private val pref = pref
    private val periodNum = 5
    var periodArray = Array<Int>(periodNum + 1, {0})
    var schLocation: Pair<String?, String?>  = Pair(null, null)

    init {
        putDefaultPref()
        setLocation()
        setPeriod()
    }

    //共有プリファレンスに値を保存
    private fun putDefaultPref() {
        val editor = pref.edit()
        val lat: Double = 35.531371
        val lng: Double = 139.697453

        editor.putInt("period1", 900)
            .putInt("period2", 1550)
            .putInt("period3", 1552)
            .putInt("period4", 1554)
            .putInt("period5", 2159)
            .putString("lat", lat.toString())
            .putString("lng", lng.toString())
            .apply()
    }

    fun putLocation(location:Pair<String, String>){
        val editor = pref.edit()
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
        for(i in 0..periodNum-1){
            val string = "period" + (i+1).toString()
            periodArray[i] = pref.getInt(string, 0)
            Log.d("period", periodArray[i].toString())
        }
        return periodArray
    }

    /*
    //共有プリファレンスに保存されている値を取得する
    private fun getPref(){
        val pref = PreferenceManager.getDefaultSharedPreferences(this)

        val latString = pref.getString("lat", "0")
        val lngString = pref.getString("lng", "0")

        Log.d("latString", latString)
        Log.d("lngString", lngString)

        if(latString != null && lngString != null)
            location = Pair(latString.toDouble(), lngString.toDouble())

        Log.d("lat", location.first.toString())
        Log.d("lng", location.second.toString())

        for(i in 1..periodNum){
            val string = "period" + i.toString()
            periodArray[i] = pref.getInt(string, 0)
            Log.d("period", periodArray[i].toString())
        }
    }

     */
}