package de.honoka.android.xposed.qingxin.util;

import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import de.honoka.android.xposed.qingxin.xposed.webview.handler.ColumnHandler;
import lombok.SneakyThrows;

@SuppressLint("SimpleDateFormat")
public class CodeUtils {

    public interface ThrowsRunnable extends Runnable {

        void throwsRun() throws Throwable;

        @SneakyThrows
        @Override
        default void run() {
            throwsRun();
        }
    }

    public static void doIgnoreException(ThrowsRunnable action) {
        try {
            action.run();
        } catch(Throwable t) {
            //ignore
        }
    }

    public static DateFormat getSimpleDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 获得首字母小写的类名
     */
    public static String getCamelCaseName(Class<?> clazz) {
        StringBuilder builder = new StringBuilder(ColumnHandler.class
                .getSimpleName());
        builder.setCharAt(0, Character.toLowerCase(
                builder.charAt(0)));
        return builder.toString();
    }

    public static boolean isAllFalse(boolean... values) {
        for(boolean value : values) {
            if(value) return false;
        }
        return true;
    }
}
