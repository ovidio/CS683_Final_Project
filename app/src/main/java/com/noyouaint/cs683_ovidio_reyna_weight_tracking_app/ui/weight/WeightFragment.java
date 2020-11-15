package com.noyouaint.cs683_ovidio_reyna_weight_tracking_app.ui.weight;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

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

    private WeightViewModel weightViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                         ViewGroup container, Bundle savedInstanceState) {
        weightViewModel =
                new ViewModelProvider(this).get(WeightViewModel.class);
        View root = inflater.inflate(R.layout.weight_fragment, container, false);
        final TextView textView = root.findViewById(R.id.text_weight);
        weightViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        return root;
    }
}