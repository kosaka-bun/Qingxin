package de.honoka.android.xposed.qingxin.util;

import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

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
}
