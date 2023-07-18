package com.example.soundsensewear.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.soundsensewear.R;
import com.example.soundsensewear.audio.AudioClassificationAcitvity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsActivity extends AppCompatActivity {

    private EditText /* editTextEmail, editTextName, editTextSurname, */ editTextTimeout;
    private Button buttonSubmit, buttonCategories;
    private Button buttonReset;

    /*
    private String email;
    private String name;
    private String surname;
    */

    private String timeout;
    private SharedPreferences sharedPreferences;
    private  ArrayAdapter<String> adapter;
    private static ArrayList<String> finalListCategory = new ArrayList<>();
    private ArrayList<String> yamnetClassList = new ArrayList<>();


    //TODO CAMBIARE L USO DELLE LISTE, USARE SOLO JSONARRAY AL POSTO DI finalListCategory

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //load data from sharedPreference
        sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE);

        //initialize UI objects

        /*
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextName = findViewById(R.id.editTextName);
        editTextSurname = findViewById(R.id.editTextSurname);
        */

        editTextTimeout = findViewById(R.id.editTextTimeOutEmail);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        buttonReset = findViewById(R.id.bttReset);
        buttonCategories = findViewById(R.id.bttCategories);

        String userCategories = sharedPreferences.getString("UserCategories", "");
        if(userCategories.equals(""))
            buttonReset.setEnabled(false);


        //check if we didnt loaded already yamnet categories
        String loadedYamnetFromSharedPref = sharedPreferences.getString("allYamnetCategories", "");
        if(loadedYamnetFromSharedPref.equals("")){

            //get all classification from yamnet.json file
            String[] yamnetClassArray = fromJSONToArray("short_yamnet_class_map.json");
            yamnetClassList.addAll(Arrays.asList(yamnetClassArray));
            Collections.sort(yamnetClassList);

            //preparing data for sharedPreference
            JSONArray jsonArray = new JSONArray(yamnetClassList);
            String arrayString = jsonArray.toString();

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("allYamnetCategories", arrayString);
            editor.apply();

        } else {
            try {

                // Converti la stringa dell'array in un JSONArray
                JSONArray savedJsonArray = new JSONArray(loadedYamnetFromSharedPref);
                Log.i("savedJsonArray", savedJsonArray.toString());

                //per ogni elemento in savedJsonArray
                for (int i = 0; i < savedJsonArray.length(); i++) {
                    yamnetClassList.add(savedJsonArray.getString(i));
                    Log.i("YAMNETELSE", savedJsonArray.getString(i));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //load the data from yamnetClassList into the Spinner
        Spinner spinner = findViewById(R.id.spinnerCategory);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, yamnetClassList);
        spinner.setAdapter(adapter);

        //on user selection item from spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (id != 0) {
                    // Rimuovi l'elemento dalla lista temporanea
                    String selectedItem = (String) parent.getItemAtPosition(position);
                    yamnetClassList.remove(selectedItem);

                    //preparing data for sharedPreference
                    JSONArray jsonArray = new JSONArray(yamnetClassList);
                    String arrayString = jsonArray.toString();

                    //removing the element from the sharedPreference allYamnetCategories
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("allYamnetCategories", arrayString);
                    editor.apply();

                    // Imposta il nuovo adapter
                    ArrayAdapter<String> newAdapter = new ArrayAdapter<>(SettingsActivity.this, android.R.layout.simple_spinner_dropdown_item, yamnetClassList);
                    spinner.setAdapter(newAdapter);

                    // Aggiungi l'elemento alla lista "finalListCategory"
                    finalListCategory.add(selectedItem);

                    //inseriamo finalListCategory alle sharedPreference
                    jsonArray = new JSONArray(finalListCategory);
                    arrayString = jsonArray.toString();
                    editor = sharedPreferences.edit();
                    editor.putString("UserCategories", arrayString);
                    editor.apply();

                    //enabling reset button for categories
                    buttonReset.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nessuna selezione effettuata
            }
        });

        //update the UI objects
        timeout = sharedPreferences.getString("timeout", "");

        editTextTimeout.setText(timeout);


        //when user send the input data with buttonSubmit
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeout = editTextTimeout.getText().toString().trim();
                if(!isNumber(timeout)){
                    Toast.makeText(SettingsActivity.this, "Insert a valid number for Timeout", Toast.LENGTH_LONG).show();
                    timeout = "";
                }
                if (timeout.isEmpty()) {
                    Toast.makeText(SettingsActivity.this, "Insert a timeout", Toast.LENGTH_SHORT).show();
                } else {
                    //memorizzo le preferenze del client in modo permanente
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("timeout", timeout);
                    editor.apply();

                    Toast.makeText(SettingsActivity.this, "Ready to start", Toast.LENGTH_LONG).show();
                    onGoToAudioClassificationActivity(v);
                }
            }
        });

        //when user use reset button
        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //grab UserCategories from sharedPref
                String userCategories = sharedPreferences.getString("UserCategories", "");
                JSONArray savedJsonArray = null;
                try {
                    savedJsonArray = new JSONArray(userCategories);
                    Log.i("userCategories", savedJsonArray.toString());
                    //for each userCategory, insert it into the yamnetClassList
                    for (int i = 0; i < savedJsonArray.length(); i++) {
                        yamnetClassList.add(savedJsonArray.getString(i));
                        Log.i("YAMNETELSE", savedJsonArray.getString(i));
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                //sorting output
                Collections.sort(yamnetClassList);

                //cleaning both arrays
                finalListCategory.clear();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("UserCategories", "");
                editor.apply();

                //disable the button reset
                buttonReset.setEnabled(false);
            }
        });
        /*
        //when use press categories button
        buttonCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    onGoToAudioClassificationActivity2(v);
                }
        });*/
    }
    
    public void onGoToAudioClassificationActivity(View view){
        // start the audio helper activity
        Intent intent = new Intent(this, AudioClassificationAcitvity.class);
        startActivity(intent);
    }

    /*
    public void onGoToSettingsListActivity(View view){
        // start the audio helper activity
        Intent intent = new Intent(this, SettingsListActivity.class);
        startActivity(intent);
    } */


    public static boolean validateEmail(String email) {
        String EMAIL_PATTERN =
                "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean isNumber(String input) {
        String pattern = "-?\\d+(\\.\\d+)?";
        return Pattern.matches(pattern, input);
    }

    public String[] fromJSONToArray(String fileName) {
        String[] ret = null;

        try {
            InputStream inputStream = getAssets().open(fileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            String jsonContent = stringBuilder.toString();

            JSONArray jsonArray = new JSONArray(jsonContent);

            ret = new String[jsonArray.length()];

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String displayName = jsonObject.getString("display_name");
                ret[i] = displayName;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ret;
    }

    //get the finalListCategory
    public ArrayList<String> getFinalListCategory() {
        return finalListCategory;
    }
}
