package com.huntmix.pw0boom;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;


import com.topjohnwu.superuser.CallbackList;
import com.topjohnwu.superuser.Shell;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ir.alirezabdn.wp7progress.WP10ProgressBar;
import ir.alirezabdn.wp7progress.WP7ProgressBar;
import pl.droidsonroids.gif.GifImageView;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

public class MainActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    public String deviceid = "";
    public Context context;
    public  Toolbar toolbar;
    private ViewPager mViewPager;
    public int unpack;
    private ImageView monmode;
    public boolean mon;
    public int test = 0;
    public Dialog dialog;
    public Shell.Result res;
    public int statusmon = 0;
    public String wlan;
    public Button button;
    public TextView output;
    public Button cancel;
    public TinyDB tinydb;
    public int mntstatus = 0;
    public String busybox = "su -c /data/data/com.huntmix.pw0boom/cache/busybox";
    public int chrootstatus = 0;
    public String chroot = "";
    public  TabLayout tabLayout;
    public BottomSheetDialog bottomSheetDialog;
    final int[] ICONS = new int[]{
            R.drawable.icon1,
            R.drawable.icon2,
            R.drawable.network,
            R.drawable.settings,
            R.drawable.icon4

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tinydb = new TinyDB(this);
        if (!tinydb.getBoolean("ft")){
        copyAssets();
        tinydb.putString("wlan","wlan0");
        tinydb.putString("wlan2","wlan1");
        tinydb.putString("chroot_path","/data/local/pw0/test/");
        tinydb.putString("busybox_path","/data/data/com.huntmix.pw0boom/cache/busybox");
        tinydb.putBoolean("ft",true);
        }
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(4);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.getTabAt(0).setIcon(ICONS[0]);
        tabLayout.getTabAt(1).setIcon(ICONS[1]);
        tabLayout.getTabAt(2).setIcon(ICONS[2]);
        tabLayout.getTabAt(3).setIcon(ICONS[3]);
        tabLayout.getTabAt(4).setIcon(ICONS[4]);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        TextView text = findViewById(R.id.source);
        if (!text.getText().toString().contains("t.me/kali_nh")){
            System.exit(1);
        }
        monmode = findViewById(R.id.monmode);
        chroot = tinydb.getString("chroot_path");
        wlan = tinydb.getString("wlan");
        checkmonitore();
        Shell.su("mkdir /data/local/").exec();
        Shell.su("mkdir /data/local/pw0/").exec();
        Shell.su("chmod 777 /data/data/com.huntmix.pw0boom/cache/bootkali_bash").exec();
        mounter();
        checkpermission();


    }



    public void mounter(){
        if (!getPackageName().equals("com.huntmix.pw0boom")){
            System.exit(1);
        }
        mntstatus = 0;
        dialog = new Dialog(this,R.style.AppBottomSheetDialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.mount_dialog, null, false);
        TextView status = view.findViewById(R.id.mnt_status);
         output = view.findViewById(R.id.mnt_output);
        Button locate = view.findViewById(R.id.setlocation);
        Button install = view.findViewById(R.id.install);
        Button file = view.findViewById(R.id.getfile);
        ProgressBar bar = view.findViewById(R.id.prgress);
        GifImageView gif = view.findViewById(R.id.mountcat);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.setContentView(view);
        output.append("Pw0 Framewrok 0.2 BETA (by @Huntmix)");
        output.append("\n");
        chroot = tinydb.getString("chroot_path");
        install.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Shell.su("rm /storage/emulated/0/Download/pw0.tar.gz").submit();
                status.setText("Downloading chroot...");
                gif.setBackgroundResource(R.drawable.catlisten);locate.setVisibility(View.INVISIBLE);
                install.setVisibility(View.INVISIBLE);
                file.setVisibility(View.INVISIBLE);
                new Thread(new Runnable() {

                    @Override
                    public void run() {



                        boolean downloading = true;

                        String url = "https://gitlab.com/huntmix/pw0/-/raw/main/pw0-B1.tar.gz";
                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                        request.setDescription("Downloading chroot..");
                        request.setTitle("Please wait");
                        request.allowScanningByMediaScanner();
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "pw0.tar.gz");
                        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                        long downloadID = manager.enqueue(request);
                        while (downloading) {

                            DownloadManager.Query q = new DownloadManager.Query();
                            q.setFilterById(downloadID);
                            Cursor cursor = manager.query(q);
                            cursor.moveToFirst();
                            int bytes_downloaded = cursor.getInt(cursor
                                    .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                            int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                            if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                                downloading = false;
                            }

                            final int dl_progress = (int) ((bytes_downloaded * 100l) / bytes_total);

                            if (dl_progress == 100){


                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        output.setText(green("Download complete! Installing..."));
                                        bar.setVisibility(View.INVISIBLE);
                                        bar.setIndeterminate(true);
                                        bar.setVisibility(View.VISIBLE);

                                          Timer time = new Timer();
                                        time.scheduleAtFixedRate(new TimerTask() {
                                            @Override
                                            public void run() {
                                               runOnUiThread(new Runnable() {
                                                   @Override
                                                   public void run() {
                                                       unpack = unpack+1;


                                                           output.setText(yellow("Unpacking... "+unpack+"s"));

                                                   }
                                               });
                                            }
                                        },0,1000);
                                        List<String> res = new CallbackList<String>() {
                                            @Override
                                            public void onAddElement(String s) {
                                                if (s.contains("ok")){
                                                    output.append("\n");
                                                    output.append(green("Installed! Everything ok"));
                                                    time.cancel();
                                                    dialog.dismiss();
                                                    chroot = "/data/local/pw0/test/";
                                                    tinydb.putString("chroot_path",chroot);
                                                    mounter();
                                                }
                                            }
                                        };


                                        Shell.su("mkdir /data/local").exec();
                                        Shell.su("chmod 777 /data/data/com.huntmix.pw0boom/cache/busybox").exec();
                                        Shell.su("mkdir /data/local/pw0").exec();
                                        Shell.su(busybox+" tar -xf /storage/emulated/0/Download/pw0.tar.gz -C /data/local/pw0 && echo 'ok'" ).to(res).submit();

                                    }
                                });
                                }

                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    if (dl_progress !=100 && dl_progress !=99){
                                    output.setText(green("Downloading... "+bytes_downloaded/1024/1024+"MB/"+bytes_total/1024/1024+"MB ("+dl_progress+"%)"));}
                                    bar.setProgress(dl_progress);
                                    if (dl_progress == 99){
                                        output.setText(green("Checking file, please wait..."));
                                    }
                                }
                            });
                            cursor.close();
                        }

                    }
                }).start();



            }

        });
        locate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getpath();


            }
        });
        file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupfile();

            }
        });
        final Handler handler2 = new Handler(Looper.getMainLooper());
        handler2.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e("chrootcheck", String.valueOf(chrootstatus));



                File test = new File(chroot);
                if (test.exists() && test.isDirectory()){
                    output.append(green("[+] Trying to mount chroot..."));
                    output.append("\n");
                    List<String> checkmount = new CallbackList<String>() {
                        @Override
                        public void onAddElement(String s) {
                            if (s.contains("[+]")){
                                output.append(green(s));
                                output.append("\n");}
                            if (s.contains("[-]") ){
                                output.append(red(s));
                                output.append("\n");}
                            if (s.contains("[!]")){
                                output.append(yellow(s));
                                output.append("\n");}
                            if (s.contains("started")){
                                mntstatus = mntstatus + 1;
                                dialog.dismiss();
                            }
                            if (s.contains("sudo not found") || s.contains("file exists")){
                                output.append(red("Please install chroot or set other location!"));
                                status.setText("Ooops, error...");
                                output.append("\n");
                                gif.setBackgroundResource(R.drawable.caterror);
                                locate.setVisibility(View.VISIBLE);
                                install.setVisibility(View.VISIBLE);
                                file.setVisibility(View.VISIBLE);
                            }
                        }
                    };
                    final Handler handler3 = new Handler(Looper.getMainLooper());
                    handler3.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Shell.su("chmod +x /data/data/com.huntmix.pw0boom/cache/bootroot").submit();
                            Shell.su("su -mm -c /data/data/com.huntmix.pw0boom/cache/bootroot").to(checkmount).submit();
                        }
                    }, 2000);
                }else{

                    status.setText("No chroot installed!");
                    output.append(red("[-] No chroot located "+chroot));
                    output.append("\n");
                    output.append(yellow("[!] Locate chroot directory or install below"));
                    output.append("\n");
                    locate.setVisibility(View.VISIBLE);
                    install.setVisibility(View.VISIBLE);
                    file.setVisibility(View.VISIBLE);
                }
            }
        }, 3000);
        final Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);


        dialog.show();
    }


    public String getpath(){
         Dialog getdialog = new Dialog(this,R.style.AppBottomSheetDialogTheme);
         getdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
         getdialog.setCancelable(true);
         LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         View view = inflater.inflate(R.layout.editpath, null, false);
         EditText error = view.findViewById(R.id.dpath);
         Button ok = view.findViewById(R.id.setpath);
         ok.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 chroot = String.valueOf(error.getText());
                 tinydb.putString("chroot_path",chroot);
                 dialog.dismiss();
                 mounter();
                 getdialog.dismiss();
             }
         });

         getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
         getdialog.setContentView(view);
         final Window window = getdialog.getWindow();
         window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
         window.setGravity(Gravity.CENTER);
         getdialog.show();
