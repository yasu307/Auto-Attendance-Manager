package com.example.aona2.mytimetabletest


import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.google.android.gms.location.LocationServices
import io.realm.Realm
import io.realm.RealmResults

class AttendService : Service(){
    private lateinit var preference: Preference

    private lateinit var realm: Realm
    private var realmResults: RealmResults<Lecture>? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("onstart", "onstart")

        //通知を表示
        val notification = NotificationCompat.Builder(this, "test").apply {
            setContentTitle("Timetable app")
            setContentText("出席をチェックしています")
            setSmallIcon(R.mipmap.ic_launcher)
        }.build()
        startForeground(1, notification)

        //出席チェック中の授業のrealmのindexを取得する
        val index = intent?.getIntExtra("index", 0)
        Log.d("index", index.toString())

        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        preference = Preference(pref)

        realm = Realm.getDefaultInstance()
        realmResults = realm.where(Lecture::class.java)
            .findAll()
            .sort("period")
            .sort("youbi")

        val rResults = realmResults
        if(rResults != null) {
            if(index != null) {
                val lecture = rResults[index]
                if (lecture != null) {
                    incrementLec(lecture)
                    if (checkAttend()) incrementAttend(lecture)
                }
            }
        }
        if(index != null) setAlarm(index)

        return super.onStartCommand(intent, flags, startId)
    }

    private fun checkAttend(): Boolean{
        var isAttend = false
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                // Got last known location. In some rare situations this can be null.
                Log.d("lastlocation", location?.latitude.toString())
                Log.d("lastlocation", location?.longitude.toString())

                val lastlat : Double? = location?.latitude
                val lastlng : Double? = location?.longitude

                val lat: Double? = preference.schLocation.first?.toDouble()
                val lng: Double? = preference.schLocation.second?.toDouble()

                if(lastlat != null && lastlng != null && lat != null && lng != null ){
                    val distance = getDistance(lat, lng, lastlat, lastlng)
                    Log.d("distance", distance.toString())
                    if(distance < 0.5) isAttend = true
                }else{
                    Log.d("AttendService", "lat or lng is null")
                }
            }
        fusedLocationClient.lastLocation
            .addOnFailureListener {
                Log.d("AttendService", "fail to get location")
            }
        return isAttend
    }

    //アラームをセットする
    private fun setAlarm(index: Int){
        val alarm = Alarm(preference.periodArray, index, this)
        //alarm.minAfter(1)
    }

    //lectureNumに1を足す
    private fun incrementLec(lecture: Lecture){
         realm.executeTransaction {
             lecture.lectureNum++
             Log.d("changeRealm","lectureNum")
         }
    }

    //attendに1を足す
    private fun incrementAttend(lecture: Lecture){
        realm.executeTransaction {
            lecture.attend++
            Log.d("changeRealm","attend")
        }
    }

    //realmに保存してあるすべての授業の値をログに表示する
    private fun realmShowIndex(){
        val rResults = realmResults
        if(rResults != null) {
            Log.d("realmIndexSize", rResults.size.toString())
            for (i in 0 until rResults.size) {
                val lecture = rResults[i]
                if (lecture != null) {
                    Log.d("realmId", lecture.id.toString())
                    Log.d("realmYoubi", lecture.youbi.toString())
                    Log.d("realmPeriod", lecture.period.toString())
                    Log.d("realmName", lecture.name)
                    Log.d("realmLectureNum", lecture.lectureNum.toString())
                    Log.d("realmAttend", lecture.attend.toString())
                }
            }
        }
    }

    //距離の計算に使用
    private fun getDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val theta = lon1 - lon2
        var dist =
            Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(
                deg2rad(lat1)
            ) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta))
        dist = Math.acos(dist)
        dist = rad2deg(dist)
        val miles = dist * 60 * 1.1515
        return miles * 1.609344
    }

    //距離の計算に使用
    private fun rad2deg(radian: Double): Double {
        return radian * (180f / Math.PI)
    }

    //距離の計算に使用
    fun deg2rad(degrees: Double): Double {
        return degrees * (Math.PI / 180f)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}