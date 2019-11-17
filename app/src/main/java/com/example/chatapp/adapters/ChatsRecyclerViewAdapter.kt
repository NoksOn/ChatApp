package com.example.chatapp.adapters

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.chatapp.MessageChatActivity
import com.example.chatapp.R
import com.example.chatapp.models.ChatModel
import com.example.chatapp.models.MessageModel
import com.example.chatapp.models.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ChatsRecyclerViewAdapter(var databaseReference: DatabaseReference,var context:Context,var mAuth: FirebaseAuth)
    :RecyclerView.Adapter<ChatsRecyclerViewAdapter.ViewHolder>(){

    var chatsList:ArrayList<ChatModel> = ArrayList()

    init {
        loadAllChats()
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val view:View = LayoutInflater.from(context).inflate(R.layout.item_chat_preview,p0,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return  chatsList.size
    }

    override fun onBindViewHolder(p0: ViewHolder, position: Int) {
        p0.bind(chatsList[position])
    }

    private fun loadAllChats(){
        databaseReference!!.child("Chats").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Log.d("ChatPreview",p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.value !=  null){
                Log.d("ChatPreview","LoadAllChatsOnChanged")
                val value = p0.value as HashMap<String,Any>
                var countNewMessage = 0

                value.forEach{
                   var map = it.value as HashMap<String,HashMap<String,String>>
                    map.forEach {
                          if(it.value["messageTo"].equals(mAuth.currentUser!!.uid)){
                              if(it.value["new"].equals("true")){
                                  countNewMessage++
                                  Log.d("ChatPreview","new messages")
                              }
                          }
                        }

                    getLastMessage(it.key,countNewMessage)
                    countNewMessage = 0
                   }
               }
            }
        })
    }

    fun getLastMessage(chatId:String,countNewMessage:Int){
        var chatModel = ChatModel()
            databaseReference!!.child("Chats").child(chatId).limitToLast(1).addChildEventListener(object : ChildEventListener{
                override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                }

                override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                    Log.d("ChatPreview","last message changed")
                }

                override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                    Log.d("ChatPreview","last message added")
                    val messageModel= p0.getValue(MessageModel::class.java)
                    if(messageModel!!.messageTo.equals(mAuth.currentUser!!.uid)||
                            messageModel.messageFrom!!.equals(mAuth.currentUser!!.uid)) {
                        if(!messageModel.messageFrom.equals(mAuth.currentUser!!.uid)){
                            chatModel.userId = messageModel.messageFrom
                        }else{
                            chatModel.userId = messageModel.messageTo
                        }
                        chatModel.newMessagesCounter = countNewMessage
                        chatModel.lastMessage = messageModel.messageContent
                        chatModel.date = messageModel.messageTime
                        initializeChatModel(chatModel)
                    }
                }

                override fun onChildRemoved(p0: DataSnapshot) {
                }

                override fun onCancelled(p0: DatabaseError) {
                }

            })
    }

    fun initializeChatModel(chatModel: ChatModel){
        databaseReference.child("Users").child(chatModel.userId!!).addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Log.d("ChatPreview",p0.message)
            }
            override fun onDataChange(p0: DataSnapshot) {
                val value = p0.getValue(UserModel::class.java)
                chatModel.userName = value!!.nickname
                chatModel.imgUrl = value!!.thumb_image
                if(!contains(chatModel.userId!!)) {
                    chatsList.add(chatModel)
                }
                notifyDataSetChanged()
            }
        })
    }

    fun contains(userId:String):Boolean{
        chatsList.forEach {
            if(it!=null) {
                if(it!!.userId.equals(userId)){
                    it.newMessagesCounter = 0
                    return true
                }
            }
        }
        return false
    }

    inner class ViewHolder(itemView:View): RecyclerView.ViewHolder(itemView),View.OnClickListener{
        var userImg:CircleImageView? = null
        var date:TextView? = null
        var userName:TextView? = null
        var lastMessage:TextView? = null
        var newMessagesCount:TextView? = null

        init {
            userImg = itemView.findViewById(R.id.chat_preview_image)
            date = itemView.findViewById(R.id.chat_preview_date)
            userName = itemView.findViewById(R.id.chat_preview_name)
            lastMessage = itemView.findViewById(R.id.chat_preview_lastMessage)
            newMessagesCount = itemView.findViewById(R.id.chat_preview_newMessagesCount)
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            var intent = Intent(context, MessageChatActivity::class.java)
            intent.putExtra("userId",chatsList[adapterPosition]!!.userId)
            intent.putExtra("userImg",chatsList[adapterPosition]!!.imgUrl)
            intent.putExtra("userName",chatsList[adapterPosition]!!.userName)
            context.startActivity(intent)
        }

        fun bind(chatModel: ChatModel){
            date!!.text = chatModel.showNormalData(chatModel.date!!)
            userName!!.text = chatModel.userName
            lastMessage!!.text = chatModel.lastMessage
            if(chatModel.newMessagesCounter == 0){
                newMessagesCount!!.visibility = View.GONE
            }else{
                newMessagesCount!!.text = chatModel.newMessagesCounter.toString()
            }
            Picasso.get()
                .load(chatModel.imgUrl)
                .placeholder(R.mipmap.ic_logo)
                .into(userImg)
        }
    }
}