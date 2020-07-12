package com.example.aona2.mytimetabletest


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.google.android.gms.location.LocationServices
import io.realm.Realm
import io.realm.RealmResults

//位置情報から出欠を確認し、realmに保存してある授業情報を更新する
class AttendService : Service(){
    private lateinit var preference: Preference

    private lateinit var realm: Realm
    private var realmResults: RealmResults<Lecture>? = null

    private val limitDistance = 1.0 //学校の位置からnkmの位置にいれば出席とする

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("onstart", "onstart")

        val channelId = "channelId"
        val channelName = "My service channel"
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                channelId, channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            var manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        //通知を表示
        val notification = NotificationCompat.Builder(this, channelId).apply {
            setContentTitle("Timetable app")
            setContentText("出席をチェックしています")
            setSmallIcon(R.mipmap.ic_launcher)
        }.build()
        val NOTIFICATION_ID = 12345
        startForeground(NOTIFICATION_ID, notification)

        //出席チェック中の授業のrealmのindexを取得する
        val index = intent?.getIntExtra("index", 0)

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
                    incrementLec(lecture) //現在の授業の授業回数を１増やす
                    checkAttend(lecture)  //出欠を確認する
                }
            }
        }
        //次の授業の時間にアラームをセットする
        if(index != null) setAlarm(index)

        //終わったら終了する
        stopSelf()

        return super.onStartCommand(intent, flags, startId)
    }

    //位置情報から出欠を確認する
    private fun checkAttend(lecture: Lecture){
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                Log.d("lastlocation", location?.latitude.toString())
                Log.d("lastlocation", location?.longitude.toString())

                //現在の位置を取得
                val lastlat : Double? = location?.latitude
                val lastlng : Double? = location?.longitude

                //学校の場所を取得
                val schoolLat: Double? = preference.getSchLocation().first?.toDouble()
                val schoolLng: Double? = preference.getSchLocation().second?.toDouble()

                //現在位置と学校の位置の距離を計算しrealmを更新する
                if(lastlat != null && lastlng != null && schoolLat != null && schoolLng != null ){
                    val distance = getDistance(schoolLat, schoolLng, lastlat, lastlng)
                    Log.d("distance", distance.toString())
                    if(distance < limitDistance){
                        incrementAttend(lecture)
                    }
                }else{
                    Log.d("AttendService", "lat or lng is null")
                }
            }
        fusedLocationClient.lastLocation
            .addOnFailureListener {
                Log.d("AttendService", "fail to get location")
            }
    }

    //次のアラームをセットする
    private fun setAlarm(index: Int){
        Alarm(preference.getPeriodArray(), index, this)
        //alarm.minAfter(1)
    }

    //授業の時間になったら呼び出される
    //lectureNumに1を足す
    private fun incrementLec(lecture: Lecture){
         realm.executeTransaction {
             lecture.lectureNum++
             Log.d("changeRealm","lectureNum")
         }
    }

    //出席をしたときに呼び出す
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