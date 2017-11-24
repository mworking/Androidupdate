package com.vector.appupdatedemo;

import android.app.Application;

import com.lzy.okgo.OkGo;

/**
 * Created by Vector
 * on 2017/7/17 0017.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        OkGo.getInstance().init(this);
    }
}
