package ua.weather.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ua.weather.R;

public class ErrorFragment extends Fragment {

    public static final String TAG = "ErrorFragment";
    public String errStr;
    private TextView errTW;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_error, container, false);
        errTW = view.findViewById(R.id.tv_error);
        errTW.setText(errStr);
        return view;
    }

    public void updateMessage(String message) {
        errTW.setText(message);
    }
}
