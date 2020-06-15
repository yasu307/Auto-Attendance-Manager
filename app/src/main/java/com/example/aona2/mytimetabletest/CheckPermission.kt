package com.example.aona2.mytimetabletest

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat

//位置情報の許可を確認するクラス
class CheckPermission(private val context: Activity) {
    private val MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 1

    //許可されているか確認
    fun checkPermission():Boolean {
        // 既に許可している
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED) {
            return true
        } else {//許可していない場合、リクエストする
            Log.d("debug", "requestLocationPermission()　------")
            requestLocationPermission()
            return false
        }
    }

    //許可を求める
    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(context,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // 以前に許可を求め、拒否されていた場合
            ActivityCompat.requestPermissions(context, arrayOf(
                Manifest.permission.
                ACCESS_FINE_LOCATION),
                MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION)
        } else {
            // まだ許可を求めていなかった場合
            ActivityCompat.requestPermissions(context, arrayOf(
                Manifest.permission.
                ACCESS_FINE_LOCATION),
                MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION)
        }
    }
}