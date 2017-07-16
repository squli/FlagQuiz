package ru.squel.flagquiz.util;

import android.app.Application;
import android.content.Context;

/**
 * Created by user on 22.04.2017.
 */

public class Injector {
    private static final Injector INSTANCE = new Injector();

    public static Injector instance() {
        return INSTANCE;
    }

    private Application application;

    void init(Application application) {
        this.application = application;

    }
    public Context getAppContext() {
        return this.application;
    }
}
