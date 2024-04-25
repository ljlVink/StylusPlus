package com.ljlvink.Hook;

import android.graphics.Path;
import android.graphics.PointF;

import com.ljlvink.utils.logutil;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class StylusPlus {
    public StylusPlus(){}
    public boolean DownPres=true;

    public PointF last_point=new PointF();

    public boolean isDrawMode;

    public boolean isKeepPath;

    public boolean isInCtrlMode;

    public void setInCtrlMode(boolean inCtrlMode) {
        if(inCtrlMode==false){
            last_point=new PointF();
        }
        isInCtrlMode = inCtrlMode;
    }
    private boolean checkPointerVaild(PointF pointf){
        return pointf.x!=0.0f||pointf.y!=0.0f; //考虑到上层可能有导致0的情况
    }

    public void Payload(ClassLoader classLoader) throws Throwable {
        //加载输入
        Class<?>inputSC=XposedHelpers.findClass("com.android.server.input.InputShellCommand",classLoader);
        Object InputShellCommand = inputSC.newInstance();
        XposedHelpers.findAndHookMethod("com.miui.server.input.laser.LaserView", classLoader, "setPosition", android.graphics.PointF.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                PointF pos=(PointF) param.args[0];
                //清除轨迹，防止轨迹生成，因为后面的代码不能阻止运行，要提前清理轨迹
                List<Path> emptyList = new ArrayList<>();
                XposedHelpers.setObjectField(param.thisObject,"mPathList" ,emptyList);
                last_point=pos;
                if(!isKeepPath||!isDrawMode){
                    return; //激光模式 或者为笔迹模式但不记录笔迹的模式
                }
                if(DownPres){
                    //落笔，需要调用down
                    //(up->1 down->0 move->2 )sendMotionEvent (inputsource 4098 ,action 0/2,x,y,0)
                    XposedHelpers.callMethod(InputShellCommand,"sendMotionEvent",4098,0,pos.x,pos.y,0);
                    DownPres=false; //已经按下，接下来的操作是滑动操作
                }else{
                    //need to call move
                    //滑动
                    XposedHelpers.callMethod(InputShellCommand,"sendMotionEvent",4098,2,pos.x,pos.y,0);
                }
            }
        });
        XposedHelpers.findAndHookMethod("com.miui.server.input.laser.LaserView", classLoader, "setKeepPath", boolean.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                isKeepPath=(boolean)param.args[0]; //是否记录轨迹，如果为记录轨迹的操作即为输入操作
            }
        });
        //截取长按操作 onLaserKeyLongPressed
        XposedHelpers.findAndHookMethod("com.miui.server.input.laser.LaserView", classLoader, "setMode", int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                isDrawMode=((int)param.args[0]==1); //mode==1 设置模式：激光模式为0，画笔模式为1(画笔模式改为输入控制模式)
            }
        });
        XposedHelpers.findAndHookMethod("com.miui.server.input.laser.LaserPointerController", classLoader, "onLaserKeyPressed",int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                int count =(int)param.args[0];
                logutil.log("Short click Detected! count ="+count);
                if(count==2){
                    if(isInCtrlMode){
                        //两次短按，退出控制模式，清除掉笔的画面
                        setInCtrlMode(false);
                        XposedHelpers.callMethod(param.thisObject,"fadeLocked",0);
                    }
                }
                //一次短按，判断最后一个点的合法性发送单击事件
                if(isDrawMode&&checkPointerVaild(last_point)){
                    PointF lst=last_point;
                    XposedHelpers.callMethod(InputShellCommand,"sendTap",4098,lst.x,lst.y,0);
                    //inject run
                    param.setResult(null);//阻止系统函数处理单击事件
                }
            }
        });
        XposedHelpers.findAndHookMethod("com.miui.server.input.laser.LaserPointerController", classLoader, "interceptLaserKeyUp", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                logutil.log("laser key up from keyevent"); //已经抬笔
                //判断是否可见
                Object state=XposedHelpers.getObjectField(param.thisObject,"mLaserState");
                boolean isVisible =XposedHelpers.getBooleanField(state,"mVisible");
                if(!isVisible){
                    setInCtrlMode(false); //不可见时要清空状态
                }
                if(!isInCtrlMode&&checkPointerVaild(last_point)){
                    setInCtrlMode(true);
                }else if(isInCtrlMode && checkPointerVaild(last_point)){
                    XposedHelpers.callMethod(InputShellCommand,"sendMotionEvent",4098,1,last_point.x,last_point.y,0); //根据最后一个位置发送抬笔事件
                }
                DownPres=true; //抬笔,下次点击时要先落笔
            }
        });
    }
}
