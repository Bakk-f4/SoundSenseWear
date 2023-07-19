package com.example.soundsensewear.helpers;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.soundsensewear.R;

public class SettingsActivityWear extends AppCompatActivity {
    private NumberPicker numberPicker;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_wear);

        //load data from sharedPreference
        sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        Integer userDelay = sharedPreferences.getInt("timeout", 60);

        numberPicker = findViewById(R.id.npSeconds);
        numberPicker.setMinValue(10);
        numberPicker.setMaxValue(180);
        numberPicker.setValue(userDelay);

        requestStoragePermissions();



        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                // Esegui l'azione desiderata quando il valore viene cambiato
                Log.d("Picker", "Nuovo valore: " + newVal);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("timeout", newVal);
                editor.apply();
            }
        });
    }

    public void onGoToSettingsListActivity(View view){
        // start the audio helper activity
        Intent intent = new Intent(this, SettingsListActivity.class);
        startActivity(intent);
    }

    private void requestStoragePermissions() {
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        int requestCode = 0x100;
        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }


}
