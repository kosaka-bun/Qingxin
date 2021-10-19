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
	 * 屏蔽所有热搜
	 */
	private Boolean blockAllHotSearchWords;

	/**
	 * 还原所有竖屏视频
	 */
	private Boolean convertAllVerticalAv;

	/**
	 * 是否在框架控制台输出拦截日志
	 */
	private Boolean showBlockLog;

	/**
	 * 是否在拦截时发出气泡信息，以显示拦截条数等信息
	 */
	private Boolean toastOnBlock;

	/**
	 * 调试模式（会在控制台输出很多调试信息）
	 */
	private Boolean testMode;

	public static MainPreference getDefaultPreference() {
		return new MainPreference()
				.setBlockAllMainPagePublicity(false)
				.setBlockAllHotSearchWords(false)
				.setConvertAllVerticalAv(false)
				.setShowBlockLog(false)
				.setToastOnBlock(false)
				.setTestMode(true);
	}
}
