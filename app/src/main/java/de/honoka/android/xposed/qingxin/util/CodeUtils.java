package de.honoka.android.xposed.qingxin.util;

public class CodeUtils {

	public static void doIgnoreException(Runnable action) {
		try {
			action.run();
		} catch(Throwable t) {
			//ignore
		}
	}
}
