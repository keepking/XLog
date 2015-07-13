package com.github.promeg.xlog_android.lib;


import com.promegu.xlog.base.MethodToLog;
import com.taobao.android.dexposed.XC_MethodHook;

import android.os.Looper;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;


/**
 * Created by guyacong on 2015/3/10.
 */
public class XLogMethodHook extends XC_MethodHook {
    private long startTime;
    private Member mMember;
    private MethodToLog mMethodToLog;

    public XLogMethodHook(Member member, MethodToLog methodToLog) {
        this.mMember = member;
        this.mMethodToLog = methodToLog;
    }

    @Override
    protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
        // this will be called before the clock was updated by the original method

        Class<?>[] parameterTypes = null;
        if(mMember instanceof Method) {
            parameterTypes = ((Method) mMember).getParameterTypes();
        } else{
            parameterTypes = ((Constructor) mMember).getParameterTypes();
        }

        StringBuilder builder = new StringBuilder("\u21E2 ");
        builder.append(param.method.getName()).append('(');
        for (int i = 0; i < param.args.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            if(mMethodToLog != null && mMethodToLog.getParameterNames().size() > i){
                builder.append(mMethodToLog.getParameterNames().get(i)).append('=');
            } else {
                builder.append(parameterTypes[i].getSimpleName()).append('=');
            }
            builder.append(Strings.toString(param.args[i]));
        }
        builder.append(')');
        if (Looper.myLooper() != Looper.getMainLooper()) {
            builder.append(" [Thread:\"").append(Thread.currentThread().getName()).append("\"]");
        }
        Log.d(asTag(mMember.getDeclaringClass()), builder.toString());
        startTime = System.currentTimeMillis();
    }

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        // this will be called after the clock was updated by the original method
        long lengthMillis = System.currentTimeMillis() - startTime;
        StringBuilder builder = new StringBuilder("\u21E0 ")
                .append(param.method.getName())
                .append(" [")
                .append(lengthMillis)
                .append("ms]");

        if (param.getResult() != null) {
            builder.append(" = ");
            builder.append(Strings.toString(param.getResult()));
        }
        Log.d(asTag(mMember.getDeclaringClass()), builder.toString());
    }

    /**
     * from https://github.com/JakeWharton/hugo
     */
    private static String asTag(Class<?> cls) {
        if (cls.isAnonymousClass()) {
            return asTag(cls.getEnclosingClass());
        }
        return cls.getSimpleName();
    }

}
