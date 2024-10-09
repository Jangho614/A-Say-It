package com.example.a_say_it;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class WordsFragment extends Fragment {
    public RecyclerView wordRecycle;
    static RandAdapter adapter = new RandAdapter();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rand_word, container, false);
        wordRecycle = view.findViewById(R.id.recyclerView1);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        wordRecycle.setLayoutManager(layoutManager);
        wordRecycle.setAdapter(adapter);

        int once = 0;
        if(once != 1){
            adapter.addItem(new RandAdapter.Item("Apple", "사과", "ˈæp.əl"));
            adapter.addItem(new RandAdapter.Item("Chair", "의자", "tʃɛr"));
            adapter.addItem(new RandAdapter.Item("Table", "탁자", "ˈteɪ.bəl"));
            adapter.addItem(new RandAdapter.Item("Book", "책", "bʊk"));
            adapter.addItem(new RandAdapter.Item("Flower", "꽃", "ˈflaʊər"));
            once = 0;
        }

        return view;
    }
}