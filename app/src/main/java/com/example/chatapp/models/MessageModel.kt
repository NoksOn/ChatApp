package com.example.chatapp.models

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class MessageModel() {
    var messageFrom:String? = null
    var messageTo:String? = null
    var messageTime:Long? = null
    var messageContent:String? = null
    var new:String? = "false"

    constructor(messageFrom:String,messageTo:String,time:Long,messageContent:String,new:String) : this() {
        this.messageFrom = messageFrom
        this.messageTo = messageTo
        this.messageContent = messageContent
        this.messageTime = time
        this.new = new
    }
    fun showNormalData(timeAssigned: Long):String{
        val sdf = SimpleDateFormat("hh:mm")
        //var dateFormat: java.text.DateFormat = DateFormat.getDateInstance()
        var formattedDate: String = sdf.format(Date(timeAssigned).time)

        return formattedDate
    }
}