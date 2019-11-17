package com.example.chatapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import com.example.chatapp.adapters.ChatAdapter
import com.example.chatapp.adapters.FriendsRecyclerViewAdapter
import com.example.chatapp.models.ChatModel
import com.example.chatapp.models.MessageModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_message_chat.*
import kotlinx.android.synthetic.main.fragment_friends.*

class MessageChatActivity : AppCompatActivity(),View.OnClickListener {


    //TODO 1.Make a offline and online status on toolbar
    //TODO 2.scroll always to last

    private var isTextMessage = false
    private var databaseReference:DatabaseReference? = null
    private var currentUser:FirebaseAuth? = null
    private var friendlyId:String? = null
    private var friendlyImg:String? = null
    private var friendlyName:String? = null
    private var adapter:ChatAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_chat)

        currentUser = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
        friendlyId = intent.getStringExtra("userId")
        friendlyImg = intent.getStringExtra("userImg")
        friendlyName = intent.getStringExtra("userName")
        chatToolbarBack.setOnClickListener(this)
        chatSend.setOnClickListener(this)

        var linearLayoutManager = LinearLayoutManager(applicationContext)
        adapter = ChatAdapter(databaseReference!!,applicationContext,createChatId(),friendlyName!!,friendlyImg!!,this)
        chatRecyclerViewBody.setHasFixedSize(true)

        linearLayoutManager.stackFromEnd = true
        chatRecyclerViewBody.layoutManager = linearLayoutManager
        chatRecyclerViewBody.adapter = adapter

        setSupportActionBar(chatToolbar)
        chatToolbarUserName.text = friendlyName
        Picasso.get()
            .load(friendlyImg)
            .placeholder(R.mipmap.ic_logo)
            .into(chatToolbarUserImg)

        chatMessageText.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(count != 0){
                    isTextMessage = true
                    chatSend.setBackgroundResource(R.drawable.ic_send_chat)
                }else{
                    isTextMessage = false
                    chatSend.setBackgroundResource(R.drawable.ic_voice_chat)
                }
            }
        })
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.chatSend ->{
                if(isTextMessage){
                    if(!TextUtils.isEmpty(chatMessageText.text.toString())){
                        sendMessage(chatMessageText.text.toString())
                        chatMessageText.text.clear()
                    }
                }
            }
            R.id.chatAddFile ->{

            }
            R.id.chatToolbarBack->{
                finish()
            }
        }
    }

    private fun sendMessage(content:String){
        var messageModel = MessageModel()
        messageModel.messageTime = System.currentTimeMillis()
        messageModel.messageContent = content
        messageModel.messageTo = friendlyId
        messageModel.messageFrom = currentUser!!.uid.toString()
        messageModel.new = "true"
        databaseReference!!.child("Chats").child(createChatId()).push().setValue(messageModel)
    }

    private fun createChatId():String{
        val twoId = currentUser!!.uid+friendlyId
        var idCharArray:CharArray = twoId!!.toCharArray()
        idCharArray.sort()

        return String(idCharArray)

    }
    private fun setStatus(status:String){
        databaseReference!!.child("Users").child(currentUser!!.currentUser!!.uid)
        var map = HashMap<String,Any>()
        map.put("status",status)
        databaseReference!!.updateChildren(map)
    }

    override fun onResume() {
        super.onResume()
        setStatus("Online")
    }

    override fun onPause() {
        super.onPause()
        setStatus("Offline")
    }
}
