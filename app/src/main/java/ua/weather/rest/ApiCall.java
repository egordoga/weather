package ua.weather.rest;

import android.net.Uri;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ApiCall {

    private static final String URL_BASE = "http://api.openweathermap.org/";
    private static final String URL_WEATHER_GET = "data/2.5/weather";
    private static final String PARAM_CITY = "q";
    private static final String PARAM_APPID = "6ea5b6c1859d65f2d2c899331d408b58"; // не очень правильно, но пока так

    // морочно, но best practice...
    public static URL getURL(String city) {
        Uri uri = Uri.parse(URL_BASE + URL_WEATHER_GET).buildUpon()
                .appendQueryParameter(PARAM_CITY, city + ",ua")
                .appendQueryParameter("appid", PARAM_APPID)
                .build();

        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }


    public static String getRestResult(OkHttpClient client, URL url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();

        return response.body() != null ? response.body().string() : null;
    }
}
