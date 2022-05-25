package com.example.click_up_final.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.click_up_final.R;

public class ChatSecondFragment extends Fragment {
    private ViewGroup rootView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_chat_second, container, false);

        return rootView;
    }
}
