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
}
