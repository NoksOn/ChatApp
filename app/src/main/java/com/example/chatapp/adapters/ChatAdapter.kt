package com.example.chatapp.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.chatapp.MessageChatActivity
import com.example.chatapp.R
import com.example.chatapp.models.MessageModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_message_chat.*

class ChatAdapter(var databaseReference: DatabaseReference, var context: Context,var chatId:String
                  ,var friendlyName:String, var friendlyImg:String,var activity:MessageChatActivity):
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    var messageList:ArrayList<MessageModel> = ArrayList()
    private var mCurrentUser: FirebaseUser? = null
    private val VIEW_TYPE_SENT:Int = 1
    private val VIEW_TYPE_RECEIVED:Int = 0


    init {
        mCurrentUser = FirebaseAuth.getInstance().currentUser
        loadMessages()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var view:View
        return if(viewType == VIEW_TYPE_SENT){
            view = LayoutInflater.from(context).inflate(R.layout.item_message_sent,viewGroup,false)
            SentMessageHolder(view)
        }else{
            view = LayoutInflater.from(context).inflate(R.layout.item_message_received,viewGroup,false)
            ReceivedMessageHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if(!messageList[position].messageFrom.equals(mCurrentUser!!.uid))
            VIEW_TYPE_RECEIVED
        else
            VIEW_TYPE_SENT
    }

    override fun onBindViewHolder(p0: RecyclerView.ViewHolder, position: Int) {
        var messageModel:MessageModel = messageList[position]
        when(p0.itemViewType){
            VIEW_TYPE_SENT ->{
                (p0 as SentMessageHolder).bind(messageModel)
            }
            VIEW_TYPE_RECEIVED ->{
                (p0 as ReceivedMessageHolder).bind(messageModel)
            }
        }
    }

    private fun loadMessages(){
        databaseReference.child("Chats").child(chatId).addChildEventListener(object : ChildEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                //TODO message is seen
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                if(p0.value!=null) {
                    var messageModel = p0.getValue(MessageModel::class.java)
                    messageModel!!.new = "false"
                    if(mCurrentUser!!.uid.equals(messageModel.messageTo)) {
                        updateIsNew(p0.key)
                    }
                        if(!contains(messageModel!!.messageTime!!)){
                            messageList.add(messageModel!!)
                        }
                        notifyDataSetChanged()
                        activity.chatRecyclerViewBody.scrollToPosition(messageList.size-1)
                }
            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

        })
    }

    fun updateIsNew(key: String?) {
        var map = HashMap<String,Any>()
        map.put("new","false")
        databaseReference.child("Chats").child(chatId).child(key!!).updateChildren(map)
    }

    fun contains(messageTime:Long):Boolean{
        messageList.forEach {
            if(it!=null) {
                if(it!!.messageTime == messageTime){
                    return true
                }
            }
        }
        return false
    }

    inner class SentMessageHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

         var messageText:TextView? = null
         var messageTime:TextView? = null

        init {
            messageText = itemView.findViewById(R.id.text_message_body)
            messageTime = itemView.findViewById(R.id.text_message_time)
        }

        fun bind(messageModel: MessageModel){
            messageTime!!.text = messageModel.showNormalData(messageModel.messageTime!!)
            messageText!!.text = messageModel.messageContent
        }
    }
    inner class ReceivedMessageHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        var messageText:TextView? = null
        var messageTime:TextView? = null
        var receivedImg:CircleImageView? = null
        var receivedName:TextView? = null

        init {
            messageText = itemView.findViewById(R.id.text_message_body)
            messageTime = itemView.findViewById(R.id.text_message_time)
            receivedImg = itemView.findViewById(R.id.image_message_profile)
            receivedName = itemView.findViewById(R.id.text_message_name)
        }

        fun bind(messageModel: MessageModel){
            messageTime!!.text = messageModel.showNormalData(messageModel.messageTime!!)
            messageText!!.text = messageModel.messageContent
            receivedName!!.text = friendlyName
            Picasso.get()
                .load(friendlyImg)
                .placeholder(R.mipmap.ic_logo)
                .into(receivedImg)
        }


    }
}