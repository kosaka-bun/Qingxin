package de.honoka.android.xposed.qingxin.xposed.init;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 与initAllHook方法结合使用，initAllHook方法会执行本类中所有附带了此注解的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InitializerMethod {

}
