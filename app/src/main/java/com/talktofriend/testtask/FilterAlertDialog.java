package com.talktofriend.testtask;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
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
    private Activity activity;
    private int lastChoice;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof Activity)
            activity = (Activity) context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.filter_alert_dialog_layout, null);

        allNotifications = view.findViewById(R.id.allBtn);
        perHour = view.findViewById(R.id.perHourBtn);
        perDay = view.findViewById(R.id.perDayBtn);
        perMonth = view.findViewById(R.id.perMounthBtn);

        allNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "ALL", Toast.LENGTH_SHORT).show();
                sendChoiceToActivity(1);
            }
        });

        perHour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendChoiceToActivity(2);
            }
        });

        perDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendChoiceToActivity(3);
            }
        });

        perMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendChoiceToActivity(4);
            }
        });

        builder.setView(view);
        return builder.create();
    }

    private void sendChoiceToActivity(int filterChoice){
        try {
            ((GetFilterChoice) activity).onSendFilterChoice(filterChoice);
        } catch (ClassCastException ignored) { }
        dismiss();
    }
}
