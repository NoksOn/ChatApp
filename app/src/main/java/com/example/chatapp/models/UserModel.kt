package com.example.chatapp.models

class UserModel() {
    var nickname: String? = null
    var image: String? = null
    var thumb_image: String? = null
    var userId:String? = null
    var IsFriend:Boolean = false


    constructor(nickName: String, image: String,
                thumb_image: String,userId:String): this() {
        this.nickname = nickName
        this.image = image
        this.thumb_image = thumb_image
        this.userId = userId

    }

}