package com.huntmix.pw0boom;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class About extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.about, container, false);
        TextView creds = view.findViewById(R.id.creds);
        TextView card = view.findViewById(R.id.visacard);
        if (!creds.getText().toString().contains("@Huntmix")){
            System.exit(1);
        }
        return view;
    }

}