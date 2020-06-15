package com.example.aona2.mytimetabletest

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration

//Realmの初期化に使用する
class CustomApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        val config = RealmConfiguration.Builder().build()
        Realm.setDefaultConfiguration(config)
    }
}