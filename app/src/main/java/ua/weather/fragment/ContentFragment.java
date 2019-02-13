package ua.weather.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import ua.weather.R;

public class ContentFragment extends Fragment {

    public static final String TAG = "ContentFragment";
    public Map<String, String> frData = new HashMap<>();
    private TextView answer;
    private TextView weather;
    private TextView temp;
    private TextView press;
    private ImageView picture;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_content, container, false);
        answer = view.findViewById(R.id.tv_answer);
        weather = view.findViewById(R.id.tv_weather);
        temp = view.findViewById(R.id.tv_temp);
        press = view.findViewById(R.id.tv_pres);
        picture = view.findViewById(R.id.iv_pict);

        updateData(frData);
        return view;
    }

    public void updateData(Map<String, String> data) {
        answer.setText(data.get("strCity"));
        weather.setText(data.get("strWeather"));
        temp.setText(data.get("temp"));
        press.setText(data.get("pressure"));

        switch (data.get("main")) {
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
    }
}
