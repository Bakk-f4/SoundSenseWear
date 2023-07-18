package com.example.soundsensewear.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

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

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_list_wear);


        LinearLayout checkboxContainer = findViewById(R.id.checkbox_container);

        //load data from sharedPreference
        sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE);

        //check if userCategories are setted
        String userCategories = sharedPreferences.getString("UserCategories", "");

        //check if we didnt loaded already yamnet categories
        String loadedYamnetFromSharedPref = sharedPreferences.getString("allYamnetCategories", "");
        if (loadedYamnetFromSharedPref.equals("")) {

            try {
                InputStream inputStream = getAssets().open("yamnet_class_map.json");
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder jsonString = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonString.append(line);
                }
                reader.close();

                StringBuilder allCategories = new StringBuilder();

                JSONArray jsonArray = new JSONArray(jsonString.toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    String id = jsonObject.getString("index");
                    String name = jsonObject.getString("display_name");
                    // TODO SALVARE DATI NEI VARI JSON (SELEZIONATI E NONSELEZIONATI)
                    //String bool = valore booleano salvato nel json

                    String final_single_category = id+";"+name+";0;";
                    allCategories.append(final_single_category);


                    //saving all the categories from the file to the sharedPreference
                    // for the first time when we open the file
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("allYamnetCategories", allCategories.toString());
                    editor.apply();


                    CheckBox checkBox = new CheckBox(this);
                    checkBox.setText(name);
                    checkBox.setTag(id);
                    checkBox.setTextSize(25);
                    checkBox.setPadding(0, 0, 0, 20);
                    checkboxContainer.addView(checkBox);


                }
                //il file e' gia stato caricato nelle shared preference
                // aggiorniamo la lista giga
                loadedYamnetFromSharedPref = sharedPreferences.getString("allYamnetCategories", "");
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

        }
        //ora questo e' pieno loadedYamnetFromSharedPref
        String[] categoriesTuple = loadedYamnetFromSharedPref.split(";");

        for (int i = 0; i < categoriesTuple.length; i+=3 ){
            CheckBox checkBox = new CheckBox(this);
            checkBox.setTag(categoriesTuple[i]);
            checkBox.setText(categoriesTuple[i+1]);
            checkBox.setChecked(stringToBoolean(categoriesTuple[i+2]));

            checkBox.setTextSize(25);
            checkBox.setPadding(0, 0, 0, 20);

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // Ottieni lo stato della checkbox
                    boolean statoCheckbox = isChecked;
                    // Puoi fare qualcosa con lo stato della checkbox,
                    // ad esempio, memorizzarlo o eseguire un'azione
                }
            });


            checkboxContainer.addView(checkBox);
        }
        //https://developer.android.com/develop/ui/views/components/checkbox


    }


    private boolean stringToBoolean(String input) {
        if (input.equals("0")) {
            return false;
        }
        return true;
    }







    private String loadJSONFromAsset () {
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
