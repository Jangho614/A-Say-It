package com.example.a_say_it;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PronounceFragment extends Fragment {
    private boolean isRecording = false;
    private boolean forceStop = false;
    private final byte[] speechData = new byte[16000 * 10 * 2]; // 10초 버퍼
    private int lenSpeech = 0;
    private ExecutorService executor;

    Button eval_btn, tts_btn, play_btn;
    EditText script_txt;
    TextView code, said_word, score;

    private AudioTrack audioTrack;
    private TextToSpeech tts;
    private final String TTS_ID = "TTS";

    Gson gson = new Gson();

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                }
            });


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pronounce, container, false);
        script_txt = view.findViewById(R.id.setScript);
        eval_btn = view.findViewById(R.id.eval_btn);
        code = view.findViewById(R.id.response_code);
        said_word = view.findViewById(R.id.SaidWord);
        score = view.findViewById(R.id.WordScore);
        tts_btn = view.findViewById(R.id.tts_btn);
        play_btn = view.findViewById(R.id.play_record);
        executor = Executors.newSingleThreadExecutor();


        tts = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                tts.setLanguage(Locale.ENGLISH);
            }
        });
        tts_btn.setVisibility(View.GONE);

        // 런타임 권한 요청
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
        }
        eval_btn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                if (isRecording) {
                    forceStop = true;
                    saveClientId(code.getText().toString());
                    eval_btn.setBackgroundResource(R.drawable.pn_mic_btn);
                } else {
                    startRecording();
                    eval_btn.setBackgroundResource(R.drawable.pn_mic_btn_stop);
                }
            }
        });
        tts_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tts.speak(said_word.getText(), TextToSpeech.QUEUE_FLUSH, null, TTS_ID);
            }
        });
        play_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (lenSpeech > 0) {
                    playRecording();
                } else {
                    Toast.makeText(getContext(), "녹음이 없어요", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

    private void startRecording() {
        executor.execute(() -> {
            try {
                SendMessage("녹음중...", 1);
                recordSpeech();
                SendMessage("분석중...", 2);
                String json = sendDataAndGetResult();
                JsonResponse jsonResponse = gson.fromJson(json, JsonResponse.class);
                getActivity().runOnUiThread(() -> {
                    SetResult(jsonResponse.getReturn_object().getRecognized(), jsonResponse.getReturn_object().getScore());
                });
            } catch (RuntimeException e) {
                getActivity().runOnUiThread(() -> code.setText("Error: " + e.getMessage()));
                Log.d("TAG", "Error: " + e.getMessage());
            }
        });
    }

    private void recordSpeech() throws RuntimeException {
        try {
            int bufferSize = AudioRecord.getMinBufferSize(
                    16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            AudioRecord audio = new AudioRecord(
                    MediaRecorder.AudioSource.VOICE_RECOGNITION, 16000,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

            lenSpeech = 0;
            if (audio.getState() != AudioRecord.STATE_INITIALIZED) {
                throw new RuntimeException("ERROR: Failed to initialize audio device. Allow app to access microphone");
            }

            short[] inBuffer = new short[bufferSize];
            forceStop = false;
            isRecording = true;
            audio.startRecording();

            while (!forceStop) {
                int ret = audio.read(inBuffer, 0, bufferSize);
                for (int i = 0; i < ret; i++) {
                    int maxLenSpeech = 16000 * 10;
                    if (lenSpeech >= maxLenSpeech) {
                        forceStop = true;
                        break;
                    }
                    speechData[lenSpeech * 2] = (byte) (inBuffer[i] & 0x00FF);
                    speechData[lenSpeech * 2 + 1] = (byte) ((inBuffer[i] & 0xFF00) >> 8);
                    lenSpeech++;
                }
            }
            audio.stop();
            audio.release();
            isRecording = false;
        } catch (Throwable t) {
            throw new RuntimeException(t.toString());
        }
    }

    // 녹음된 내용을 재생하는 메서드
    private void playRecording() {
        // AudioTrack 초기화
        int bufferSize = AudioTrack.getMinBufferSize(
                16000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

        AudioTrack audioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC, 16000,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                bufferSize, AudioTrack.MODE_STREAM);

        // AudioTrack 시작
        audioTrack.play();

        // 녹음된 데이터를 재생
        audioTrack.write(speechData, 0, lenSpeech * 2);

        // 재생 완료 후 정지
        audioTrack.stop();
        audioTrack.release();
        audioTrack = null;

        Toast.makeText(getContext(), "재생완료", Toast.LENGTH_SHORT).show();
    }

    public String sendDataAndGetResult() {
        String openApiURL = "http://aiopen.etri.re.kr:8000/WiseASR/Pronunciation";
        String accessKey = "8fa896a1-dc2a-48d6-a1b6-5ec44804f84f";
        String languageCode = "english";
        String script = script_txt.getText().toString();

        Map<String, Object> request = new HashMap<>();
        Map<String, String> argument = new HashMap<>();

        String audioContents = Base64.encodeToString(speechData, 0, lenSpeech * 2, Base64.NO_WRAP);
        if (!script.isEmpty()) {
            argument.put("script", script);
        }
        argument.put("language_code", languageCode);
        argument.put("audio", audioContents);
        request.put("access_key", accessKey);
        request.put("argument", argument);

        try {
            URL url = new URL(openApiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setRequestProperty("Authorization", accessKey);

            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.write(gson.toJson(request).getBytes("UTF-8"));
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();
            if (responseCode == 200) {
                InputStream is = new BufferedInputStream(con.getInputStream());
                return readStream(is);
            } else {
                return "ERROR: " + responseCode;
            }

        } catch (Throwable t) {
            return "ERROR: " + t.toString();
        }
    }

    public static String readStream(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(in), 1000);
        for (String line; (line = r.readLine()) != null; ) {
            sb.append(line);
        }
        in.close();
        return sb.toString();
    }

    private void saveClientId(String clientId) {
        // SharedPreferences 저장 로직 구현
        getActivity().getSharedPreferences("PREFS_NAME", MODE_PRIVATE)
                .edit()
                .putString("client-id", clientId)
                .apply();
    }

    private void SendMessage(String message, int type) {
        getActivity().runOnUiThread(() -> code.setText(message));
    }

    public void SetResult(String Word, String Score) {
        if (!Word.equals("<?xml v") || !Score.equals("<?xml vers")) {
            code.setText("분석 완료!");
            said_word.setText(Word);
            score.setText(Score);
            tts_btn.setVisibility(View.VISIBLE);
            AddRecyclerViewItem(Word, Score);
        } else {
            code.setText("분석 실패..");
            said_word.setText("단어를 인식하지 못했습니다");
            score.setText("다시 시도해주세요");
            tts_btn.setVisibility(View.GONE);
        }
        saveClientId(said_word.getText().toString());
        saveClientId(score.getText().toString());
    }
    public void AddRecyclerViewItem(String word, String score) {
        MyWordAdapter adapter = new MyWordAdapter();
        adapter.addItem(new MyWordAdapter.Item(word, score, ""));
    }
}