package com.example.osavci.memurharcirahyollukhesaplama;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    RadioGroup kadroRadioGroup;
    RadioButton kadroRadioButton;
    RadioGroup aileRadioGroup;
    RadioButton aileRadioButton;
    AutoCompleteTextView fromTextView;
    AutoCompleteTextView toTextView;
    EditText priceText;
    String[] cityArray;
    Button calculateButton;
    double kadroYevmiyeKatsayi;
    double yolUcretiKatsayi;
    double aileYevmiye;
    double mesafe;
    double tasitUcreti;
    double totalFee;
    int fertSayısı;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityArray = getResources().getStringArray(R.array.city_array);

        kadroRadioGroup = (RadioGroup) findViewById(R.id.radioGroup1);
        aileRadioGroup = (RadioGroup) findViewById(R.id.radioGroup2);
        fromTextView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        toTextView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView2);
        priceText = (EditText) findViewById(R.id.priceText);

        ArrayAdapter autocompletetextAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_dropdown_item_1line, cityArray);

        fromTextView.setAdapter(autocompletetextAdapter);
        toTextView.setAdapter(autocompletetextAdapter);

        calculateButton = (Button) findViewById(R.id.button);

        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedId = kadroRadioGroup.getCheckedRadioButtonId();

                kadroRadioButton = (RadioButton) findViewById(selectedId);

                if (kadroRadioButton.getText().equals("1-4")) {
                    kadroYevmiyeKatsayi = 33.00;
                    yolUcretiKatsayi = 1.65;
                } else {
                    kadroYevmiyeKatsayi = 32.00;
                    yolUcretiKatsayi = 1.6;
                }

                selectedId = aileRadioGroup.getCheckedRadioButtonId();

                aileRadioButton = (RadioButton) findViewById(selectedId);

                String aileSelection = aileRadioButton.getText().toString();

                if (aileSelection.equals("0")) {
                    aileYevmiye = 0;
                } else if (aileSelection.equals("1")) {
                    aileYevmiye = 20;
                } else if (aileSelection.equals("2")) {
                    aileYevmiye = 20;
                } else if (aileSelection.equals("3")) {
                    aileYevmiye = 30;
                } else {
                    aileYevmiye = 40;
                }

                String cities[] = {fromTextView.getText().toString(), toTextView.getText().toString()};
                new RetrieveFeedTask().execute(cities);
                tasitUcreti = Double.parseDouble(priceText.getText().toString());
                tasitUcreti = tasitUcreti + ((aileYevmiye / 10) * tasitUcreti);

                totalFee = (20 * kadroYevmiyeKatsayi) + (aileYevmiye * kadroYevmiyeKatsayi) + ((mesafe / 1000) * yolUcretiKatsayi) + (tasitUcreti);

                Toast.makeText(getApplication(), String.valueOf(totalFee), Toast.LENGTH_LONG).show();
            }
        });


    }

    private double getDistanceInfo(String city1, String city2) {
        StringBuilder stringBuilder = new StringBuilder();
        Double dist = 0.0;
        try {

            String url = "http://maps.googleapis.com/maps/api/directions/json?origin=" + city1 + "&destination=" + city2 + "&sensor=false";

            HttpPost httppost = new HttpPost(url);

            HttpClient client = new DefaultHttpClient();
            HttpResponse response;
            stringBuilder = new StringBuilder();


            response = client.execute(httppost);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }

        JSONObject jsonObject = new JSONObject();
        try {

            jsonObject = new JSONObject(stringBuilder.toString());

            JSONArray array = jsonObject.getJSONArray("routes");

            JSONObject routes = array.getJSONObject(0);

            JSONArray legs = routes.getJSONArray("legs");

            JSONObject steps = legs.getJSONObject(0);

            JSONObject distance = steps.getJSONObject("distance");

            Log.i("Distance", distance.toString());
            dist = Double.parseDouble(distance.getString("value"));

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return dist;
    }

    class RetrieveFeedTask extends AsyncTask<String, Void, Double> {

        protected void onPostExecute(Double feed) {
            mesafe = feed;
        }

        @Override
        protected Double doInBackground(String... params) {
            return getDistanceInfo(params[0], params[1]);
        }
    }

}
