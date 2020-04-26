package com.example.aona2.mytimetabletest

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import com.google.android.gms.location.*
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_attend.*
import java.util.*

class AttendActivity : AppCompatActivity() {
    private lateinit var preference: Preference

    private lateinit var realm: Realm
    private var realmResults: RealmResults<Lecture>? = null

    private var index = -1

    private val MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 1

    private lateinit var fusedLocationProviderClient : FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private var locationCallback : LocationCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        setContentView(R.layout.activity_attend)

        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        preference = Preference(pref)

        realm = Realm.getDefaultInstance()
        realmResults = realm.where(Lecture::class.java)
            .findAll()
            .sort("period")
            .sort("youbi")

        index = intent.getIntExtra("index", 0)
        Log.d("index", index.toString())

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        checkPermission()

        var myCalendar = MyCalendar(pref)
        //myCalendar.periodArray = periodArray

        val nextPair = myCalendar.nextIdLec(index)
        myCalendar.logCalendar(nextPair.first, "nextAlarm")

        setAlarm(nextPair.first, nextPair.second)
    }

    //アラームをセットする
    private fun setAlarm(calendar: Calendar?, alarmId: Int){
        if(calendar == null) return

        var alarmManager : AlarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val notifyIntent = Intent(this, AttendActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        notifyIntent.putExtra("index", alarmId)

        val notifyPendingIntent = PendingIntent.getActivity(
            this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmTimeMillis = calendar.timeInMillis
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(alarmTimeMillis, null), notifyPendingIntent)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTimeMillis, notifyPendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTimeMillis, notifyPendingIntent)
        }
    }

    private fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED){
            myLocationEnable()
        } else {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // 許可を求め、拒否されていた場合
            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.
                ACCESS_FINE_LOCATION),
                MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION)
        } else {
            // まだ許可を求めていない
            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.
                ACCESS_FINE_LOCATION),
                MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions:
    Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION->{
                if (permissions.isNotEmpty() && grantResults[0] == PackageManager.
                    PERMISSION_GRANTED) {
                    // 許可された
                    myLocationEnable()
                } else {
                    showToast(" 現在位置は表示できません ")
                }
            }
        }
    }

    private fun myLocationEnable(){
        // 赤い波線でエラーが表示されてしまうので
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //mMap.isMyLocationEnabled = true
            val locationRequest = LocationRequest().apply {
                //interval = 10000 // 最も長い更新時間
                //fastestInterval = 5000 // 最も短い更新時間
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    if (locationResult?.lastLocation != null) {
                        lastLocation = locationResult.lastLocation
                        //val currentLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
                        textView.text = "Lat:${lastLocation.latitude}, Lng:${lastLocation.longitude}"

                        Log.d("last", lastLocation.latitude.toString())
                        Log.d("last", lastLocation.longitude.toString())

                        val lat: Double = preference.schLocation.first?.toDouble() ?: 0.0
                        val lng: Double = preference.schLocation.first?.toDouble() ?: 0.0

                        val distance = getDistance(lat, lng, lastLocation.latitude, lastLocation.longitude)
                        Log.d("distance", distance.toString())
                        changeRealm("lectureNum")
                        if(distance < 0.5) changeRealm("attend")
                        realmShowIndex()
                    }
                }
            }
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                locationCallback, null)
        }
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
            for (i in 0..(rResults.size-1)) {
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

    private fun showToast(msg: String) {
        val toast = Toast.makeText(this, msg, Toast.LENGTH_LONG)
        toast.show()
    }
}
