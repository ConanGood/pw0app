package com.huntmix.pw0boom;


import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.topjohnwu.superuser.CallbackList;
import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import pl.droidsonroids.gif.GifImageView;

public class Handshakes extends Fragment {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    public ArrayList<String> hs = new ArrayList<>();
    public GifImageView cat;
    public TextView error;

    public Context context;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View viewroot = inflater.inflate(R.layout.hs_boom, container, false);
        context= getContext();
        cat = viewroot.findViewById(R.id.hscat);
        error = viewroot.findViewById(R.id.hserror);
        mRecyclerView = viewroot.findViewById(R.id.hslist);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (!getListFiles(new File("/storage/emulated/0/Pw0/hs/")).isEmpty()){
        mAdapter = new HS_Adapter(getContext(),getActivity(),getListFiles(new File("/storage/emulated/0/Pw0/hs/")));
           }
        else{
            cat.setVisibility(View.VISIBLE);
            error.setVisibility(View.VISIBLE);
        }
        mRecyclerView.setAdapter(mAdapter);
        return viewroot;
    }
    private ArrayList<String> getListFiles(File parentDir) {
        ArrayList<String> inFiles = new ArrayList<String>();
        File[] files = parentDir.listFiles();
        if (files != null){
        for (File file : files) {
            if (file.isDirectory()) {
                inFiles.addAll(getListFiles(file));
            } else {
                if(file.getName().endsWith(".cap")||file.getName().endsWith(".hccapx")){
                    inFiles.add(file.getAbsolutePath());
                }
            }
        }}
        return inFiles;
    }
}