package com.example.soundsensewear.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.wear.widget.DismissibleFrameLayout;

import com.example.soundsensewear.R;
import com.google.android.gms.wearable.Asset;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class SettingsListActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_list_wear);


        LinearLayout checkboxContainer = findViewById(R.id.checkbox_container);

        JSONArray jsonArrayCategories;
        try {
            jsonArrayCategories = readJsonArrayFromFile("short_yamnet_class_map.json");

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


                        updateJSONObjectInFile(buttonView.getContext(),"short_yamnet_class_map.json", id, "checked", statoCheckbox.toString());


                    }
                });

                checkboxContainer.addView(checkBox);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


        //https://developer.android.com/develop/ui/views/components/checkbox


    }


    private boolean stringToBoolean(String input) {
        if (input.equals("false")) {
            return false;
        }
        return true;
    }




    public String readJSONFromFile(String filePath) throws IOException, JSONException {
        InputStream inputStream = getAssets().open(filePath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder jsonString = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonString.append(line);
        }
        reader.close();

        return jsonString.toString();
    }




    public void updateeJSONToFile(String filePath, String jsonString) throws IOException {
        BufferedWriter writer = null;
        try {
            File file = new File(filePath);
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            writer = new BufferedWriter(osw);
            writer.write(jsonString);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public static void updateJsonFile(String path, String jsonString) throws IOException {


        if (!new File("").exists()) {
            // File does not exist, create it.
            FileWriter writer = new FileWriter(path);
            writer.write("{}");
            writer.close();
        }

        try {
            updateJsonFile("path", jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JSONArray readJsonArrayFromFile(String filePath) throws IOException, JSONException {
        InputStream inputStream = getAssets().open(filePath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder jsonString = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonString.append(line);
        }
        reader.close();

        return new JSONArray(jsonString.toString());
    }


    private static void writeJSONToFile1(Context context, String fileName, String jsonString) {
        try {
            File file = new File(context.getFilesDir(), fileName);
            FileOutputStream fos = new FileOutputStream(file);
            Log.i("fos", fos.toString());
            fos.write(jsonString.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }








    public void updateJSONObjectInFile(Context context, String fileName, String id, String keyToUpdate, String newValue) {
        try {
            // Step 1: Leggi il contenuto del file JSON esistente in una stringa
            JSONArray jsonArray = readJsonArrayFromFile(fileName);

            // Step 2: Converti la stringa JSON in un oggetto JSONObject

            JSONObject jsonObjectUpdated = jsonArray.getJSONObject(Integer.parseInt(id));


            // Step 3: Aggiorna le informazioni nel JSONObject
            jsonObjectUpdated.put(keyToUpdate, newValue);

            // Step 4: Converti il JSONObject aggiornato in una stringa JSON
            String updatedJsonString = jsonObjectUpdated.toString();
            Log.i("json dopo della modifica", updatedJsonString);

            // Step 5: Sovrascrivi il file JSON con la nuova stringa JSON aggiornata
            updateJsonFile( fileName , updatedJsonString);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }






    /*private String loadJSONFromAsset () {
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
     */



}
