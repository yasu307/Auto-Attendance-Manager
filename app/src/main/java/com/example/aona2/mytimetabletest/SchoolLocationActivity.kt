package com.example.aona2.mytimetabletest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_school_location.*


class SchoolLocationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_school_location)

        val location = getLocation()
        latEdit.setText(location.first.toString())
        lngEdit.setText(location.second.toString())

        saveBtn.setOnClickListener {
            if(latEdit.text.isNullOrEmpty() || lngEdit.text.isNullOrEmpty())
                Toast.makeText(applicationContext, "正しく入力してください", Toast.LENGTH_SHORT).show()
            else{
                putLocation(Pair(latEdit.text.toString(), lngEdit.text.toString()))
                Toast.makeText(applicationContext, "保存しました", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    //共有プリファレンスに値を保存
    private fun putLocation(location:Pair<String, String>){
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = pref.edit()

        editor.putString("lat", location.first)
            .putString("lng", location.first)
            .apply()
    }

    //共有プリファレンスに保存されている値を取得する
    private fun getLocation(): Pair<Double?, Double?> {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)

        val latString = pref.getString("lat", "0")
        val lngString = pref.getString("lng", "0")
        return Pair(latString?.toDouble(), lngString?.toDouble())
    }
}
