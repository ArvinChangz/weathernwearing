package com.example.weather_wearing.ui.map;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MapViewModel extends ViewModel {

    private MutableLiveData<String> mText;



    public MapViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is weathermap fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}