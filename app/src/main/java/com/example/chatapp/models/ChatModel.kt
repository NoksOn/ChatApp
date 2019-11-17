package com.example.chatapp.models

import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatModel() {
    var lastMessage:String? = null
    var date:Long? = null
    var userName:String? = null
    var userId:String? = null
    var imgUrl:String? = null
    var newMessagesCounter:Int? = null

    constructor(lastMessage:String,date:Long,userName:String,imgUrl:String,newMessagesCounter:Int):this(){
        this.lastMessage = lastMessage
        this.date = date
        this.userName = userName
        this.imgUrl = imgUrl
        this.newMessagesCounter = newMessagesCounter
    }

    fun showNormalData(timeAssigned: Long):String{
        var dateFormat: java.text.DateFormat = DateFormat.getDateInstance()
        var formattedDate: String = dateFormat.format(Date(timeAssigned).time)

        return formattedDate
    }
}