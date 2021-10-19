package de.honoka.android.xposed.qingxin.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

import lombok.SneakyThrows;

public class FileUtils {

	@SneakyThrows
	public static String streamToString(FileInputStream is) {
		byte[] bytes = new byte[is.available()];
		is.read(bytes);
		return new String(bytes, StandardCharsets.UTF_8);
	}
	
	/**
	 * 获取当前运行环境的classpath
	 */
	public static String getClasspath() {
		try {
			return new File(Thread.currentThread().getContextClassLoader()
					.getResource("").toURI()).getAbsolutePath();
		} catch(Exception e) {
			return new File("").getAbsolutePath();
		}
	}
	
	@SneakyThrows
	public static void checkDirs(File... dirs) {
		for(File dir : dirs) {
			if(!dir.exists()) dir.mkdirs();
		}
	}
	
	/**
	 * 检查必要的文件是否存在，不存在则创建
	 */
	@SneakyThrows
	public static void checkFiles(File... files) {
		for(File f : files) {
			if(!f.exists()) {    //文件不存在
				//检查文件所在的的目录是否存在，不存在先创建多级目录
				File path = f.getParentFile();
				if(!path.exists()) path.mkdirs();
				f.createNewFile();
			}
		}
	}

	/**
	 * 当SD卡存在或者SD卡不可被移除的时候，就调用getExternalCacheDir()方法来获取缓存路径，
	 * 否则就调用getCacheDir()方法来获取缓存路径。
	 * getExternalCacheDir()获取到的是 /sdcard/Android/data/<application package>/cache这个路径，
	 * getCacheDir()获取到的是 /data/data/<application package>/cache 这个路径。
	 */
	public static String getDiskCacheDir(Context context) {
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
			!Environment.isExternalStorageRemovable()) {
			return context.getExternalCacheDir().getPath();
		} else {
			return context.getCacheDir().getPath();
		}
	}
}
