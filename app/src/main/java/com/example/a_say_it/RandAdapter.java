package com.example.a_say_it;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RandAdapter extends RecyclerView.Adapter<RandAdapter.ViewHolder>{

    public ArrayList<Item> items = new ArrayList<Item>();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView word, pronunciation,definition;

        public ViewHolder(View view) {
            super(view);
            word = view.findViewById(R.id.word);
            pronunciation = view.findViewById(R.id.pronunciation);
            definition = view.findViewById(R.id.definition);
        }

        public void setItem(Item item) {
            word.setText(item.word);
            pronunciation.setText(item.pronunciation);
            definition.setText(item.definition);
        }
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.rand_item, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Item item = items.get(position);
        viewHolder.setItem(item);
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(Item item) {
        items.add(item);
    }

    public static class Item {
        String word, pronunciation, definition;

        public Item(Item item){
            this(item.word, item.pronunciation, item.definition);
        }

        public Item(String word, String pronunciation, String definition) {
            this.word = word;
            this.pronunciation = definition;
            this.definition = pronunciation;
        }
    }
}