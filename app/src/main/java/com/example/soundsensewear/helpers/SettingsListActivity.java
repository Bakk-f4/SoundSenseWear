package com.example.soundsensewear.helpers;

import android.os.Bundle;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;
import androidx.wear.widget.DismissibleFrameLayout;

import com.example.soundsensewear.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SettingsListActivity extends AppCompatActivity {

    private DismissibleFrameLayout checkboxContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_list_wear);

        checkboxContainer = findViewById(R.id.checkboxContainer);

        // Leggi il file JSON
        String json = loadJSONFromAsset();
        if (json != null) {
            try {
                // Parsa il file JSON
                JSONArray jsonArray = new JSONArray(json);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    // Leggi i dati dall'oggetto JSON
                    int id = jsonObject.getInt("id");
                    String label = jsonObject.getString("label");

                    // Crea il checkbox dinamicamente
                    createCheckBox(id, label);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private void createCheckBox(int id, String label) {
        CheckBox checkBox = new CheckBox(this);
        checkBox.setId(id);
        checkBox.setText(label);
        checkboxContainer.addView(checkBox);
    }

    private String loadJSONFromAsset() {
        String line = "";
        try {
            InputStream inputStream = getAssets().open("short_yamnet_class_map.json");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return line;
    }
}
