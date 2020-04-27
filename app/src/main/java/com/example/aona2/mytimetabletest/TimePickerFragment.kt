package com.example.aona2.mytimetabletest

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class TimePickerFragment(private var hour: Int, private var minute: Int): DialogFragment(), TimePickerDialog.OnTimeSetListener{

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(activity, this, hour, minute, DateFormat.is24HourFormat(activity))
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        // Do something with the time chosen by the user

    }

}