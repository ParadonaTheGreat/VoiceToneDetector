package com.example.voicetonedetector;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
/*
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpHeaders;
 */

public class MainActivity extends AppCompatActivity {

    private final String API_TOKEN = "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";

    private MediaRecorder recorder;
    private MediaPlayer player;
    private String filePath;
    private static final String TAG = "VoiceToneDetector";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    TextView resultText;
    private String result;
    Handler h;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        filePath = getExternalFilesDir(null).getAbsolutePath() + "/recordedFile123.mp3";
        Log.d(TAG, "File path: " + filePath);
        h = new Handler();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_RECORD_AUDIO_PERMISSION);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        resultText = findViewById(R.id.resultText);
    }

    public void startRecPressed(View view) {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setOutputFile(filePath);

        try {
            recorder.prepare();
            recorder.start();
            Log.d(TAG, "Recording started");
        } catch (IOException e) {
            Log.e(TAG, "Recording failed", e);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Illegal state during recording", e);
        }
    }

    public void stopRecPressed(View view) {
        if (recorder != null) {
            try {
                recorder.stop();
                Log.d(TAG, "Recording stopped");
            } catch (RuntimeException stopException) {
                Log.e(TAG, "Stop failed", stopException);
            }
            recorder.release();
            recorder = null;
        }

        File file = new File(filePath);
        if (file.exists()) {
            Log.d(TAG, "Recording saved to: " + filePath);
        } else {
            Log.e(TAG, "Recording file not found");
        }

        h.postDelayed(r,100);
    }

    public void startPlayingPressed(View view) {
        if (player != null) {
            player.release();
        }
        player = new MediaPlayer();
        try {
            player.setDataSource(filePath);
            player.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build());
            player.prepare();
            player.start();
            Log.d(TAG, "Playback started");
            player.setOnCompletionListener(mp -> {
                Log.d(TAG, "Playback completed");
                mp.release();
                player = null;
            });
        } catch (IOException e) {
            Log.e(TAG, "Playback failed", e);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Illegal argument during playback", e);
        }
    }

    public void stopPlayingPressed(View view) {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
            Log.d(TAG, "Playback stopped");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Record audio permission granted");
            } else {
                Log.e(TAG, "Record audio permission denied");
            }
        }
    }

    public void processBtn(View view) {
        resultText.setText(result);
    }
    Runnable r = new Runnable() {
        @Override
        public void run() {
            try {
                Log.e(TAG, "Processing Started");

                String[] command = {
                        "curl",
                        "https://api-inference.huggingface.co/models/ehcalabres/wav2vec2-lg-xlsr-en-speech-emotion-recognition",
                        "-X", "POST",
                        "--data-binary", "@" + filePath,
                        "-H", "Authorization: Bearer " + API_TOKEN};
                Process execute = Runtime.getRuntime().exec(command);

                BufferedReader stdInput = new BufferedReader(new InputStreamReader(execute.getInputStream()));
                StringBuilder output = new StringBuilder();
                String line;

                while ((line = stdInput.readLine()) != null) {
                    output.append(line);
                }
                result = output.toString();
                Log.e(TAG, output.toString());
                Log.e(TAG, "Processing Ended");
            } catch (IOException e) {
                Log.e(TAG, "Processing Failed");
                throw new RuntimeException(e);
            }
        }
    };
}

