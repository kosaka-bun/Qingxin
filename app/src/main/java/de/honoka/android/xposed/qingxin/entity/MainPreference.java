package de.honoka.android.xposed.qingxin.entity;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MainPreference {

	/**
	 * 屏蔽所有首页推广
	 */
	private Boolean blockAllMainPagePublicity;

	/**
	 * 是否在框架控制台输出拦截日志
	 */
	private Boolean showBlockLog;

	/**
	 * 是否在拦截时发出气泡信息，以显示拦截条数等信息
	 */
	private Boolean toastOnBlock;

	public static MainPreference getDefaultPreference() {
		return new MainPreference()
				.setBlockAllMainPagePublicity(false)
				.setShowBlockLog(false)
				.setToastOnBlock(false);
	}
}
