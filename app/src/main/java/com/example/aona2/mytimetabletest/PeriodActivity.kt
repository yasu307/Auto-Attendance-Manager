package com.example.aona2.mytimetabletest

import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment

class PeriodActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_period)

    }

    fun showTimePickerDialog(v: View){
        val timePickerFragment = TimePickerFragment(12, 0)
        timePickerFragment.show(supportFragmentManager, "timePicker")
    }


}
