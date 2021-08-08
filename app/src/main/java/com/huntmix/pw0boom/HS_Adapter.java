package com.huntmix.pw0boom;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
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

public class HS_Adapter extends RecyclerView.Adapter<HS_Adapter.ViewHolder> {
    public ArrayList<String> hslist = new ArrayList<>();
    public Context justcontext;
    public String name;
    public Activity act;
    public EditText email;
    public TinyDB tinydb;
    public String chroot;
    public String busybox = "su -c /data/data/com.huntmix.pw0boom/cache/busybox";
    public HS_Adapter(Context context, Activity mActivity, ArrayList<String> list) {
        justcontext = context;
        hslist = list;
        act = mActivity;
        tinydb = new TinyDB(justcontext);
        chroot = tinydb.getString("chroot_path");

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView hsname;
        public TextView hspath;
        public RelativeLayout hsitem;

        public ViewHolder(View v) {
            super(v);

            hsname =  v.findViewById(R.id.hsname);
            hsitem =  v.findViewById(R.id.itemhs);
            hspath =  v.findViewById(R.id.hsdest);


        }

    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(justcontext).inflate(R.layout.hs_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        String name = hslist.get(position).replace("/storage/emulated/0/Pw0/hs/","").replace(".cap","");
        holder.hsname.setText(name);
        holder.hspath.setText(hslist.get(position));
        holder.hsitem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheetDialog(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        // Count the installed apps
        return hslist.size();
    }
public void sendhs(String path){
    Dialog dialog = new Dialog(justcontext,R.style.AppBottomSheetDialogTheme);
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialog.setCancelable(true);
    String sendpath = path.replace("/storage/emulated/0/","/sdcard/");
    LayoutInflater inflater = (LayoutInflater) justcontext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View view = inflater.inflate(R.layout.emaildialog, null, false);
    TextView status = view.findViewById(R.id.emailstatus);
    TextView cancel = view.findViewById(R.id.cancelhs);
    TextView tophs = view.findViewById(R.id.tophs);
    Button ok = view.findViewById(R.id.send);
     email = view.findViewById(R.id.email);
    cancel.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dialog.dismiss();
        }
    });
    ok.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
           String mail  = String.valueOf(email.getText());
           if (mail.length() > 10 && mail.contains("@")){
               hideKeyboardFrom(justcontext,view);
               List<String>  check = new CallbackList<String>() {
                   @Override
                   public void onAddElement(String s) {
                      if (s.contains("successfully")){
                          status.setText("Sended! Please check email and activate account!");
                          tophs.setText("Success!");
                          email.setVisibility(View.INVISIBLE);
                          ok.setVisibility(View.INVISIBLE);
                      }
                   }
               };
               Shell.su(busybox+" chroot "+chroot+" /usr/bin/sudo curl -X POST -F 'email="+mail+"' -F 'file=@"+sendpath+"' https://api.onlinehashcrack.com").to(check).submit();
           }
        }
    });
    ((Activity) justcontext).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    dialog.setContentView(view);
    final Window window = dialog.getWindow();
    window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    window.setGravity(Gravity.CENTER);
    dialog.show();
}
    private void showBottomSheetDialog(int id) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(justcontext,R.style.AppBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.hs_dialog);
        TextView hsap = bottomSheetDialog.findViewById(R.id.hsap);
        TextView hspath = bottomSheetDialog.findViewById(R.id.hspath);
        Button sendhs = bottomSheetDialog.findViewById(R.id.upload);
        sendhs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendhs(hslist.get(id));
            }
        });
        String name = hslist.get(id).replace("/storage/emulated/0/Pw0/hs/","").replace(".cap","");
        String path = hslist.get(id);
        hspath.setText(path);
        hsap.setText(name);
        bottomSheetDialog.show();

    }
    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
