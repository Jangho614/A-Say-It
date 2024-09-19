package com.example.a_say_it;

import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;
import java.io.IOException;

public class PronounceFragment extends Fragment {
    Button eval_btn;
    EditText script_txt;
    MediaRecorder recorder;
    String filename;
    boolean record = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pronounce, container, false);
        script_txt = view.findViewById(R.id.setScript);
        eval_btn = view.findViewById(R.id.eval_btn);

        eval_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!record){
                    record = true;
                    eval_btn.setBackgroundResource(R.drawable.pn_mic_btn_stop);
                }else{
                    record = false;
                    eval_btn.setBackgroundResource(R.drawable.pn_mic_btn);
                }
            }
        });

        return view;
    }
}