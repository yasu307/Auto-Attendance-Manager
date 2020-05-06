package com.example.aona2.mytimetabletest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_school_location.*


class SchoolLocationActivity : AppCompatActivity() {
    private lateinit var preference: Preference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_school_location)

        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        preference = Preference(pref)

        val location = preference.schLocation
        latEdit.setText(location.first.toString())
        lngEdit.setText(location.second.toString())

        saveBtn.setOnClickListener {
            if(latEdit.text.isNullOrEmpty() || lngEdit.text.isNullOrEmpty())
                Toast.makeText(applicationContext, "正しく入力してください", Toast.LENGTH_SHORT).show()
            else{
                preference.putLocation(Pair(latEdit.text.toString(), lngEdit.text.toString()))
                Toast.makeText(applicationContext, "保存しました", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        initBtn.setOnClickListener {
            latEdit.setText(preference.defaultLat.toString())
            lngEdit.setText(preference.defaultLng.toString())
        }
    }
}
