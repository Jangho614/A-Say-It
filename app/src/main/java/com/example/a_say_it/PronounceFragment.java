package com.example.a_say_it;

import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Map;

public class PronounceFragment extends Fragment {
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    Button eval_btn;
    EditText script_txt;
    TextView code, body;
    boolean record = false;
    int maxLenSpeech = 16000 * 45;
    byte[] speechData = new byte[maxLenSpeech * 2];
    int lenSpeech = 0;
    boolean isRecording = false;
    boolean forceStop = false;
    boolean permissionToRecordAccepted = false;
    String[] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pronounce, container, false);
        script_txt = view.findViewById(R.id.setScript);
        eval_btn = view.findViewById(R.id.eval_btn);
        code = view.findViewById(R.id.response_code);
        body = view.findViewById(R.id.response_body);

        // 권한 요청 및 초기화

        eval_btn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                if (!record) {
                    // 녹음 시작
                    record = true;
                    eval_btn.setBackgroundResource(R.drawable.pn_mic_btn_stop);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("TAG","recordStart");
                            recordSpeech();
                        }
                    }).start();
                } else {
                    // 녹음 중지 및 평가 실행
                    forceStop = true;
                    record = false;
                    eval_btn.setBackgroundResource(R.drawable.pn_mic_btn);
                    EvalPronounce(script_txt.getText().toString());
                }
                if(!permissionToRecordAccepted) {
                    Toast.makeText(getActivity(), "녹음 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

    // 권한 요청 결과 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 권한 요청이 성공적으로 이루어졌는지 확인
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            permissionToRecordAccepted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }

        if (!permissionToRecordAccepted) {
            Toast.makeText(getActivity(), "녹음 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
        }
    }
    // 음성 녹음 기능
    public void recordSpeech() {
        try {
            int bufferSize = AudioRecord.getMinBufferSize(
                    16000, // 샘플링 주파수
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            if (ActivityCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            AudioRecord audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    16000,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize);

            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                throw new RuntimeException("ERROR: 오디오 장치 초기화 실패");
            }

            short[] buffer = new short[bufferSize];
            lenSpeech = 0;
            isRecording = true;
            audioRecord.startRecording();

            while (!forceStop) {
                int read = audioRecord.read(buffer, 0, bufferSize);
                for (int i = 0; i < read; i++) {
                    if (lenSpeech >= maxLenSpeech) {
                        forceStop = true;
                        break;
                    }
                    speechData[lenSpeech * 2] = (byte) (buffer[i] & 0x00FF);
                    speechData[lenSpeech * 2 + 1] = (byte) ((buffer[i] & 0xFF00) >> 8);
                    lenSpeech++;
                }
            }
            audioRecord.stop();
            audioRecord.release();
            isRecording = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 음성 데이터를 서버로 전송
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void EvalPronounce(String script) {
        String openApiURL = "http://aiopen.etri.re.kr:8000/WiseASR/Pronunciation";
        String accessKey = "8fa896a1-dc2a-48d6-a1b6-5ec44804f84f"; // 자신의 API Key 사용
        String languageCode = "english";

        // 녹음된 음성을 Base64로 인코딩
        String audioContents = Base64.encodeToString(speechData, 0, lenSpeech * 2, Base64.NO_WRAP);
        Gson gson = new Gson();

        Map<String, Object> request = new HashMap<>();
        Map<String, String> argument = new HashMap<>();

        argument.put("language_code", languageCode);
        argument.put("script", script);
        argument.put("audio", audioContents);

        request.put("access_key", accessKey);
        request.put("argument", argument);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(openApiURL);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setDoOutput(true);

                    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                    wr.write(gson.toJson(request).getBytes("UTF-8"));
                    wr.flush();
                    wr.close();

                    int responseCode = con.getResponseCode();
                    if (responseCode == 200) {
                        InputStream is = new BufferedInputStream(con.getInputStream());
                        String responseBody = readStream(is);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                body.setText(responseBody);
                                code.setText(String.valueOf(responseCode));
                            }
                        });
                    } else {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                body.setText("Error: " + responseCode);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // 서버 응답을 읽는 메서드
    public static String readStream(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }
}
