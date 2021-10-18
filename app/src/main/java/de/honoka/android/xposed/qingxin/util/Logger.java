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

	public static void testLog(String log) {
		if(XposedMain.mainPreference.getTestMode()) {
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
			toast(log, Toast.LENGTH_SHORT);
		}
	}

	/**
	 * 无条件toast
	 */
	public static void toast(String log, int length) {
		try {
			Toast.makeText(XposedMain.hookApplication, log,
					length).show();
		} catch(RuntimeException re) {
			String msg = re.getMessage();
			//如果是当前线程不能够Toast
			if(msg != null && msg.contains("Can't toast")) {
				toastOnNewThread(log);
			} else {
				throw re;
			}
		}
	}

	public static synchronized void writeToFile(String log) {
		log += "\n\n\n";
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
		class ToastThread extends Thread {

			public Looper looper;

			@Override
			public void run() {
				Looper.prepare();
				Toast.makeText(XposedMain.hookApplication, log,
						Toast.LENGTH_SHORT).show();
				looper = Looper.myLooper();
				Looper.loop();
			}
		}
		ToastThread toastThread = new ToastThread();
		//监听toast线程的线程
		new Thread(() -> {
			try {
				toastThread.start();
				toastThread.join(100);
				if(toastThread.isAlive()) {
					toastThread.looper.quit();
					toastThread.interrupt();
				}
			} catch(Throwable t) {
				//ignore
			}
		}).start();
	}
}
