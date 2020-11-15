package com.noyouaint.cs683_ovidio_reyna_weight_tracking_app.ui.nutrition;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class NutritionViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public NutritionViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is nutrition fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}