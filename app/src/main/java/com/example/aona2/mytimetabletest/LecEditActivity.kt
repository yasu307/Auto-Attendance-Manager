package com.example.aona2.mytimetabletest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_lec_edit.*
import java.util.*

class LecEditActivity : AppCompatActivity() {
    private lateinit var realm: Realm


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lec_edit)

        val period = intent.getIntExtra("period", 0)
        val day = intent.getIntExtra("day", 0)
        val youbi = dayToYoubi(day)

        realm = Realm.getDefaultInstance()
        val lecture = realm.where<Lecture>().equalTo("youbi", youbi).equalTo("period", period).findFirst()

        val days_string = resources?.getStringArray(R.array.Days)
        if(days_string != null)
        whenText.setText(days_string[day].toString() + (period+1).toString() + "限")
        nameEdit.setText(lecture?.name)

        saveBtn.setOnClickListener {
            if(lecture == null){
                realm.executeTransaction {
                    val maxId = realm.where<Lecture>().max("id")
                    val nextId = (maxId?.toLong() ?: 0L) + 1L
                    val lecture = realm.createObject<Lecture>(nextId)
                    lecture.youbi = youbi
                    lecture.period = period
                    lecture.name = nameEdit.text.toString()
                }
            }else{
                realm.executeTransaction{
                    lecture.name = nameEdit.text.toString()
                }
            }
            Toast.makeText(applicationContext, "保存しました", Toast.LENGTH_SHORT).show()
            finish()
        }

        deleteBtn.setOnClickListener {
            realm.executeTransaction{
                if(lecture != null)
                    lecture.deleteFromRealm()
            }
            Toast.makeText(applicationContext, "削除しました", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun dayToYoubi(day: Int): Int{
        if(day == 6) return 0
        else return day+ 1
    }
}
