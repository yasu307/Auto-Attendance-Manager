package com.example.aona2.mytimetabletest

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import io.realm.Realm
import io.realm.RealmResults

class AttendService : Service(){
    private lateinit var preference: Preference

    private lateinit var realm: Realm
    private var realmResults: RealmResults<Lecture>? = null

    private var index = -1

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var lat: Double? = null
    private var lng: Double? = null


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("onstart", "onstart")
        index = intent?.getIntExtra("index", 0)?:0
        Log.d("index", index.toString())

        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        preference = Preference(pref)

        realm = Realm.getDefaultInstance()
        realmResults = realm.where(Lecture::class.java)
            .findAll()
            .sort("period")
            .sort("youbi")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                // Got last known location. In some rare situations this can be null.
                Log.d("lastlocation", location?.latitude.toString())
                Log.d("lastlocation", location?.longitude.toString())

                val lastlat : Double = location?.latitude?:0.0
                val lastlng : Double = location?.longitude?:0.0

                val lat: Double = preference.schLocation.first?.toDouble() ?: 0.0
                val lng: Double = preference.schLocation.first?.toDouble() ?: 0.0

                val distance = getDistance(lat, lng, lastlat, lastlng)
                Log.d("distance", distance.toString())
                changeRealm("lectureNum")
                if(distance < 0.5) changeRealm("attend")
                realmShowIndex()
            }
        fusedLocationClient.lastLocation
            .addOnFailureListener {
                Log.d("fail", "fail")
            }

        setAlarm()

        return super.onStartCommand(intent, flags, startId)
    }

    //アラームをセットする
    private fun setAlarm(){
        val alarm = Alarm(preference.periodArray, index)
        alarm.minAfter(1)
        val notifyIntent = Intent(this, AttendService::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        notifyIntent.putExtra("index", alarm.myCalendar.nextIndex)
        val notifyPendingIntent = PendingIntent.getService(
            this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager : AlarmManager = getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
        alarm.setAlarm(alarmManager, notifyPendingIntent)
    }

    private fun changeRealm(string: String){
        val rResults = realmResults
        if(rResults != null) {
            val lecture = rResults[index]
            if(lecture != null) {
                realm.executeTransaction {
                    if (string == "lectureNum") {
                        lecture.lectureNum = lecture.lectureNum + 1
                        Log.d("changeRealm","lectureNum")
                    }
                    else if (string == "attend"){
                        lecture.attend = lecture.attend + 1
                        Log.d("changeRealm","attend")
                    }
                }
            }
        }
    }

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

    private fun rad2deg(radian: Double): Double {
        return radian * (180f / Math.PI)
    }

    fun deg2rad(degrees: Double): Double {
        return degrees * (Math.PI / 180f)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}