package com.ljlvink.Hook;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookMain implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        String pkgname=loadPackageParam.packageName;
        ClassLoader classLoader=loadPackageParam.classLoader;
        XposedBridge.log("123");
        if(pkgname.equals("com.anddnd.com")){
            XposedBridge.log("123");
        }
    }

}
