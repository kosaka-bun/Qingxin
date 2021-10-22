package de.honoka.android.xposed.qingxin.common

object Constant {

	/**
	 * 更新广播的action
	 */
	const val UPDATE_BROADCAST_ACTION =
			"de.honoka.android.xposed.qingxin.UPDATE_BROADCAST"

	object UpdateType {

		const val MAIN_PREFERENCE = "main_preference"

		const val BLOCK_RULE = "block_rule"
	}

	object ErrorMessage {

		const val INIT_NO_AUTO_BOOT_PERMISSION =
				"清心模块加载失败，请检查清心模块的自启动权限是否已开启"

		const val INIT_UNKNOWN_ERROR =
				"清心模块加载失败，请到bilibili数据目录下查看日志文件"
	}
}