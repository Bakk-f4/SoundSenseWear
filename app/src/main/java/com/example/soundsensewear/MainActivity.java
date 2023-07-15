package com.example.soundsensewear;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.soundsensewear.audio.AudioClassificationAcitvity;

// TODO IMPLEMENTARE VIBRAZIONE + VISUALIZZAZIONE A SCHERMO DELLA CATEGORIA SCRITTA MOBILE AND WEAR OS
// TODO OPZIONE IN SETTING VIBRAZIONE ON OR OFF MOBILE AND WEAR OS
// TODO pulizia delle email, nome, cognome SOLO WEAR OS
// TODO ADATTARE GRAFICA PER WEAR OS
// TODO Slider al posto dei minuti in secondi ( circa da 10 a SIVEDE ) MOBILE AND WEAR OS
// TODO semplificare selezione categorie: speech reconization oppure SIVEDE MOBILE AND WEAR OS
// TODO cambiare grafica schermata home MOBILE AND WEAR OS

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onGoToAudioActivity(View view){
        // start the audio helper activity
        Intent intent = new Intent(this, AudioClassificationAcitvity.class);
        startActivity(intent);
    }
}