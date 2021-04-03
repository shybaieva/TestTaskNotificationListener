package com.talktofriend.testtask;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import static android.content.Context.CONTEXT_IGNORE_SECURITY;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {

    private Context context;
    private ArrayList titles, texts, icons, dates, times;

    public RecyclerAdapter(
            Context context,
            ArrayList titles, ArrayList texts, ArrayList icons, ArrayList dates, ArrayList times) {
        this.context = context;
        this.titles = titles;
        this.texts = texts;
        this.icons = icons;
        this.dates = dates;
        this.times = times;
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
        holder.title.setText(titles.get(position).toString());
        holder.icon.setImageResource(R.drawable.ic_launcher_background);
        holder.text.setText(texts.get(position).toString());
        holder.date.setText(dates.get(position).toString());
        holder.time.setText(times.get(position).toString());
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
}
