package com.noyouaint.cs683_ovidio_reyna_weight_tracking_app.ui.activity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.noyouaint.cs683_ovidio_reyna_weight_tracking_app.R;

public class ActivityFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_activity, container, false);
    }

}