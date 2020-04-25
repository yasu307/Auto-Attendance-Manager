package com.example.aona2.mytimetabletest

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Lecture : RealmObject(){
    @PrimaryKey
    var id: Int = 0
    var youbi: Int = 0 //日曜日が1
    var period: Int = 0
    var name: String = ""
    var lectureNum: Int = 0
    var attend: Int = 0

}