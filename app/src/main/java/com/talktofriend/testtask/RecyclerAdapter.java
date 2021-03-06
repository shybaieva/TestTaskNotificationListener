package com.talktofriend.testtask;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {

    private Context context;
    private ArrayList titles, texts, icons, dates, times, packageNames;

//, ArrayList packageNames
    public RecyclerAdapter(
            Context context,
            ArrayList titles, ArrayList texts, ArrayList icons, ArrayList dates, ArrayList times, ArrayList packageNames) {
        this.context = context;
        this.titles = titles;
        this.texts = texts;
        this.icons = icons;
        this.dates = dates;
        this.times = times;
        this.packageNames = packageNames;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.notification_item_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.title.setText(checkStringSize(titles.get(position).toString()));
        ///holder.icon.setImageResource((Integer) icons.get(position));
        holder.icon.setImageDrawable(getAllIcons(packageNames.get(position).toString()));
        holder.text.setText(checkStringSize(texts.get(position).toString()));
        holder.date.setText(checkStringSize(dates.get(position).toString()));
        holder.time.setText(checkStringSize(times.get(position).toString()));
    }

    @Override
    public int getItemCount() {
        return titles.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title, text, date, time;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.notificationIco);
            title = itemView.findViewById(R.id.notificationTitle);
            text = itemView.findViewById(R.id.notificationText);
            date = itemView.findViewById(R.id.noificationDate);
            time = itemView.findViewById(R.id.noificationTime);
        }


    }

    private String checkStringSize(String str){
        if(str.length()>15)
            return str.substring(0, 16);
        else return str;
    }

    private Drawable getAllIcons(String packageName){
        Drawable appicon = null;
        try {
          appicon = context.getPackageManager().getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appicon;
    }
}
