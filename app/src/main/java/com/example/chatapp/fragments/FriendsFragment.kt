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
import com.example.chatapp.adapters.FriendsRecyclerViewAdapter
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_friends.*
import kotlinx.android.synthetic.main.search_bottom_toolbar.*


class FriendsFragment : Fragment(),android.support.v7.widget.SearchView.OnQueryTextListener {

    private var mRef:DatabaseReference? = null
    private var adapter:FriendsRecyclerViewAdapter? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_friends, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).setSupportActionBar(SearchToolbar)
        (requireActivity() as AppCompatActivity).supportActionBar!!.title = ""
        setHasOptionsMenu(true)

        var linearLayoutManager = LinearLayoutManager(context)
        mRef= FirebaseDatabase.getInstance().reference
        adapter = FriendsRecyclerViewAdapter(mRef!!,context!!)
        friendsFragmentRecyclerView.layoutManager = linearLayoutManager
        friendsFragmentRecyclerView.adapter = adapter

    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu!!.clear()
        super.onCreateOptionsMenu(menu, inflater)
        inflater!!.inflate(R.menu.search_toolbar,menu)
        val menuItem = menu!!.findItem(R.id.toolbarSearchButton)
        val searchView = menuItem.actionView as android.support.v7.widget.SearchView
        menuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener{
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                Log.d("Adapter","Open")
               if (!adapter!!.filterList.contains(null)&&!adapter!!.allUsersList.contains(null)){
                   adapter!!.filterList.add(null)
                   adapter!!.allUsersList.add(null)
                   Log.d("Adapter","not Contains")
               }else{
                   Log.d("Adapter","Contains")
               }
                adapter!!.loadAllUsers()
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                adapter!!.allUsersList.clear()
                adapter!!.filterList.clear()
                adapter!!.loadFriendList()
                return true
            }

        })

        searchView.setOnQueryTextListener(this)
        var button = searchView.findViewById<AppCompatImageView>(android.support.v7.appcompat.R.id.search_close_btn)
        button.setOnClickListener {
            searchView.setQuery("", false)
            searchView.clearFocus()
            adapter!!.loadFriendList()
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }


    override fun onQueryTextChange(newText: String?): Boolean {
        adapter!!.filter.filter(newText)
        return true
    }


}

