package com.example.cats_catch_mice.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.cats_catch_mice.Test;

public class HomeViewModel extends ViewModel {

//    private final MutableLiveData<String> mText;
    private Test test;

    public HomeViewModel() {
//        mText = new MutableLiveData<>();
        //mText.setValue("This is home fragment");
    }

    public void initTest(int a){
        this.test = new Test(a);
    }

    public Test getTest(){
        return this.test;
    }

//    public LiveData<String> getText() {
//        return mText;
//    }
}