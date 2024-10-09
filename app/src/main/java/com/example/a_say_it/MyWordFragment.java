package com.example.a_say_it;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MyWordFragment extends Fragment {
    public RecyclerView wordRecycle;
    static MyWordAdapter adapter = new MyWordAdapter();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_word, container, false);
        wordRecycle = view.findViewById(R.id.recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        wordRecycle.setLayoutManager(layoutManager);
        wordRecycle.setAdapter(adapter);

        return view;
    }
}