package de.honoka.android.xposed.qingxin.common;

public interface Constant {

	/**
	 * 更新广播的action
	 */
	String UPDATE_BROADCAST_ACTION =
			"de.honoka.android.xposed.qingxin.UPDATE_BROADCAST";

	interface UpdateType {

		String MAIN_PREFERENCE = "main_preference";

		String BLOCK_RULE = "block_rule";
	}

	interface ErrorMessage {

		String INIT_NO_AUTO_BOOT_PERMISSION =
				"清心模块加载失败，请检查清心模块的自启动权限是否已开启";

		String INIT_UNKNOWN_ERROR =
				"清心模块加载失败，请到bilibili数据目录下查看日志文件";
	}
}
