package com.huntmix.pw0boom;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.text.Html;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.topjohnwu.superuser.CallbackList;
import com.topjohnwu.superuser.Shell;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocalAdapter extends RecyclerView.Adapter<LocalAdapter.ViewHolder> {
    public ArrayList<ArrayList<String>> local = new ArrayList<>();
    public Context justcontext;
    public Activity act;
    public TinyDB tinydb;
    public ArrayList<ViewHolder> views = new ArrayList<>();
    public String chroot;
    public ViewHolder hold;
    public boolean run = false;
    public ArrayList<ArrayList<String>> portsip = new ArrayList<>();
    public ArrayList<String> portdata = new ArrayList<>();
    public String busybox = "su -c /data/data/com.huntmix.pw0boom/cache/busybox";
    public LocalAdapter(Context context, Activity mActivity, ArrayList<ArrayList<String>> list) {
        justcontext = context;
        local = list;
        act = mActivity;
        tinydb = new TinyDB(justcontext);
        chroot = tinydb.getString("chroot_path");

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView ip;
        public TextView company;
        public TextView ports;

        public TextView porter;
        public TextView porter2;
        public ImageView img;
        public ProgressBar progress;
        public LinearLayout localitem;

        public ViewHolder(View v) {
            super(v);

            ip =  v.findViewById(R.id.ip);
            img = v.findViewById(R.id.imglocal);
            company =  v.findViewById(R.id.company);
            ports =  v.findViewById(R.id.ports);
            porter =  v.findViewById(R.id.porter);
            porter2 =  v.findViewById(R.id.porter2);
            progress =  v.findViewById(R.id.portprogress);
            localitem =  v.findViewById(R.id.localitem);

        }

    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(justcontext).inflate(R.layout.local_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        String name = local.get(position).get(0);
        views.add(holder);
        if (position == 0){
            holder.img.setImageDrawable(justcontext.getDrawable(R.drawable.router));
        }
        if (position == getItemCount()-1){
            holder.img.setImageDrawable(justcontext.getDrawable(R.drawable.your));
        }
        if (tinydb.getBoolean("hidemack")){
        holder.ip.setText(name);}else{
        holder.ip.setText(Html.fromHtml(name+"<b> ("+local.get(position).get(1)+")</b> ", Html.FROM_HTML_MODE_COMPACT));}
        holder.company.setText(local.get(position).get(2));
        holder.ports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                  if (!run){
                      if (tinydb.getInt(name) <1) {
                          run = true;
                          holder.ports.setVisibility(View.INVISIBLE);
                          holder.progress.setVisibility(View.INVISIBLE);
                          holder.progress.setIndeterminate(true);
                          holder.progress.setVisibility(View.VISIBLE);
                          holder.porter.setText("");
                          portdata = new ArrayList<>();
                          portsip = new ArrayList<>();
                          List<String> ports = new CallbackList<String>() {
                              @Override
                              public void onAddElement(String s) {
                                  String temp = s.replaceAll("\\s+", "").replace("*", "");
                                  if (temp.contains("tcp")) {
                                      String port = temp.replaceAll("[^0-9]", "");
                                      String service = temp.replace("/tcpopen", "").replace(port, "");
                                      holder.porter.append(Html.fromHtml(port + " <b> (" + service.toUpperCase() + ") </b>"));
                                      portdata.add(port);
                                      portdata.add(service);
                                      portsip.add(portdata);
                                      portdata = new ArrayList<>();
                                  }
                                  if (temp.contains("Nmapdone")) {
                                      if (portsip.size() > 0) {
                                          holder.porter2.setVisibility(View.VISIBLE);
                                          holder.porter.setVisibility(View.VISIBLE);
                                      }
                                      holder.ports.setText("Ports\n" + portsip.size());
                                      holder.progress.setVisibility(View.INVISIBLE);
                                      holder.ports.setVisibility(View.VISIBLE);
                                      run = false;
                                  }
                              }
                          };
                          Shell.su(busybox + " chroot " + chroot + " /usr/bin/sudo nmap " + local.get(position).get(0) + " --open --top-ports 40").to(ports).submit();
                      }else{
                          toaster("Please uncut device before!");
                      }
                    }else{
                        toaster("Wait previous scan result!..");
                    }
                    }

        });


        holder.localitem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tinydb.getInt(name)<1){
                    Dialog dialog = new Dialog(justcontext,R.style.AppBottomSheetDialogTheme);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setCancelable(true);
                    LayoutInflater inflater = (LayoutInflater) justcontext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View view = inflater.inflate(R.layout.netcut_dialog, null, false);
                    TextView ip = view.findViewById(R.id.cutip);
                    TextView desc = view.findViewById(R.id.cutdesc);
                    Button no = view.findViewById(R.id.cutno);
                    Button yes = view.findViewById(R.id.cutyes);
                    ImageView img = view.findViewById(R.id.curimg);
                    if (position == 0 && getItemCount() != 1){
                        ip.setText("Cut all netwrok?");
                        desc.setText("All devices except your will be cutted!");
                        img.setImageDrawable(justcontext.getDrawable(R.drawable.cutall));
                    }else if(position == getItemCount() -1 || getItemCount() == 1){
                        ip.setText("Bad day?");
                        desc.setText("Don't be so strange. I think you still need the Internet!");
                        yes.setVisibility(View.INVISIBLE);
                        no.setVisibility(View.INVISIBLE);
                        no.setClickable(false);
                        yes.setClickable(false);
                        img.setImageDrawable(justcontext.getDrawable(R.drawable.strange));
                    }else {
                    ip.setText("Cut "+name+"?");}

                    no.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    yes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (position !=0){
                            try {
                                Process ar = Runtime.getRuntime().exec(busybox+" chroot "+chroot+" /usr/bin/sudo arping -A -S "+local.get(0).get(0)+" "+name+" -g 3003");
                                tinydb.putInt(name,getPid(ar));
                                holder.img.setColorFilter(justcontext.getResources().getColor(R.color.red));
                                dialog.dismiss();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }}else{
                                Process ar = null;
                                try {
                                    ar = Runtime.getRuntime().exec(busybox+" chroot "+chroot+" /usr/bin/sudo arping -A -S "+local.get(0).get(0)+" "+local.get(0).get(0)+" -i wlan0 -g 3003");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                tinydb.putInt(name,getPid(ar));
                                holder.ports.setVisibility(View.INVISIBLE);
                                changeicons(1);
                                holder.img.setColorFilter(justcontext.getResources().getColor(R.color.red));
                                dialog.dismiss();

                            }

                        }
                    });
                    ((Activity) justcontext).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                    dialog.setContentView(view);
                    final Window window = dialog.getWindow();
                    window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                    window.setGravity(Gravity.CENTER);
                    dialog.show();
                }else{
                    int pid = tinydb.getInt(name);
                    try {
                        Runtime.getRuntime().exec(busybox+" chroot "+chroot+" /usr/bin/sudo kill "+pid);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    holder.img.setColorFilter(justcontext.getResources().getColor(R.color.img));
                    holder.ports.setVisibility(View.VISIBLE);
                    tinydb.putInt(name,0);
                    if (position == 0){
                        changeicons(0);
                    }
                }


            }
        });

    }
    public void changeicons(int st){
        if (st == 0){
            int count = views.size()-1;
            for(int i=0; i<count; i++)
            {
                int pid = tinydb.getInt(local.get(i).get(0));
                if (pid < 1 ){
                views.get(i).img.setColorFilter(justcontext.getResources().getColor(R.color.img));}
            }
        }else{
            int count = views.size();
            for(int i=0; i<count; i++)
            {
                int pid = tinydb.getInt(local.get(i).get(0));
                if (pid < 1 && i != getItemCount()-1){
                views.get(i).img.setColorFilter(justcontext.getResources().getColor(R.color.red));}
            }
        }
    }
    public static int getPid(Process p) {
        int pid = -1;

        try {
            Field f = p.getClass().getDeclaredField("pid");
            f.setAccessible(true);
            pid = f.getInt(p);
            f.setAccessible(false);

        } catch (Throwable ignored) {
            try {
                Matcher m = Pattern.compile("pid=(\\d+)").matcher(p.toString());
                pid = m.find() ? Integer.parseInt(m.group(1)) : -1;
            } catch (Throwable ignored2) {
                pid = -1;
            }
        }
        return pid;
    }
    @Override
    public int getItemCount() {
        return local.size();
    }

    public void toaster(String msg){
        Toast toast = Toast.makeText(justcontext,
                msg, Toast.LENGTH_SHORT);
        toast.show();
    }
}
