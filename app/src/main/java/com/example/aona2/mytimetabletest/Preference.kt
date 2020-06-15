package com.example.aona2.mytimetabletest

import android.content.SharedPreferences

//共有プリファレンスを扱うクラス
//フィールドの値を他のクラスから使用される
class Preference(private val pref: SharedPreferences) {
    private val periodNum = 6

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

    //学校の位置を返す
    fun getSchLocation():Pair<String?, String?>{
        return Pair(pref.getString("lat", defaultLat.toString()),pref.getString("lng", defaultLat.toString()))
    }

    //時限の時間を返す
    fun getPeriodArray():Array<Int>{
        return arrayOf(pref.getInt("period1", defaultPeriod1),
            pref.getInt("period2", defaultPeriod2),
            pref.getInt("period3", defaultPeriod3),
            pref.getInt("period4", defaultPeriod4),
            pref.getInt("period5", defaultPeriod5),
            pref.getInt("period6", defaultPeriod6)
        )
    }

    //アラームがセットしてあるかを返す
    fun getIsAlarm():Boolean{
        return pref.getBoolean("isAlarm", true)
    }

    //共有プリファレンスに初期値を保存
    private fun setDefaultPref() {
        editor.putInt("period1", defaultPeriod1)
            .putInt("period2", defaultPeriod2)
            .putInt("period3", defaultPeriod3)
            .putInt("period4", defaultPeriod4)
            .putInt("period5", defaultPeriod5)
            .putInt("period6", defaultPeriod6)
            .putString("lat", defaultLat.toString())
            .putString("lng", defaultLng.toString())
            .putBoolean("isAlarm", true)
            .apply()
    }

    //時限開始時間を初期化する
    fun periodInit(){
        editor.putInt("period1", defaultPeriod1)
            .putInt("period2", defaultPeriod2)
            .putInt("period3", defaultPeriod3)
            .putInt("period4", defaultPeriod4)
            .putInt("period5", defaultPeriod5)
            .putInt("period6", defaultPeriod6)
            .apply()
    }

    //引数の値を学校の位置として保存する
    fun setSchLocation(location:Pair<String, String>){
        editor.putString("lat", location.first)
            .putString("lng", location.second)
            .apply()
    }

    //引数の値を時限の時間として保存する
    fun setPeriod(periodIndex: Int, hour: Int, minute: Int){
        val time = hour * 100 + minute
        val periodString = "period" + (periodIndex+1).toString()
        editor.putInt(periodString, time)
            .apply()
    }

    //引数の値をアラームがセットしてあるかの変数として返す
    fun setIsAlarm(IsAlarm: Boolean){
        editor.putBoolean("isAlarm", IsAlarm)
            .apply()
    }

    //共有プリファレンスを削除する
    //デバッグ用
    fun clearPref(){
        editor.clear().commit()
    }
}