package ru.squel.flagquiz.util;

import android.app.Application;

/**
 * Created by user on 22.04.2017.
 */

public class FlagQuizApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Injector.instance().init(this);
    }
}
