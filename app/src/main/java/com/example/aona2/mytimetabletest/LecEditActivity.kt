package com.example.aona2.mytimetabletest


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_lec_edit.*

//授業コマをタップすると呼び出される
//授業をrealmに保存する
class LecEditActivity : AppCompatActivity() {
    //test
    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lec_edit)

        //どのコマから遷移してきたかを取得
        val period = intent.getIntExtra("period", 0)
        val day = intent.getIntExtra("day", 0)
        val youbi = MyCalendar().dayToYoubi(day)

        //そのコマのrealmがあれば取得
        realm = Realm.getDefaultInstance()
        var lecture = realm.where<Lecture>().
            equalTo("youbi", youbi).equalTo("period", period).findFirst()

        //TextViewを埋める
        val daysString = resources?.getStringArray(R.array.Days)
        if(daysString != null)
        whenText.text = daysString[day].toString() +
                (period+1).toString() + "限"
        nameEdit.setText(lecture?.name)

        //保存ボタン：realmに保存する
        saveBtn.setOnClickListener {
            if(lecture == null){ //そのコマのrealmがなければ作成
                realm.executeTransaction {
                    val maxId = realm.where<Lecture>().max("id")
                    val nextId = (maxId?.toLong() ?: 0L) + 1L
                    lecture = realm.createObject(nextId)
                    lecture?.youbi = youbi
                    lecture?.period = period
                    lecture?.name = nameEdit.text.toString()
                }
            }else{ //そのコマのrealmがあれば、nameを更新する
                realm.executeTransaction{
                    lecture?.name = nameEdit.text.toString()
                }
            }
            Toast.makeText(applicationContext, "保存しました", Toast.LENGTH_SHORT).show()
            finish()
        }

        //削除ボタン:そのコマのrealmが存在していれば削除する
        deleteBtn.setOnClickListener {
            realm.executeTransaction{
                    lecture?.deleteFromRealm()
            }
            Toast.makeText(applicationContext, "削除しました", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}
