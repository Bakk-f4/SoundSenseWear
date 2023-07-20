package com.example.soundsensewear.helpers;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.soundsensewear.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SettingsListActivity extends AppCompatActivity {
    private final String TAG = "SettingsListActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_list_wear);
        LinearLayout checkboxContainer = findViewById(R.id.checkbox_container);

        try {
            //Se il json non esiste nella dir Files lo copio da assets (assets è una dir readonly)
            File file = new File(this.getFilesDir()+ "/short_list_category.json");

            if (!file.exists())
                copyFileFromAssetsToFiles(this, "short_list_category.json");
            //creo la check list
            createCheckList(checkboxContainer);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static void copyFileFromAssetsToFiles(Context context, String fileName) throws IOException {
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = assetManager.open(fileName);
        File outFile = new File(context.getFilesDir(), fileName);

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));

        char[] buffer = new char[1024];
        int bytesRead;
        while ((bytesRead = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, bytesRead);
        }
        writer.close();
        reader.close();
    }


    private void createCheckList(LinearLayout checkboxContainer) throws JSONException, IOException {
        JSONArray jsonArrayCategories;
        jsonArrayCategories = readJsonFromFile(this, "short_list_category.json");

        for (int i = 0; i < jsonArrayCategories.length(); i++) {
            JSONObject jsonObject = jsonArrayCategories.getJSONObject(i);

            CheckBox checkBox = new CheckBox(this);

            checkBox.setTag(jsonObject.get("index").toString());
            checkBox.setText(jsonObject.get("display_name").toString());
            checkBox.setChecked(stringToBoolean((jsonObject.get("checked").toString())));
            checkBox.setTextSize(25);
            checkBox.setPadding(0, 0, 0, 20);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // Ottieni lo stato della checkbox
                    Boolean statoCheckbox = isChecked;
                    CheckBox checkBox = (CheckBox) buttonView;

                    // Ottieni il tag associato al CheckBox (id)
                    String id = checkBox.getTag().toString();

                    Context context = SettingsListActivity.this;
                    Log.i("cartellaOnchecked", context.getFilesDir().toString());
                    updateJSONObjectInFile(context, "short_list_category.json", id, "checked", statoCheckbox.toString());
                }
            });
            checkboxContainer.addView(checkBox);
        }
    }


    private void updateJSONObjectInFile(Context context, String fileName, String id, String keyToUpdate, String newValue) {
        try {
            Log.i("cartellaUPDATE", context.getFilesDir().toString());
            // Legge il contenuto del file JSON esistente in una stringa
            JSONArray jsonArray = readJsonFromFile(context, fileName);

            //Converte la stringa JSON in un oggetto JSONObject
            JSONObject jsonObjectUpdated = jsonArray.getJSONObject(Integer.parseInt(id));

            //Aggiorna le informazioni nel JSONObject
            jsonObjectUpdated.put(keyToUpdate, newValue);

            //Aggiorna l'elemento nel JSONArray
            jsonArray.put(Integer.parseInt(id), jsonObjectUpdated);

            //Sovrascrive il file JSON con il nuovo JSONArray aggiornato
            writeJSONToFile(context, fileName, jsonArray);
            Log.i("fileJsonArray", jsonArray.toString());
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }


    public static JSONArray readJsonFromFile(Context context, String fileName) throws IOException, JSONException {
        InputStream inputStream = context.openFileInput(fileName);

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder jsonString = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonString.append(line);
        }
        reader.close();

        return new JSONArray(jsonString.toString());
    }



    private static void writeJSONToFile(Context context, String fileName, JSONArray jsonArray) throws IOException {
        File file = new File(context.getFilesDir(), fileName);
        FileWriter fileWriter = new FileWriter(file);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(jsonArray.toString());
        bufferedWriter.close();
    }


    private boolean stringToBoolean (String input){
        if (input.equals("false")) {
            return false;
        }
        return true;
    }
}
