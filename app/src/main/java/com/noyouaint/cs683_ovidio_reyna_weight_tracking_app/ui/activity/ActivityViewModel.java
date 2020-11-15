package com.noyouaint.cs683_ovidio_reyna_weight_tracking_app.ui.activity;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ActivityViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ActivityViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is the activity fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }

}