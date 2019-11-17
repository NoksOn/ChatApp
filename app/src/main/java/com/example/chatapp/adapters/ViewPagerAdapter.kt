package com.example.chatapp.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.example.chatapp.fragments.ChatsFragment
import com.example.chatapp.fragments.FriendsFragment

class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm){



    override fun getPageTitle(position: Int): CharSequence? {
        when(position) {
            0 ->
                return "CHATS"
            1 ->
                return "FRIENDS"
        }
        return null!!
    }

    override fun getItem(position: Int): Fragment {
        when(position){
            0->
                return ChatsFragment()

            1->
                return FriendsFragment()
        }
        return null!!
    }

    override fun getCount(): Int {
        return 2
    }
}