package com.talktofriend.testtask;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class FilterAlertDialog extends DialogFragment {

    private ImageButton allNotifications, perHour, perDay, perMonth;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.filter_alert_dialog_layout, null);

        allNotifications = view.findViewById(R.id.allBtn);
        perHour = view.findViewById(R.id.perHourBtn);
        perDay = view.findViewById(R.id.perDayBtn);
        perMonth = view.findViewById(R.id.perMounthBtn);


        allNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "ALL", Toast.LENGTH_SHORT).show();
            }
        });

        perHour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        perDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        perMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        builder.setView(view);
        return builder.create();
    }
}
