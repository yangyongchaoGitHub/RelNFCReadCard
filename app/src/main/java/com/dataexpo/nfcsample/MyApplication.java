package com.dataexpo.nfcsample;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {
    private static Context context;

    public final static int PERMISSION_SELECT_WAIT = 0;
    public final static int PERMISSION_SELECT_OK = 1;

    private static int permissionSelect = PERMISSION_SELECT_WAIT;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }

    public static int getPermissionSelect() {
        return permissionSelect;
    }

    public static void setPermissionSelect(int select) {
        permissionSelect = select;
    }
}
