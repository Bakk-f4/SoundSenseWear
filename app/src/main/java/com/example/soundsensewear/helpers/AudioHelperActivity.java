package com.example.soundsensewear.helpers;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.wear.widget.DismissibleFrameLayout;

import com.example.soundsensewear.R;

public class AudioHelperActivity extends AppCompatActivity {

    protected TextView tvOutput;
    protected TextView tvSpecs;
    protected Button bttStartRecording;
    protected Button bttStopRecording;
    protected ImageButton bttSettings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_helper_wear);

        tvOutput = findViewById(R.id.tvAudioOutput);
        //tvSpecs = findViewById(R.id.tvAudioSpecs);
        bttStartRecording = findViewById(R.id.bttStartRecording);
        bttStopRecording = findViewById(R.id.bttStopRecording);
        bttSettings = findViewById(R.id.bttSettings);

        //disable stop recording at start of the activity
        bttStopRecording.setEnabled(false);

        //check if permission is granted
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 0);
            }
        }
    }

    public void startRecording(View view){
        bttStartRecording.setEnabled(false);
        bttStopRecording.setEnabled(true);
    }

    public void stopRecording(View view){
        bttStartRecording.setEnabled(true);
        bttStopRecording.setEnabled(false);
    }

    public void onGoToSettings(View view){
        // start the audio helper activity
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void onGoToSettingsWear(View view){
        // start the audio helper activity
        Intent intent = new Intent(this, SettingsActivityWear.class);
        startActivity(intent);
    }

}