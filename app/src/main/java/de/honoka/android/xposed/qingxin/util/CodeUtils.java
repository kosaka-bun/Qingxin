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

	/**
	 * 将多行字符串转为单行，并将字符串限制在限制字符数内
	 */
	public static String singleLine(String str, Integer limit) {
		String singleLine = str.replace("\r", "")
				.replace("\n", " ");
		if(limit != null) {
			if(singleLine.length() > limit) {
				singleLine = singleLine.substring(0, limit) + "...";
			}
		}
		return singleLine;
	}

	public static String singleLine(String str) {
		return singleLine(str, null);
	}
}
