package com.noyouaint.cs683_ovidio_reyna_weight_tracking_app.ui.weight;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.noyouaint.cs683_ovidio_reyna_weight_tracking_app.R;

public class WeightFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                         ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_weight, container, false);
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        TextView weightText = (TextView) view.findViewById(R.id.text_weight);
        weightText.setText("This is the weight fragment");
    }
}