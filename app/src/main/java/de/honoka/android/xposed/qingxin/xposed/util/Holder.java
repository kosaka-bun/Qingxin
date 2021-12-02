package de.honoka.android.xposed.qingxin.xposed.util;

/**
 * 用于保存Unhook对象，便于在Hook到后取消Hook
 */
public class Holder<T> {

    public T obj;
}