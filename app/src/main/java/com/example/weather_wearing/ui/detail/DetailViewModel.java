package com.example.weather_wearing.ui.detail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DetailViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public DetailViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("這是資訊fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }

}