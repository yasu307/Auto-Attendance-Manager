package com.example.aona2.mytimetabletest

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import androidx.fragment.app.DialogFragment


class TimePickerFragment(private var listener: TimePickerDialog.OnTimeSetListener,
                         private val preference: Preference,
                         private val index:Int): DialogFragment(){

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val time = preference.periodArray[index]
        return TimePickerDialog(activity, R.style.TimePickerDialogTheme, listener,
            time/100, time%100, DateFormat.is24HourFormat(activity))
    }
}