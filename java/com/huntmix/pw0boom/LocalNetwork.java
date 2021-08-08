package com.huntmix.pw0boom;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.topjohnwu.superuser.CallbackList;
import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ir.alirezabdn.wp7progress.WP7ProgressBar;
import pl.droidsonroids.gif.GifImageView;

public class LocalNetwork extends Fragment {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    public ArrayList<ArrayList<String>> local = new ArrayList<>();
    public String chroot;
    public WP7ProgressBar scan;
    public TinyDB tinydb;
    public String gate;
    public String localip;
    public String mymac;
    public  ArrayList<String> device = new ArrayList<>();
    public TextView count;
    public ArrayList<ArrayList<String>> list = new ArrayList<>();
    public int i = 0;
    public String busybox = "su -c /data/data/com.huntmix.pw0boom/cache/busybox chroot ";
    public Context context;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View viewroot = inflater.inflate(R.layout.local_network, container, false);
        context= getContext();
        tinydb = new TinyDB(context);
        chroot = tinydb.getString("chroot_path");
        mRecyclerView = viewroot.findViewById(R.id.local);
        scan = viewroot.findViewById(R.id.scanlocal);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        FloatingActionButton fab = viewroot.findViewById(R.id.fablocal);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gate = "";
                mymac = "";
                scan.showProgressBar();
                List<String> mac = new CallbackList<String>() {
                    @Override
                    public void onAddElement(String s) {
                        mymac = s.toUpperCase();
                    }
                };
                Shell.su("su -c /data/data/com.huntmix.pw0boom/cache/busybox cat /sys/class/net/wlan0/address").to(mac).exec();


                List<String> gw = new CallbackList<String>() {
                    @Override
                    public void onAddElement(String s) {

                        char[] ch = s.toCharArray();
                        for (char c : ch) {
                            String b = String.valueOf(c);
                            if (!b.equals("d")){
                                gate = gate + b;
                            }else{
                                String test = s.replace(gate,"");
                                Matcher matcher = Pattern.compile("\\d+\\.\\d+\\.\\d+\\.\\d+").matcher(test);
                                if (matcher.find()){
                                    localip = matcher.group(0);
                                }
                                if (gate.length()<2){
                                    toaster("Please connect to wifi first");
                                }else{
                                getmacs(gate);}
                                break;
                            }
                        }
                    }
                };
                Shell.su("ip route show").to(gw).submit();
            }
        });
        return viewroot;
    }

    public String getSystemProperty(String key) {
        String value = null;

        try {
            value = (String) Class.forName("android.os.SystemProperties")
                    .getMethod("get", String.class).invoke(null, key);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return value;
    }
    public void getmacs(String netmask){


        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                list = new ArrayList<>();
                device = new ArrayList<>();
                i = 0;
                List<String> nmap = new CallbackList<String>() {
                    @Override
                    public void onAddElement(String s) {
                        String temp = s.replaceAll("\\s+","").replace("*","");
                        if (temp.contains("Nmapscanreportfor")){
                            String ip = temp.replace("Nmapscanreportfor","");
                            Matcher matcher = Pattern.compile("\\(\\d+\\.\\d+\\.\\d+\\.\\d+\\)").matcher(ip);
                            if (matcher.find()) {
                                String local = matcher.group(0);
                                String local2 = local.replace("(","").replace(")","");
                                device.add(local2);
                            }else{
                                device.add(ip);}

                        }
                        if (temp.contains("MACAddress:")){
                            String mac = "";
                            String comp = "";
                             mac = temp.replace("MACAddress:","").substring(0,17);
                             comp = s.replace("MAC Address:","").replace(mac,"").replace("  ","").replace("(","").replace(")","");
                            device.add(mac);
                            device.add(comp);
                            if (device.size() == 3){
                            list.add(device);}
                            device = new ArrayList<>();
                        }
                        if (temp.contains("Nmapdone")){
                            device = new ArrayList<>();
                            device.add(localip);
                            device.add(mymac);
                            device.add(getSystemProperty("ro.product.model"));
                            list.add(device);
                            mAdapter = new LocalAdapter(getContext(),getActivity(),list);
                            mRecyclerView.setAdapter(mAdapter);
                            scan.hideProgressBar();


                        }
                    }
                };
                Shell.su(busybox+chroot+" /usr/bin/sudo pkill -e nmap").exec();
                Shell.su(busybox+chroot+" /usr/bin/sudo nmap -sP "+netmask).to(nmap).submit();
            }
        });

    }
    public void toaster(String msg){
        Toast toast = Toast.makeText(context,
                msg, Toast.LENGTH_SHORT);
        toast.show();
    }
}