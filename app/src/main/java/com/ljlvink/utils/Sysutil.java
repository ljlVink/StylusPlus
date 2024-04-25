package com.ljlvink.utils;

public class Sysutil {
    public static String getprop(String str) {
        try {
            Class<?>[] clsArr = {String.class};
            Object[] objArr = {str};
            Class<?> cls = Class.forName("android.os.SystemProperties");
            return (String) cls.getDeclaredMethod("get", clsArr).invoke(cls, objArr);
        } catch (Throwable th) {
            return "";
        }
    }
    public static void PrintStackTrace(){
        logutil.log("Dump Stack: ---------------start----------------");
        Throwable ex = new Throwable();
        StackTraceElement[] stackElements = ex.getStackTrace();
        if (stackElements != null) {
            for (int i = 0; i < stackElements.length; i++) {
                logutil.log("Dump Stack"+i+": "+ stackElements[i].getClassName()
                        +"----"+stackElements[i].getFileName()
                        +"----" + stackElements[i].getLineNumber()
                        +"----" +stackElements[i].getMethodName());
            }
        }
        logutil.log("Dump Stack:  ---------------over----------------");

    }
}
