package com.example.soundsensewear.audio;

import static com.example.soundsensewear.helpers.SettingsListActivity.readJsonFromFile;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.soundsensewear.R;
import com.example.soundsensewear.helpers.AudioHelperActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.support.audio.TensorAudio;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.audio.classifier.AudioClassifier;
import org.tensorflow.lite.task.audio.classifier.Classifications;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class AudioClassificationAcitvity extends AudioHelperActivity {

    private TextView tvAudioOutput;
    private String model = "lite-model_yamnet_classification_tflite_1.tflite";
    private final String TAG = "AudioIdentificationActivity";
    private AudioRecord audioRecord;
    private TimerTask timerTask;
    private AudioClassifier audioClassifier;
    private TensorAudio tensorAudio;
    private static long minutes;

    private String  eventTime;
    private HashMap<String, Long> userClassification;



    private static Boolean isRecording;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tvAudioOutput = findViewById(R.id.tvAudioOutput);
        isRecording = false;

        //creazione canale comunicazione per notifiche
        createNotificationChannel();

        //inizialize audioClassifier from TF model
        try {
            audioClassifier = AudioClassifier.createFromFile(this, model);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //inizialize audio recorder for classifying audio
        tensorAudio = audioClassifier.createInputTensorAudio();
    }

    @Override
    protected void onPause() {
        super.onPause();
        View myCurrentView = findViewById(R.id.activity_audio_helper_dismiss_layout);
        //stopRecordingL(myCurrentView);
    }

    @Override
    protected void onResume(){
        super.onResume();
        tvAudioOutput.setText("SoundSense");
        View myCurrentView = findViewById(R.id.activity_audio_helper_dismiss_layout);
        initDelayTime();
        try {
            initCategories();
            if (isRecording){
                stopRecordingL(myCurrentView);
                startRecordingL(myCurrentView);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void initDelayTime(){
        //impostazioni delay notifiche (picker)
        SharedPreferences sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        minutes = sharedPreferences.getInt("timeout", 60)*1000;
    }

    private void initCategories() throws JSONException, IOException {
        userClassification = new HashMap<String, Long>();

        //Se il json non esiste nella dir Files lo copio da assets (assets è una dir readonly)
        File file = new File(this.getFilesDir() + "/short_list_category.json");

        //TODO inserire TAG in giro per il progetto, eliminare try/catch dove possibile
        if (!file.exists())
            Toast.makeText(AudioClassificationAcitvity.this, "Please select categories", Toast.LENGTH_LONG).show();
        else {
            Log.i("fileList", "il file esiste");

            JSONArray savedJsonArray = null;
            savedJsonArray = readJsonFromFile(this, "short_list_category.json");

            //per ogni elemento in savedJsonArray
            for (int i = 0; i < savedJsonArray.length(); i++) {
                JSONObject jsonObject = savedJsonArray.getJSONObject(i);
                if (jsonObject.get("checked").toString().equals("true"))
                    userClassification.put(jsonObject.get("display_name").toString(), 0L);

                Log.i("userClassification", jsonObject.get("display_name").toString());
            }
        }
    }


    public void startRecordingL(View view) {
        super.startRecording(view);
        if (isRecording)
            return;
        isRecording = true;

        audioRecord = audioClassifier.createAudioRecord();
        audioRecord.startRecording();



        timerTask = new TimerTask() {
            @Override
            public void run() {
                tensorAudio.load(audioRecord);
                List<Classifications> output = audioClassifier.classify(tensorAudio);

                String categoryLabel = "";

                //filtering out classifications with low probability
                List<Category> finalOutput = new ArrayList<>();
                for (Classifications classifications : output) {
                    for (Category category : classifications.getCategories()) {
                        //if score is higher than 30% possibility...
                        categoryLabel = category.getLabel();
                        if (category.getScore() > 0.3f && userClassification.get(categoryLabel) != null) {
                            finalOutput.add(category);
                            eventTime = getCurrentDateTime();
                            // TODO METTERE VIBRAZIONE

                            if(checkTime(categoryLabel)){
                                // TODO IMPLEMENTARE CONDIZIONE DAL MENU IMPOSTAZIONI
                                int notificationId = Integer.parseInt(eventTime.replace(":",""));
                                myMessage("Abbiamo rilevato un evento audio: " + categoryLabel, notificationId);
                                Log.i("category.getIndex()", "" + notificationId);
                            }
                        }
                    }
                }

                //Creating a multiline string with the filtered results
                StringBuilder outputStr = new StringBuilder();
                for (Category category : finalOutput) {
                    outputStr.append(category.getLabel())
                            .append(": ").append(eventTime).append("\n");
                    Log.i(TAG, outputStr.toString());
                }

                //updating the textView for output
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (finalOutput.isEmpty())
                            tvOutput.setText("Could not classify audio");
                        tvOutput.setText(outputStr.toString());
                    }
                });
            }
        };
        //after 1 second it start and every 0.5 second will classify audio
        new Timer().scheduleAtFixedRate(timerTask, 1, 500);
    }


    public void stopRecordingL(View view) {
        super.stopRecording(view);
        if (isRecording) {
            timerTask.cancel();
            audioRecord.stop();
        }
        isRecording = false;
    }

    public String getCurrentDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "ASNotificationN"; //getString(R.string.channel_name);
            String description = "ASNotificationD"; //getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("ASNotificationID", name, importance);
            channel.setDescription(description);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void myMessage(String message, Integer notificationId) {

        // Creazione della notifica
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "ASNotificationID")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("AudioSense")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);


        //redirezione click su notifica
        Intent intent = new Intent(this, AudioClassificationAcitvity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details..
            return;
        }
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationId, builder.build());
    }


    public Boolean checkTime(String category){
        long currentTime = System.currentTimeMillis();
        if ((currentTime - userClassification.get(category)) > minutes) {
            // Aggiorna il tempo dell'ultima chiamata
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                userClassification.replace(category, currentTime);
            }
            return true;
        }
        // La funzione non può essere chiamata perché sono passati meno di 5 minuti
        Log.i(TAG, "EMAIL TIMEOUT NON ANCORA TERMINATO");
        Log.i(TAG, (currentTime - userClassification.get(category) > minutes) +"");
        return false;

    }


}
