package com.huntmix.pw0boom;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class Setting extends Fragment {

    public LinearLayout hidemack;
    public LinearLayout hideap;
    public LinearLayout setscan;
    public LinearLayout setdeauth;
    public LinearLayout setbusybox;
    public LinearLayout setchroot;
    public CheckBox chkmac;
    public CheckBox chkapname;
    public Boolean mac;
    public Boolean ap;
    public TextView scanwlan;
    public TextView deauthwlan;
    public TextView nowchroot;
    public TextView nowbusybox;
    public String chroot;
    public TinyDB tinydb;
    public Context context;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View viewroot = inflater.inflate(R.layout.settings, container, false);
        context = getContext();
        tinydb = new TinyDB(context);
        chroot = tinydb.getString("chroot_path");
        mac = tinydb.getBoolean("hidemac");
        ap = tinydb.getBoolean("hideap");
        hidemack = viewroot.findViewById(R.id.hidemac);
        hideap = viewroot.findViewById(R.id.hideap);
        setscan = viewroot.findViewById(R.id.selectwlan);
        setdeauth = viewroot.findViewById(R.id.selectdeauth);
        setbusybox = viewroot.findViewById(R.id.busybox_cmd);
        setchroot = viewroot.findViewById(R.id.selectchoot);
        chkmac = viewroot.findViewById(R.id.chkmac);
        chkapname = viewroot.findViewById(R.id.chkap);
        scanwlan = viewroot.findViewById(R.id.currentscan);
        deauthwlan = viewroot.findViewById(R.id.currentdeauth);
        nowchroot = viewroot.findViewById(R.id.currentchroot);
        nowbusybox = viewroot.findViewById(R.id.currentbusybox);
        chkmac.setChecked(mac);
        chkapname.setChecked(ap);
        nowchroot.setText("Current: "+tinydb.getString("chroot_path"));
        nowbusybox.setText("Current: "+tinydb.getString("busybox_path"));
        scanwlan.setText("Current: "+tinydb.getString("wlan"));
        deauthwlan.setText("Current: "+tinydb.getString("wlan2"));

        hidemack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chkmac.isChecked()){chkmac.setChecked(false);tinydb.putBoolean("hidemac",false);}
                else if(!chkmac.isChecked()){chkmac.setChecked(true);tinydb.putBoolean("hidemac",true);}
            }
        });
        hideap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chkapname.isChecked()){chkapname.setChecked(false);tinydb.putBoolean("hideap",false);}
                else if(!chkapname.isChecked()){chkapname.setChecked(true);tinydb.putBoolean("hideap",true);}
            }
        });
        setscan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setwlan(false);
            }
        });
        setdeauth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setwlan(true);
            }
        });
        setchroot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getpath(false);
            }
        });
        setbusybox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getpath(true);
            }
        });
        return viewroot;
    }
    public void setwlan(boolean type){
        Dialog dialog = new Dialog(context,R.style.AppBottomSheetDialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.setwlan, null, false);
        RadioButton w0 = view.findViewById(R.id.wl0);
        RadioButton w1 = view.findViewById(R.id.wl1);
        RadioButton w2 = view.findViewById(R.id.wl2);
        RadioButton w3 = view.findViewById(R.id.wl3);
        if(type){w0.setClickable(false);w0.setVisibility(View.INVISIBLE);}
        w0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!type){
                tinydb.putString("wlan","wlan0");}else{
                    tinydb.putString("wlan2","wlan0");
                }
                dialog.dismiss();
                if (!type){scanwlan.setText("Current: "+tinydb.getString("wlan"));}else{deauthwlan.setText("Current: "+tinydb.getString("wlan2"));}

            }
        });
        w1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!type){
                    tinydb.putString("wlan","wlan1");}else{
                    tinydb.putString("wlan2","wlan1");
                }
                dialog.dismiss();
                if (!type){scanwlan.setText("Current: "+tinydb.getString("wlan"));}else{deauthwlan.setText("Current: "+tinydb.getString("wlan2"));}
            }
        });
        w2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!type){
                    tinydb.putString("wlan","wlan2");}else{
                    tinydb.putString("wlan2","wlan2");
                }
                dialog.dismiss();
                if (!type){scanwlan.setText("Current: "+tinydb.getString("wlan"));}else{deauthwlan.setText("Current: "+tinydb.getString("wlan2"));}
            }
        });
        w3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!type){
                    tinydb.putString("wlan","wlan3");}else{
                    tinydb.putString("wlan2","wlan3");
                }
                dialog.dismiss();
                if (!type){scanwlan.setText("Current: "+tinydb.getString("wlan"));}else{deauthwlan.setText("Current: "+tinydb.getString("wlan2"));}
            }
        });
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.setContentView(view);
        final Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
        dialog.show();
    }


    public void getpath(boolean type){
        Dialog getdialog = new Dialog(context,R.style.AppBottomSheetDialogTheme);
        getdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getdialog.setCancelable(true);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.editpath, null, false);
        EditText error = view.findViewById(R.id.dpath);
        Button ok = view.findViewById(R.id.setpath);
        if (!type){error.setText(tinydb.getString("chroot_path"));}else{error.setText(tinydb.getString("busybox_path"));}
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chroot = String.valueOf(error.getText());
                if(!type){if (!chroot.endsWith("/")){chroot = chroot+"/";}}
                if(type){if (chroot.endsWith("/")){chroot = chroot.substring(0,chroot.length()-1);}}
                if (!type){tinydb.putString("chroot_path",chroot);nowchroot.setText("Current: "+tinydb.getString("chroot_path"));}else{tinydb.putString("busybox_path",chroot);nowbusybox.setText("Current: "+tinydb.getString("busybox_path"));}

                getdialog.dismiss();
            }
        });
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        getdialog.setContentView(view);
        final Window window = getdialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
        getdialog.show();
    }

    public Spanned green (String out){
        Spanned formated = Html.fromHtml("<font color='#19D121'>"+out+"</font>");
        return formated;
    }
    public Spanned yellow (String out){
        Spanned formated = Html.fromHtml("<font color='#F9D625'>"+out+"</font>");
        return formated;
    }
    public Spanned red (String out){
        Spanned formated = Html.fromHtml("<font color='#F60B0B'>"+out+"</font>");
        return formated;
    }
}