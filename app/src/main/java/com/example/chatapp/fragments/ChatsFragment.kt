package com.example.chatapp.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.*

import com.example.chatapp.R
import com.example.chatapp.adapters.ChatsRecyclerViewAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_chats.*
import kotlinx.android.synthetic.main.search_bottom_toolbar.*


class ChatsFragment : Fragment(){

    private var adapter:ChatsRecyclerViewAdapter? = null
    private var databaseReference:DatabaseReference? = null
    private var databaseAuth:FirebaseAuth? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var linearLayoutManager = LinearLayoutManager(context)


        databaseReference= FirebaseDatabase.getInstance().reference
        databaseAuth = FirebaseAuth.getInstance()

        adapter = ChatsRecyclerViewAdapter(databaseReference!!,context!!,databaseAuth!!)
        fragment_chat_body.layoutManager = linearLayoutManager
        fragment_chat_body.adapter = adapter
    }


}
