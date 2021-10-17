package de.honoka.android.xposed.qingxin.util;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

import de.honoka.android.xposed.qingxin.xposed.XposedMain;
import de.robv.android.xposed.XposedBridge;

public class Logger {

	public static void blockLog(String log) {
		Boolean b = XposedMain.mainPreference.getShowBlockLog();
		if(b != null && b.equals(true)) {
			XposedBridge.log("\n" + log);
		}
	}

	/**
	 * toast容易出现异常
	 */
	public static void toastOnBlock(String log) {
		if(XposedMain.hookApplication == null) return;
		Boolean b = XposedMain.mainPreference.getToastOnBlock();
		//判断开关
		if(b != null && b.equals(true)) {
			try {
				Toast.makeText(XposedMain.hookApplication, log,
						Toast.LENGTH_SHORT).show();
			} catch(RuntimeException re) {
				String msg = re.getMessage();
				if(msg != null && msg.contains("Can't toast")) {
					toastOnNewThread(log);
				} else {
					throw re;
				}
			}
		}
	}

	public static void testLog(String log) {
		if(XposedMain.mainPreference.getTestMode()) {
			XposedBridge.log("\n" + log);
			//writeToFile(log + "\n\n\n");
		}
	}

	public static synchronized void writeToFile(String log) {
		try(FileOutputStream fileOutputStream = XposedMain.hookApplication
				.openFileOutput("qingxin_log.txt", Context.MODE_APPEND)) {
			fileOutputStream.write(log.getBytes(StandardCharsets.UTF_8));
		} catch(Throwable t) {
			//ignore
		}
	}

	/**
	 * 在新的线程中调用Looper.prepare来进行toast，不阻塞主线程
	 */
	public static void toastOnNewThread(String log) {
		//toast线程（可能会阻塞）
		Thread toastThread = new Thread(() -> {
			Looper.prepare();
			Toast.makeText(XposedMain.hookApplication, log,
					Toast.LENGTH_SHORT).show();
			Looper.loop();
		});
		//监听toast线程的线程
		new Thread(() -> {
			try {
				toastThread.start();
				toastThread.join(100);
				if(toastThread.isAlive()) toastThread.interrupt();
			} catch(Throwable t) {
				//ignore
			}
		}).start();
	}
}
