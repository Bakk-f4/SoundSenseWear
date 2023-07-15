package com.example.soundsensewear.audio;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioRecord;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.example.soundsensewear.helpers.AudioHelperActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.tensorflow.lite.support.audio.TensorAudio;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.audio.classifier.AudioClassifier;
import org.tensorflow.lite.task.audio.classifier.Classifications;

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

    private String model = "lite-model_yamnet_classification_tflite_1.tflite";
    private final String TAG = "AudioIdentificationActivity";
    private AudioRecord audioRecord;
    private TimerTask timerTask;
    private AudioClassifier audioClassifier;
    private TensorAudio tensorAudio;
    //var per temporizzare l' invio delle email
    private long lastCallTime = 0;
    private static long MINUTES = 60 * 1000; // minuti in millisecondi

    private String objectOfAudio;
    private String emailTo, userName, emailBody, eventTime;
    private HashMap<String, Long> userClassification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        emailTo = sharedPreferences.getString("email", "");
        userName = sharedPreferences.getString("name", "utente");

        MINUTES *= Integer.parseInt(sharedPreferences.getString("timeout", "1"));
        userClassification = new HashMap<String, Long>();

        //prendiamo dalla sharedPrefence le categorie dell' utente
        String userCategoriesSharedPreference = sharedPreferences.getString("UserCategories", "");
        if (!TextUtils.isEmpty(userCategoriesSharedPreference)) {
            try {
                Log.i("StringaRitorno" , userCategoriesSharedPreference);
                JSONArray savedJsonArray = new JSONArray(userCategoriesSharedPreference);
                //per ogni elemento in savedJsonArrayd
                for (int i = 0; i < savedJsonArray.length(); i++) {
                    userClassification.put(savedJsonArray.getString(i), 0L);
                    Log.i("userClassification", savedJsonArray.getString(i));
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        //inizialize audioClassifier from TF model
        try {
            audioClassifier = AudioClassifier.createFromFile(this, model);
        }catch (IOException e){
            e.printStackTrace();
        }

        //inizialize audio recorder for classifying audio
        tensorAudio = audioClassifier.createInputTensorAudio();
    }

    @Override
    public void startRecording(View view) {
        super.startRecording(view);
        TensorAudio.TensorAudioFormat format = audioClassifier.getRequiredTensorAudioFormat();
        String specs = "Number of channels: " + format.getChannels() + "\n" +
                "Sample Rate: " + format.getSampleRate();
        tvSpecs.setText(specs);
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
                for (Classifications classifications : output){
                    for (Category category : classifications.getCategories()){
                        //if score is higher than 30% possibility...
                        categoryLabel = category.getLabel();
                        if(category.getScore() > 0.3f && userClassification.get(categoryLabel) != null){
                            finalOutput.add(category);
                            objectOfAudio = categoryLabel;
                            eventTime = getCurrentDateTime();
                            // TODO METTERE VIBRAZIONE
                            //sendEmail(categoryLabel);
                        }
                    }
                }

                //Creating a multiline string with the filtered results
                StringBuilder outputStr = new StringBuilder();
                for(Category category : finalOutput){
                    outputStr.append(category.getLabel())
                            .append(": ").append(eventTime).append("\n");
                    Log.i(TAG, outputStr.toString());
                }

                //updating the textView for output
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(finalOutput.isEmpty())
                            tvOutput.setText("Could not classify audio");
                        tvOutput.setText(outputStr.toString());
                    }
                });
            }
        };
        //after 1 second it start and every 0.5 second will classify audio
        new Timer().scheduleAtFixedRate(timerTask, 1, 500);
    }

    @Override
    public void stopRecording(View view) {
        super.stopRecording(view);
        timerTask.cancel();
        audioRecord.stop();
    }

    public String getCurrentDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}
