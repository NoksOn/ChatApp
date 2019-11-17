package com.example.chatapp.adapters

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.chatapp.MessageChatActivity
import com.example.chatapp.R
import com.example.chatapp.models.UserModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class FriendsRecyclerViewAdapter(var databaseReference: DatabaseReference, var context: Context):
    RecyclerView.Adapter<FriendsRecyclerViewAdapter.ViewHolder>(),Filterable{

    var allUsersList:ArrayList<UserModel?> = ArrayList<UserModel?>()
    var filterList:ArrayList<UserModel?> = ArrayList<UserModel?>()
    private var mCurrentUser: FirebaseUser? = null
    private val VIEW_TYPE_ITEM:Int = 1
    private val VIEW_TYPE_LOADING:Int = 0

    //TODO Make childValueEventListener


    init {
        mCurrentUser = FirebaseAuth.getInstance().currentUser
        loadFriendList()
        Log.d("Adapter","init")
    }


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): FriendsRecyclerViewAdapter.ViewHolder {

        var view:View? = null
        if(viewType == VIEW_TYPE_ITEM){
            view = LayoutInflater.from(context).inflate(R.layout.friend_item,viewGroup,false)
            return DataViewHolder(view)
        }else{
            view = LayoutInflater.from(context).inflate(R.layout.friends_item_header_title,viewGroup,false)
            return ViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return allUsersList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if(allUsersList[position] != null)
            VIEW_TYPE_ITEM
        else
            VIEW_TYPE_LOADING

    }



    override fun onBindViewHolder(p0: FriendsRecyclerViewAdapter.ViewHolder, p1: Int) {
        if(p0 is DataViewHolder){
            if(allUsersList.get(p1)!!.IsFriend){
                p0.addToFriend!!.visibility = View.GONE
            }else{
                p0.addToFriend!!.visibility = View.VISIBLE
            }
            p0.nickName!!.text = allUsersList[p1]!!.nickname
            Picasso.get()
                .load(allUsersList[p1]!!.thumb_image)
                .placeholder(R.mipmap.ic_logo)
                .into(p0.userImg)

        }

    }


    fun loadFriendList(){
        Log.d("Adapter","LoadFriends")
        databaseReference.child("Users").child(mCurrentUser!!.uid).addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Log.d("Adapter","Cannot read data")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.value as HashMap<String, HashMap<String, Any>>
                 if(!value["friends"].toString().equals("null")){
                    val map = value.get("friends") as HashMap<String,String>
                    map.forEach {

                        databaseReference.child("Users").child(it.value).addValueEventListener(object : ValueEventListener{
                            override fun onCancelled(p0: DatabaseError) {
                                Log.d("Adapter","Cannot read data 90 lines")
                            }
                            override fun onDataChange(p0: DataSnapshot) {
                                val userModel = p0.getValue(UserModel::class.java)
                                userModel!!.IsFriend = true
                                userModel.userId = p0.key
                                if(!contains(userModel)) {
                                    allUsersList.add(userModel)
                                    filterList.add(userModel)
                                    notifyDataSetChanged()
                                }
                            }
                        })
                    }
                 }else{
                     allUsersList.add(null)
                     notifyDataSetChanged()
                     loadAllUsers()
                 }
            }

        })
    }
    fun loadAllUsers(){
        databaseReference.child("Users").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Log.d("Adapter","Cannot read data")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
               val value = dataSnapshot.value as HashMap<String,HashMap<String,Any>>
                value.forEach{
                    val map = it.value
                    if(!it.key.equals(mCurrentUser!!.uid)) {
                        var user = UserModel()
                        user.userId = it.key
                        user.nickname = map["nickname"].toString()
                        user.thumb_image = map["thumb_image"].toString()
                        user.image = map["image"].toString()
                        if(!contains(user)){
                            allUsersList.add(user)
                            filterList.add(user)
                            notifyDataSetChanged()
                        }
                    }
                }
            }

        })
    }

    fun contains(userModel: UserModel):Boolean{
        allUsersList.forEach {
            if(it!=null) {
                if(it!!.userId!!.equals(userModel.userId)){
                    return true
                }
            }
        }
        return false
    }

    open inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        init {
        }
    }

    inner class DataViewHolder(itemView: View): ViewHolder(itemView),View.OnClickListener {


        var userImg:CircleImageView? = null
        var nickName:TextView? = null
        var addToFriend:Button? = null
        var sendMessage:Button? = null

        init {
            userImg = itemView.findViewById(R.id.friendItemImg)
            nickName = itemView.findViewById(R.id.friendItemName)
            addToFriend = itemView.findViewById(R.id.friendItemAddFriend)
            sendMessage = itemView.findViewById(R.id.friendItemSendMessage)
            addToFriend!!.setOnClickListener(this)
            sendMessage!!.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            when(v!!.id){
                R.id.friendItemSendMessage ->{
                    var intent = Intent(context,MessageChatActivity::class.java)
                    intent.putExtra("userId",allUsersList[adapterPosition]!!.userId)
                    intent.putExtra("userImg",allUsersList[adapterPosition]!!.thumb_image)
                    intent.putExtra("userName",allUsersList[adapterPosition]!!.nickname)
                    context.startActivity(intent)
                }
                R.id.friendItemAddFriend ->{
                    var userId:String = allUsersList[adapterPosition]!!.userId.toString()
                    databaseReference.child("Users").child(mCurrentUser!!.uid).child("friends").push()
                        .setValue(userId).addOnCompleteListener {
                        task: Task<Void> ->
                            if(task.isSuccessful){
                                Toast.makeText(context,"User add to friend list",Toast.LENGTH_SHORT).show()
                                allUsersList[adapterPosition]!!.IsFriend = true
                                notifyDataSetChanged()
                            }
                            else{
                                Toast.makeText(context,"User not add to friend list",Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
        }
    }

    override fun getFilter(): Filter {
       return object : Filter(){
           private val filterResults = FilterResults()
           override fun performFiltering(constraint: CharSequence?): FilterResults {
               var filtredList:ArrayList<UserModel?> = ArrayList<UserModel?>()
               if(constraint == null || constraint.isEmpty()){
                   filtredList.addAll(filterList)
               }else{
                   val filterPattern:String = constraint.toString().toLowerCase().trim()
                   filterList.forEach{
                       if(it!=null){
                           if(it.nickname!!.toLowerCase().contains(filterPattern)){
                               filtredList.add(it)
                           }
                       }
                   }
               }
               return filterResults.also {
                   it.values = filtredList
               }
           }
           override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                   allUsersList.clear()
                   allUsersList.addAll(results!!.values as ArrayList<UserModel?>)
                   notifyDataSetChanged()
           }
       }
    }







}


