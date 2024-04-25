package com.ljlvink.Hook;
import com.ljlvink.utils.Sysutil;
import com.ljlvink.utils.logutil;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookMain implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        ClassLoader classLoader = loadPackageParam.classLoader;
        if(loadPackageParam.packageName.equals("android")){
            String fingerprint=Sysutil.getprop("ro.build.fingerprint");
            if(fingerprint.contains("liuqin")||fingerprint.contains("pipa")){
                logutil.log("Device Not Supported. Exiting");
                return;
            }
            new StylusPlus().Payload(classLoader);
        }
    }
}
