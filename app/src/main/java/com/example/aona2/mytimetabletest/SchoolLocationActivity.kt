package com.example.aona2.mytimetabletest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_school_location.*

//学校の位置を変更するアクティビティ
//MainActivityの「学校の位置を変更」メニューが選択されたときにこのactivityに遷移する
class SchoolLocationActivity : AppCompatActivity() {
    private lateinit var preference: Preference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_school_location)

        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        preference = Preference(pref)

        //現在保存してある学校位置を共有プリファレンスから取得する
        val location = preference.schLocation
        //TextViewに現在の学校位置を入力
        latEdit.setText(location.first.toString())
        lngEdit.setText(location.second.toString())

        //保存ボタンが押された場合にTextViewに入力された位置を学校位置として保存する
        saveBtn.setOnClickListener {
            if(latEdit.text.isNullOrEmpty() || lngEdit.text.isNullOrEmpty())//どちらかが入力されていない場合
                Toast.makeText(applicationContext, "正しく入力してください", Toast.LENGTH_SHORT).show()
            else{
                preference.putLocation(Pair(latEdit.text.toString(), lngEdit.text.toString()))
                Toast.makeText(applicationContext, "保存しました", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        //初期化ボタンが押された場合にTextViewに初期位置を入力する
        initBtn.setOnClickListener {
            latEdit.setText(preference.defaultLat.toString())
            lngEdit.setText(preference.defaultLng.toString())
        }
    }
}
