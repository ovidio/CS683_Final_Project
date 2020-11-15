package com.noyouaint.cs683_ovidio_reyna_weight_tracking_app.ui.weight;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class WeightViewModel extends ViewModel {
    private MutableLiveData<String> mText;

    public WeightViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is the weight fragment");
    }

    public LiveData<String> getText() { return mText; }
}