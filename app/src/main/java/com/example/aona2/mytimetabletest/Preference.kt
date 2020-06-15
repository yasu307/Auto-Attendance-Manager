package com.example.aona2.mytimetabletest

import android.content.SharedPreferences

//共有プリファレンスを扱うクラス
//フィールドの値を他のクラスから使用される
class Preference(private val pref: SharedPreferences) {
    private val periodNum = 6
    var periodArray = Array(periodNum) {0}
    var schLocation: Pair<String?, String?>  = Pair(null, null)
    var isAlarm: Boolean? = null

    private var editor: SharedPreferences.Editor = pref.edit()

    //学校の位置の初期値
    //AVDの位置 google本社？
    val defaultLat = 37.421998
    val defaultLng = -122.084000
    //電気通信大学
    //private val defaultLat = 35.657597
    //private val defaultLng = 139.543641

    //時限の時間の初期値
    private val defaultPeriod1 = 900
    private val defaultPeriod2 = 1040
    private val defaultPeriod3 = 1300
    private val defaultPeriod4 = 1440
    private val defaultPeriod5 = 1615
    private val defaultPeriod6 = 1800

    //初期値を共有プリファレンスに保存し, フィールドに値を持つ
    //初めて起動した場合に呼び出される
    init {
        if(isDefault()) putDefaultPref()
        setLocation()
        setPeriod()
        setIsAlarm()
    }

    //初めて起動したかどうか判別
    private fun isDefault(): Boolean{
        return pref.getBoolean("isDefault", true)
    }

    //共有プリファレンスに初期値を保存
    private fun putDefaultPref() {
        editor.putInt("period1", defaultPeriod1)
            .putInt("period2", defaultPeriod2)
            .putInt("period3", defaultPeriod3)
            .putInt("period4", defaultPeriod4)
            .putInt("period5", defaultPeriod5)
            .putInt("period6", defaultPeriod6)
            .putString("lat", defaultLat.toString())
            .putString("lng", defaultLng.toString())
            .putBoolean("isDefault", false)
            .putBoolean("isAlarm", true)
            .apply()
    }

    //時限の時間に初期値を保存する
    fun periodInit(){
        editor.putInt("period1", defaultPeriod1)
            .putInt("period2", defaultPeriod2)
            .putInt("period3", defaultPeriod3)
            .putInt("period4", defaultPeriod4)
            .putInt("period5", defaultPeriod5)
            .putInt("period6", defaultPeriod6)
            .apply()
        setPeriod()
    }

    //学校の位置に初期値を保存する
    fun locationInit(){
        editor.putString("lat", defaultLat.toString())
            .putString("lng", defaultLng.toString())
            .apply()
        setLocation()
    }

    //引数の値を学校の位置として保存する
    fun putLocation(location:Pair<String, String>){
        editor.putString("lat", location.first)
            .putString("lng", location.second)
            .apply()
        setLocation()
    }

    //引数の値を時限の時間として保存する
    fun putPeriod(periodIndex: Int, hour: Int, minute: Int){
        val time = hour * 100 + minute
        val periodString = "period" + (periodIndex+1).toString()
        editor.putInt(periodString, time)
            .apply()
        setPeriod()
    }

    //引数の値をアラームセットとして保存する
    fun putIsAlarm(IsAlarm: Boolean){
        isAlarm = IsAlarm
        editor.putBoolean("isAlarm", IsAlarm)
            .apply()
    }

    //共有プリファレンスに保存されている学校の場所の値をフィールドに保持する
    private fun setLocation() {
        val lat = pref.getString("lat", "0")
        val lng = pref.getString("lng", "0")
        schLocation = Pair(lat, lng)
    }

    //共有プリファレンスに保存されている時限時間の値をフィールドに保持する
    private fun setPeriod(){
        for(i in 0 until periodNum){
            val string = "period" + (i+1).toString()
            periodArray[i] = pref.getInt(string, 0)
        }
    }

    //共有プリファレンスに保存されているアラームセットの値をフィールドに保持する
    private fun setIsAlarm(){
        isAlarm = pref.getBoolean("isAlarm", true)
    }
}