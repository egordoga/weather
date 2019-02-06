package ua.weather;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import ua.weather.rest.ApiCall;

/**
 * Данное приложение мое первое приложение под Android и никаких сверхзадач не ставилось.
 * Простая программа для получения данных с веб-сервиса и отображения результатов.
 */

public class MainActivity extends AppCompatActivity {

    private TextView txtWeather;
    private TextView txtAnsw;
    private TextView txtTemp;
    private TextView txtPressure;
    private TextView txtMm;
    private TextView txtCelsius;
    private TextView txtError;
    private ImageView picture;
    private AutoCompleteTextView actvCityChoice;
    private ProgressBar loading;
    private FrameLayout content;
    private FrameLayout error;
    String[] cities = {"Vinnytsya", "Poltava", "Mykolayiv", "Chernihiv", "Cherkasy", "Sumy", "Lviv",
            "Kherson", "Rivne", "Ivano-Frankivsk"};

    private OkHttpClient client;
    private String city;
    private Map<String, Long> mapCheckTime = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        client = new OkHttpClient();

        txtWeather = findViewById(R.id.tv_weather);
        txtAnsw = findViewById(R.id.tv_answer);
        txtPressure = findViewById(R.id.tv_pres);
        txtTemp = findViewById(R.id.tv_temp);
        txtCelsius = findViewById(R.id.tv_cels);
        txtMm = findViewById(R.id.tv_mm);
        txtError = findViewById(R.id.tv_error);
        picture = findViewById(R.id.iv_pict);
        loading = findViewById(R.id.pb_loading);
        content = findViewById(R.id.fl_content);
        error = findViewById(R.id.fl_error);
        actvCityChoice = findViewById(R.id.actv_city);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, cities);
        actvCityChoice.setThreshold(1);
        actvCityChoice.setAdapter(adapter);
        listenChange();
    }

    private void listenChange() {
        actvCityChoice.setOnItemClickListener((parent, view, position, id) -> {
            TextView txtCity = (TextView) view;
            city = txtCity.getText().toString();

            if (checkMinutes()) {
                URL url = ApiCall.getURL(city);
                new QueryTask().execute(url);
            } else {
                txtError.setText(getString(R.string.minutes));
                showErrorFrame();
            }

            actvCityChoice.setText("");
        });
    }

    //Требование API сервера. Запрос на один город не чаще раз в 10 минут
    private boolean checkMinutes() {
        if (mapCheckTime.get(city) == null) {
            return true;
        }
        return (mapCheckTime.get(city)) + (1000 * 60 * 10) < System.currentTimeMillis();
    }

    private void showContentFrame() {
        content.setVisibility(View.VISIBLE);
        error.setVisibility(View.INVISIBLE);
    }

    private void showErrorFrame() {
        content.setVisibility(View.INVISIBLE);
        error.setVisibility(View.VISIBLE);
    }

    class QueryTask extends AsyncTask<URL, Void, String> {
        @Override
        protected String doInBackground(URL... urls) {
            String response = null;
            try {
                response = ApiCall.getRestResult(client, urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPreExecute() {
            loading.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String response) {
            // странные названия переменных таковы в приходящем объекте
            loading.setVisibility(View.INVISIBLE);

            if (response != null && !"".equals(response)) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONArray weather = jsonResponse.getJSONArray("weather");
                    String main = weather.getJSONObject(0).getString("main");
                    String description = weather.getJSONObject(0).getString("description");
                    JSONObject mainObj = jsonResponse.getJSONObject("main");
                    String temp = String.valueOf((int) Math.round(mainObj.getDouble("temp") - 273.15));
                    String pressure = String.valueOf((int) Math.round(mainObj.getDouble("pressure") * .75));
                    String strCity = getString(R.string.weather_in) + " " + city;
                    String strWeather = main + ", " + description;

                    txtAnsw.setText(strCity);
                    txtWeather.setText(strWeather);
                    txtTemp.setText(temp);
                    txtPressure.setText(pressure);
                    txtCelsius.setVisibility(View.VISIBLE);
                    txtMm.setVisibility(View.VISIBLE);

                    switch (main) {
                        case "Clear":
                            picture.setImageResource(R.drawable.sunny);
                            break;
                        case "Snow":
                            picture.setImageResource(R.drawable.snow);
                            break;
                        case "Rain":
                            picture.setImageResource(R.drawable.rain);
                            break;
                        case "Clouds":
                            picture.setImageResource(R.drawable.overcast);
                    }
                    showContentFrame();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                txtError.setText(getString(R.string.wrong));
                showErrorFrame();
            }
            mapCheckTime.put(city, System.currentTimeMillis());
        }
    }

}
