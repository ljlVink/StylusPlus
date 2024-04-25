package com.ljlvink.Hook;
import com.ljlvink.utils.Sysutil;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookMain implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        ClassLoader classLoader = loadPackageParam.classLoader;
        if(loadPackageParam.packageName.equals("android")){
            String fingerprint=Sysutil.getprop("ro.build.fingerprint");
            if(fingerprint.contains("liuqin")||fingerprint.contains("pipa")){
                XposedHelpers.findAndHookMethod("com.android.server.power.PowerManagerService", classLoader, "onStart", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        XposedHelpers.callMethod(param.thisObject,"lowLevelReboot","quiescent");
                    }
                });
            }
            new StylusPlus().Payload(classLoader);
        }
    }
}
