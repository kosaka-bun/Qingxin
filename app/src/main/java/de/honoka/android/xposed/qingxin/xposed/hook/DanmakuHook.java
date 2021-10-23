package de.honoka.android.xposed.qingxin.xposed.hook;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.honoka.android.xposed.qingxin.util.Logger;
import de.honoka.android.xposed.qingxin.util.TextUtils;
import de.honoka.android.xposed.qingxin.xposed.XposedMain;
import de.robv.android.xposed.XposedHelpers;

/**
 * 弹幕拦截逻辑
 */
@SuppressWarnings("unchecked")
public class DanmakuHook extends LateInitHook {

	/**
	 * 日志中单条弹幕的最大长度
	 */
	private static final int BLOCK_LOG_DANMAKU_MAX_LENGTH = 30;

	/**
	 * 最多一次在日志中显示多少条被拦截弹幕
	 */
	private static final int MAX_LOG_DANMAKU_COUNT = 10;

	@Override
	public void after(MethodHookParam param) {
		//拿到列表，并转换为可修改列表
		List<Object> danmakuElemList = (List<Object>) param.getResult();
		danmakuElemList = new ArrayList<>(danmakuElemList);
		//弹幕拦截时不显示气泡，并且不会拦截一条就输出一条日志
		//将会将一个List中的所有被拦截的弹幕用一条日志输出出去
		List<String> blockList = new ArrayList<>();
		for(Iterator<Object> iterator = danmakuElemList.iterator();
		    iterator.hasNext(); ) {
			Object danmaku = iterator.next();
			String content = XposedHelpers.callMethod(danmaku,
					"getContent").toString();
			if(XposedMain.blockRuleCache.isBlockDanmakuContent(content)) {
				iterator.remove();
				//日志报告的弹幕内容限制字符数
				blockList.add(TextUtils.singleLine(content,
						BLOCK_LOG_DANMAKU_MAX_LENGTH));
				continue;
			}
		}
		//日志和修改返回值
		if(blockList.size() > 0) {
			StringBuilder log = new StringBuilder("拦截了" +
					blockList.size() + "条弹幕：");
			if(blockList.size() > MAX_LOG_DANMAKU_COUNT) {
				for(int i = 0; i < MAX_LOG_DANMAKU_COUNT; i++) {
					log.append("\n").append(blockList.get(i));
				}
				log.append("\n……\n").append("【省略了")
						.append(blockList.size() - MAX_LOG_DANMAKU_COUNT)
						.append("条弹幕】\n\n\n");
			} else {
				for(String s : blockList) {
					log.append("\n").append(s);
				}
				log.append("\n\n\n");
			}
			Logger.blockLog(log.toString());
		}
		param.setResult(danmakuElemList);
	}
}
