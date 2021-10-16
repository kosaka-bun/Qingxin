package de.honoka.android.xposed.qingxin.util;

import android.widget.Toast;

import de.honoka.android.xposed.qingxin.xposed.XposedMain;
import de.robv.android.xposed.XposedBridge;

public class Logger {

	private static final boolean TEST_MODE = true;

	public static void blockLog(String log) {
		Boolean b = XposedMain.mainPreference.getShowBlockLog();
		if(b != null && b.equals(true)) {
			XposedBridge.log("\n" + log);
		}
	}

	public static void toastOnBlock(String log) {
		if(XposedMain.hookApplication == null) return;
		Boolean b = XposedMain.mainPreference.getToastOnBlock();
		//判断开关
		if(b != null && b.equals(true)) {
			Toast.makeText(XposedMain.hookApplication, log,
					Toast.LENGTH_SHORT).show();
		}
	}

	public static void testLog(String log) {
		if(TEST_MODE) {
			XposedBridge.log("\n" + log);
		}
	}
}
