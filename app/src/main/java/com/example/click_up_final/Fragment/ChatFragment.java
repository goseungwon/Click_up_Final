package com.example.click_up_final.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.click_up_final.R;
import com.google.android.material.tabs.TabLayout;

public class ChatFragment extends Fragment {
    private ViewGroup rootView;
    Fragment fragment_chat, fragment_friendchat, fragment_everychat;

    @Override
    public void onStart() {
        fragment_chat = new ChatWithFriendFragment();
        setChildFragment(fragment_chat);
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_chat, container, false);

        fragment_chat = new ChatWithFriendFragment();
        fragment_friendchat = new ChatSecondFragment();
        fragment_everychat = new ChatWithOpenchatFragment();

        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.tabs);
        
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                
                Fragment selected = null;
                
                if (position == 0) {
                    selected = fragment_chat;
                    setChildFragment(selected);
                } 
                else if (position == 1) {
                    // 임시
                    selected = fragment_friendchat;
                    setChildFragment(selected);
                }
                
                else if (position == 2) {
                    selected = fragment_everychat;
                    setChildFragment(selected);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        return rootView;
    }

    private void setChildFragment(Fragment child) {
        FragmentTransaction childFrag = getChildFragmentManager().beginTransaction();

        if(!child.isAdded()) {
            childFrag.replace(R.id.child_fragment_container, child);
            childFrag.addToBackStack(null);
            childFrag.commit();
        }
    }
}