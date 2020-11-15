package com.noyouaint.cs683_ovidio_reyna_weight_tracking_app.ui.nutrition;

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

public class NutritionFragment extends Fragment {

    private NutritionViewModel nutritionViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                         ViewGroup container, Bundle savedInstanceState) {
        nutritionViewModel =
                new ViewModelProvider(this).get(NutritionViewModel.class);
        View root = inflater.inflate(R.layout.nutrition_fragment, container, false);
        final TextView textView = root.findViewById(R.id.text_nutrition);
        nutritionViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        return root;
    }
}