return chroot;
 }
    public void setupfile(){
        Dialog getdialog = new Dialog(this,R.style.AppBottomSheetDialogTheme);
        getdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getdialog.setCancelable(true);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.editpath, null, false);
        EditText error = view.findViewById(R.id.dpath);
        error.setText("/storage/emulated/0/Download/");
        Button ok = view.findViewById(R.id.setpath);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File f = new File(String.valueOf(error.getText()));
                if (f.exists()){
                    getdialog.dismiss();
                    Timer time = new Timer();
                    time.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    unpack = unpack+1;


                                    output.setText(yellow("Unpacking... "+unpack+"s"));

                                }
                            });
                        }
                    },0,1000);
                    List<String> unpack = new CallbackList<String>() {
                        @Override
                        public void onAddElement(String s) {
                            if (s.equals("ok")){
                                dialog.dismiss();
                                time.cancel();
                                mounter();
                            }
                        }
                    };
                    Shell.su("mkdir /data/local").exec();
                    Shell.su("mkdir /data/local/pw0").exec();
                    Shell.su("su -c chmod 777 /data/data/com.huntmix.pw0boom/cache/busybox").exec();
                    Shell.su(busybox+" tar -xf "+error.getText()+" -C /data/local/pw0 && echo 'ok'" ).to(unpack).submit();
                }else{
                    getdialog.dismiss();
                    output.setText(red("[-] File not found! ("+error.getText()+")"));
                }
            }
        });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        getdialog.setContentView(view);
        final Window window = getdialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
        getdialog.show();

    }

    public void checkmonitore(){

    List<String> out = output("iw dev");
    List<String> clear = new ArrayList<>();
    for (int j = 0;j < out.size();j++){
            String temp = out.get(j).replaceAll("\\s+","");
            temp.replaceAll(" ","");
            clear.add(temp);}

    if (clear.contains("typemanaged")){
        mon = false;

        monmode.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.startmon));
    } else if (clear.contains("typemonitor")){
        mon = true;
        monmode.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.stopmon));
    }

}
    public void monitore(View view){


        if (!mon){start();
        checkmonitore();
        if (!mon){snack("Failed");}
        else {snack("Success");monmode.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.stopmon));}
        }
        else if (mon){off();
            checkmonitore();
            if (mon){snack("Failed");}
            else {snack("Success");monmode.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.startmon));
        }}
}
    public void start() {

        String wlan = tinydb.getString("wlan");
        if (wlan.equals("wlan0")){
        Shell.su("ip link set wlan0 down").exec();
        Shell.su("echo 4 > /sys/module/wlan/parameters/con_mode").exec();
        Shell.su("ip link set wlan0 up").exec();}

    }
    public void off() {
        String wlan = tinydb.getString("wlan");
        if (wlan.equals("wlan0")){
        Shell.su("ip link set wlan0 down").exec();
        Shell.su("ip link set wlan0 up").exec();
        Shell.su("echo 0 > /sys/module/wlan/parameters/con_mode").exec();
        Shell.su("svc wifi enable").exec(); }

    }
    public List<String> output(String command){
        List<String> logs = new ArrayList<>();

                Shell.su(command).to(logs).exec();

        return logs;
    }
    public void snack(String text){
        Snackbar.make(mViewPager, text, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkmonitore();
    }

    public static class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @NotNull
        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position) {
                case 0:
                    fragment = new WiFi();
                    break;
                case 1:
                    fragment = new Handshakes();
                    break;
                case 2:
                    fragment = new LocalNetwork();
                    break;
                case 3:
                    fragment = new Setting();
                    break;
                case 4:
                    fragment = new About();
            }
            assert fragment != null;
            return fragment;
        }
        @Override
        public int getCount() {
            // Show 4 total pages.
            return 5;
        }
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "";
                case 1:
                    return "";
                case 2:
                    return "";
                case 3:
                    return "";
                case 4:
                    return "";
            }
            return null;
        }
    }

    public void checkusb(View view){
        showBottomSheetDialog();
    }
    public List<String> checkdevice(){
        List<String> temp = new ArrayList<>();
        UsbManager manager = (UsbManager) MainActivity.this.getSystemService(Context.USB_SERVICE);
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
            deviceid = string2+":"+string3;

            temp.add(string);
            temp.add(manu);
            temp.add(string2);
            temp.add(string3);
        }
        return temp;
    }
    public void mon1(View view){
        String wlanmon = tinydb.getString("wlan2");
        cancel.setEnabled(false);

    List<String> callbackList = new CallbackList<String>() {
        @Override
        public void onAddElement(String s) {
            String temp = s.replaceAll("\\s+","").replace("*","");
            if (temp.contains("mac80211monitormodevifenabled")){
                button.setText("SUCCESS!");
                cancel.setEnabled(true);
                statusmon = 1;
            }
        }
    };
        Shell.su(busybox+" chroot "+chroot+" /usr/bin/sudo airmon-ng start "+wlanmon).to(callbackList).submit();

        cancel.setEnabled(true);

}
    private void showBottomSheetDialog() {
        bottomSheetDialog = new BottomSheetDialog(MainActivity.this,R.style.AppBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.usb_dialog);
        bottomSheetDialog.setCancelable(false);
        TextView status = bottomSheetDialog.findViewById(R.id.status);
        TextView usbinfo = bottomSheetDialog.findViewById(R.id.usbinfo);
        TextView drivercheck = bottomSheetDialog.findViewById(R.id.drivercheck);
        TextView detected = bottomSheetDialog.findViewById(R.id.detected);
        WP10ProgressBar progress = bottomSheetDialog.findViewById(R.id.checkbar);
        WP7ProgressBar wait = bottomSheetDialog.findViewById(R.id.waitbar);

        wait.showProgressBar();
        progress.showProgressBar();
        button = bottomSheetDialog.findViewById(R.id.monbutton);
        cancel = bottomSheetDialog.findViewById(R.id.dismiss);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!checkdevice().isEmpty()){
                            List<String> temp = checkdevice();
                            status.setTextColor(getColor(R.color.green));
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
                                    test = test+1;
                                    String temp = s.replaceAll("\\s+","").replace("*","");
                                    if (temp.contains(wlan)){
                                        progress.setIndicatorColor(R.color.green);
                                        drivercheck.setText(drivercheck.getText()+"   OK");
                                        button.setEnabled(true);
                                    }

                                    timer.cancel();
                                }
                            };
                            Shell.su(busybox+" chroot "+chroot+" /usr/bin/sudo airmon-ng | grep "+wlan)
                                    .to(callbackList)
                                    .submit();


                        }
                    }
                });
            }
        }, 0, 1000);


        bottomSheetDialog.show();

    }

    public void checkpermission(){
        if(SDK_INT>=Build.VERSION_CODES.M){

            if(checkSelfPermission(WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                if(shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)){
                    ActivityCompat.requestPermissions(
                            this,
                            new String[]{WRITE_EXTERNAL_STORAGE},
                            123
                    );

                }else {
                    // Request permission
                    ActivityCompat.requestPermissions(
                            this,
                            new String[]{WRITE_EXTERNAL_STORAGE},
                            123
                    );
                }
            }
        }

        if (SDK_INT >= Build.VERSION_CODES.R) {

                            try {
                                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                                intent.addCategory("android.intent.category.DEFAULT");
                                intent.setData(Uri.parse(String.format("package:%s",getApplicationContext().getPackageName())));
                                startActivityForResult(intent, 2296);
                            } catch (Exception e) {
                                Intent intent = new Intent();
                                intent.setAction(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                                startActivityForResult(intent, 2296);
                            }
                        }



        else{
            ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, 123);
        }

    }
    private void copyAssets() {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        if (files != null) for (String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(filename);
                File outFile = new File("/data/data/com.huntmix.pw0boom/cache/", filename);
                out = new FileOutputStream(outFile);
                copyFile(in, out);
            } catch(IOException e) {
                Log.e("tag", "Failed to copy asset file: " + filename, e);
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
            }
        }
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
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }
}