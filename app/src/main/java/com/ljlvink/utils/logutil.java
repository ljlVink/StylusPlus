package com.ljlvink.utils;

import de.robv.android.xposed.BuildConfig;
import de.robv.android.xposed.XposedBridge;

public class logutil {
    public static void log(String log){
        if(BuildConfig.DEBUG){
            XposedBridge.log("[StylusPlus_xposed]:"+log);
        }
    }
}
