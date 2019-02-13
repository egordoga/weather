package ua.weather;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import ua.weather.fragment.ContentFragment;
import ua.weather.fragment.ErrorFragment;
import ua.weather.rest.ApiCall;

/**
 * Данное приложение мое первое приложение под Android и никаких сверхзадач не ставилось.
 * Простая программа для получения данных с веб-сервиса и отображения результатов.
 */

public class MainActivity extends AppCompatActivity {


    String[] cities = {"Vinnytsya", "Poltava", "Mykolayiv", "Chernihiv", "Cherkasy", "Sumy", "Lviv",
            "Kherson", "Rivne", "Ivano-Frankivsk"};

    private AutoCompleteTextView actvCityChoice;
    private ProgressBar loading;
    private OkHttpClient client;
    private String city;
    private Map<String, Long> mapCheckTime = new HashMap<>();
    private Map<String, String> data = new HashMap<>(); //можно было сделать и объектом
    private ErrorFragment errorFragment;
    private ContentFragment contentFragment;
    private FragmentTransaction transaction;
    private FragmentManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        client = new OkHttpClient();
        errorFragment = new ErrorFragment();
        contentFragment = new ContentFragment();
        manager = getSupportFragmentManager();
        loading = findViewById(R.id.pb_loading);
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
                boolean b = showErrorFrame(getString(R.string.minutes));
                if (b) {
                    errorFragment.updateMessage(getString(R.string.minutes));
                }
            }

            actvCityChoice.setText("");
        });
    }

    //Требование API сервера. Запрос на один город не чаще одного раза в 10 минут
    private boolean checkMinutes() {
        if (mapCheckTime.get(city) == null) {
            return true;
        }
        return (mapCheckTime.get(city)) + (1000 * 60 * 10) < System.currentTimeMillis();
    }

    private boolean showContentFrame() {
        if (errorFragment.isAdded()) {
            transaction = manager.beginTransaction();
            transaction.replace(R.id.fl_frame, contentFragment, ContentFragment.TAG);
            transaction.commit();
            return false;
        } else if (manager.findFragmentByTag(ContentFragment.TAG) == null) {
            transaction = manager.beginTransaction();
            transaction.add(R.id.fl_frame, contentFragment, ContentFragment.TAG);
            transaction.commit();
            return false;
        }
        return true;
    }

    private boolean showErrorFrame(String message) {
        if (contentFragment.isAdded()) {
            transaction = manager.beginTransaction();
            transaction.replace(R.id.fl_frame, errorFragment, ErrorFragment.TAG);
            transaction.commit();
            errorFragment.errStr = message;
            return false;
        } else if (manager.findFragmentByTag(ErrorFragment.TAG) == null) {
            transaction = manager.beginTransaction();
            transaction.add(R.id.fl_frame, errorFragment, ErrorFragment.TAG);
            transaction.commit();
            errorFragment.errStr = message;
            return false;
        }
        return true;
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
            boolean noChange;

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

                    data.put("main", main);
                    data.put("description", description);
                    data.put("temp", temp);
                    data.put("pressure", pressure);
                    data.put("strCity", strCity);
                    data.put("strWeather", strWeather);

                    contentFragment.frData = data;

                    noChange = showContentFrame();
                    if (noChange) {
                        contentFragment.updateData(data);
                    }
                } catch (JSONException e) {
                    noChange = showErrorFrame(getString(R.string.wrong));
                    if (noChange) {
                        errorFragment.updateMessage(getString(R.string.wrong));
                    }
                    e.printStackTrace();
                }
            } else {
                noChange = showErrorFrame(getString(R.string.wrong));
                if (noChange) {
                    errorFragment.updateMessage(getString(R.string.wrong));
                }
            }
            mapCheckTime.put(city, System.currentTimeMillis());
        }
    }

}
