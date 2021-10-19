package de.honoka.android.xposed.qingxin.util;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import de.honoka.android.xposed.qingxin.xposed.XposedMain;
import de.robv.android.xposed.XposedBridge;

public class Logger {

	/**
	 * 专用于处理Toast的线程
	 */
	private static final Thread toastThread = new Thread(() -> {
		Looper.prepare();
		toastHandler = new Handler(Looper.myLooper());
		Looper.loop();
	});

	static {
		toastThread.start();
	}

	/**
	 * Toast线程中用于接收消息的Handler
	 */
	private static Handler toastHandler;

	private static final String LOG_FILE_NAME = "qingxin_log.log";

	public static void blockLog(String log) {
		Boolean b = XposedMain.mainPreference.getShowBlockLog();
		if(b != null && b.equals(true)) {
			XposedBridge.log("\n" + log);
		}
	}

	/**
	 * 仅在调试模式下输出日志
	 */
	public static void testLog(String log) {
		if(XposedMain.mainPreference.getTestMode()) {
			testLogForce(log);
		}
	}

	/**
	 * 调试日志，同时输出到框架控制台和文件
	 */
	public static void testLogForce(String log) {
		XposedBridge.log("\n" + log);
		writeToFile(log);
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
			if(msg != null) {
				if(msg.contains("Can't toast") ||
				   msg.contains("Can't create handler")) {
					toastOnToastThread(log, length);
				} else {
					throw re;
				}
			} else {
				throw re;
			}
		}
	}

	/**
	 * 在专有线程中进行toast，不阻塞主线程
	 */
	private static void toastOnToastThread(String log, int length) {
		if(toastHandler == null) return;
		toastHandler.post(() -> {
			Toast.makeText(XposedMain.hookApplication, log, length).show();
		});
	}

	/**
	 * 写出日志到文件
	 */
	private static synchronized void writeToFile(String log) {
		log = CodeUtils.getSimpleDateFormat().format(new Date()) + "\n" +
				log + "\n\n\n";
		File logFile = new File(XposedMain.hookApplication
				.getCacheDir().getPath() + File.separator + LOG_FILE_NAME);
		FileUtils.checkFiles(logFile);
		try(FileOutputStream fileOutputStream = new FileOutputStream(
				logFile, true)) {
			fileOutputStream.write(log.getBytes(StandardCharsets.UTF_8));
		} catch(Throwable t) {
			//ignore
		}
	}
}
