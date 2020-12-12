package com.noyouaint.cs683_ovidio_reyna_weight_tracking_app.ui;

import android.view.View;
import androidx.fragment.app.Fragment;

import com.noyouaint.cs683_ovidio_reyna_weight_tracking_app.R;

public abstract class BaseFragment extends Fragment {

    public View setBackground(View view) {
        view.setBackgroundColor(getResources().getColor(R.color.background_analogous_color));
        return view;
    }
}
