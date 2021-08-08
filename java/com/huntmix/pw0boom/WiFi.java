package com.huntmix.pw0boom;


import android.Manifest;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;

import com.topjohnwu.superuser.CallbackList;
import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import ir.alirezabdn.wp7progress.WP7ProgressBar;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

public class WiFi extends Fragment {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    public Context context;
    public Shell.Result check;
    public TextView text;
    public String chroot;
    public int count = 0;
    public int count2 =0 ;
    public String ch = "";
    public TinyDB tinydb;
    public String busybox = "su -c /data/data/com.huntmix.pw0boom/cache/busybox";
    public ArrayList<String> wifi = new ArrayList<>();
    public WP7ProgressBar scan;
    public MaterialTextView log;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.wifi_boom, container, false);
        context= getContext();
        mRecyclerView = rootView.findViewById(R.id.wifilist);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        scan = rootView.findViewById(R.id.scanwifi);
        checkroot();
        tinydb = new TinyDB(context);
        chroot = tinydb.getString("chroot_path");
        mAdapter = new Adapter(getContext(),getActivity(),new ArrayList<>());
        mRecyclerView.setAdapter(mAdapter);



        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setwlan();
                return false;
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tinydb.getString("wlan").equals("") || tinydb.getString("wlan") == null) {
                setwlan();
                }else {

                scan.showProgressBar();
                count = 0;
                count2 = 0;

                ArrayList<ArrayList<String>> networks = new ArrayList<>();
                List<String> out = new ArrayList<>();
                List<String> callbackList = new CallbackList<String>() {
                    @Override
                    public void onAddElement(String s) {
                        String temp = s.replaceAll("\\s+","").replace("*","");
                        out.add(temp);
                        if (temp.contains("BSS") && temp.contains("wlan") && !temp.contains("Load")&& !temp.contains("width")&& !temp.contains("scan")){
                            String bssid = temp.replace("BSS","");
                            count = count +1;
                            wifi.add(bssid.substring(0,17));
                        }
                        if (temp.contains("signal:")){
                            String power = temp.replace("signal:","").replace("dBm","");
                            wifi.add(power.substring(0, power.length() - 3));
                            count = count +1;
                        }
                        if (temp.contains("SSID:")){
                            String name = temp.replace("SSID:","");
                            if (name.length() >1 || !name.isEmpty()){
                                wifi.add(name);
                            }else{
                                wifi.add("Hidden network");
                            }
                            count = count +1;

                        }
                        if (temp.contains("DSParameterset:channel") && count == 3){
                            String ch = temp.replace("DSParameterset:channel","");
                            wifi.add(ch);
                            count = count + 1;
                        }
                        if (temp.contains("primarychannel:") && count == 3){
                            String ch = temp.replace("primarychannel:","");
                            wifi.add(ch);
                            count = count+1;
                        }
                        if (count == 4){
                            Log.e("WTF ",count+"/"+count2+"/+"+wifi);
                            networks.add(wifi);
                            count =0;
                            wifi = new ArrayList<>();
                        }
                        if (temp.contains("WPS:Version")){
                            networks.get(networks.size()-1).add("wps");
                        }
                        if (temp.contains("Model:")){
                            String model = temp.replace("Model:","");
                            networks.get(networks.size()-1).add(model);
                        }


                         }
                };
                String wlan = tinydb.getString("wlan");
                if (!wlan.equals("wlan0")){
                    Shell.su("ip link set "+wlan+" up").exec();
                    Shell.su(busybox+" chroot "+chroot+" /usr/bin/sudo iw "+wlan+" scan")
                            .to(callbackList)
                            .submit();
                }
                if (wlan.equals("wlan0")){
                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    if (!wifiManager.isWifiEnabled()){
                        toaster("Enable WIFI!");
                    }else {
                        Shell.su("/data/data/com.huntmix.pw0boom/cache/busybox chroot "+chroot+" /usr/bin/sudo iw wlan0 scan")
                                .to(callbackList)
                                .submit();
                    }

                }

                Handler handler=new Handler();
                    Runnable r= new Runnable() {
                        public void run() {
                            Snackbar.make(view, "Updated", Snackbar.LENGTH_LONG).show();
                            mAdapter = new Adapter(getContext(),getActivity(),networks);
                            mRecyclerView.setAdapter(mAdapter);
                            scan.hideProgressBar();
                        }
                    };
                    handler.postDelayed(r, 8000);}

            }
        });

        return rootView;
}
public void toaster(String msg){
    Toast toast = Toast.makeText(context,
           msg, Toast.LENGTH_SHORT);
    toast.show();
}
    public void setwlan(){
        Dialog dialog = new Dialog(context,R.style.AppBottomSheetDialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.setwlan, null, false);
        RadioButton w0 = view.findViewById(R.id.wl0);
        RadioButton w1 = view.findViewById(R.id.wl1);
        RadioButton w2 = view.findViewById(R.id.wl2);
        RadioButton w3 = view.findViewById(R.id.wl3);
        w0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tinydb.putString("wlan","wlan0");
                toaster("Setted: wlan0");
                dialog.dismiss();
            }
        });
        w1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tinydb.putString("wlan","wlan1");
                toaster("Setted: wlan1");
                dialog.dismiss();
            }
        });
        w2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tinydb.putString("wlan","wlan2");
                toaster("Setted: wlan2");
                dialog.dismiss();
            }
        });
        w3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tinydb.putString("wlan","wlan3");
                toaster("Setted: wlan3");
                dialog.dismiss();
            }
        });
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.setContentView(view);
        final Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
        dialog.show();
    }
    public boolean checkroot(){
        check = Shell.su("su").exec();
        return check.isSuccess();

    }

}
