package de.honoka.android.xposed.qingxin.xposed.hook;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.honoka.android.xposed.qingxin.entity.BlockRule;
import de.honoka.android.xposed.qingxin.util.Logger;
import de.honoka.android.xposed.qingxin.util.PatternUtils;
import de.honoka.android.xposed.qingxin.xposed.model.BlockRuleCache;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import lombok.SneakyThrows;

/**
 * 评论拦截逻辑
 */
@SuppressWarnings("unchecked")
public class CommentHook extends XC_MethodHook {

	private final BlockRuleCache blockRuleCache;

	public CommentHook(BlockRuleCache blockRuleCache) {
		this.blockRuleCache = blockRuleCache;
	}

	@SneakyThrows
	@Override
	protected void afterHookedMethod(MethodHookParam param) {
		//拿到评论区数据列表
		List<Object> replies = (List<Object>) param.getResult();
		//转换为可修改列表
		replies = new ArrayList<>(replies);
		//拦截评论数
		int blockCount = 0;
		//遍历每一个评论
		repliesLoop:
		for(Iterator<Object> iterator = replies.iterator();
		    iterator.hasNext(); ) {
			Object reply = iterator.next();
			Object content = XposedHelpers.callMethod(reply,
					"getContent");
			//region 按评论内容过滤
			String message = XposedHelpers.callMethod(content,
					"getMessage").toString();
			for(BlockRule blockRule : blockRuleCache.getCommentList()) {
				switch(blockRule.getType()) {
					//按关键词过滤
					case BlockRule.RuleType.KEYWORD: {
						//在不区分大小写的情况下，消息是否包含这条规则的内容（关键词）
						if(StringUtils.containsIgnoreCase(message,
								blockRule.getContent())) {
							//包含则移除这条评论，然后判断下一条评论
							iterator.remove();
							blockCount++;
							Logger.blockLog(String.format(
									"关键词【%s】拦截：%s",
									blockRule.getContent(), message));
							continue repliesLoop;
						}
						break;
					}
					//正则过滤
					case BlockRule.RuleType.PATTERN: {
						if(PatternUtils.containsMatch(message,
								blockRule.getContent())) {
							iterator.remove();
							blockCount++;
							Logger.blockLog(String.format(
									"正则表达式【%s】拦截：%s",
									blockRule.getContent(), message));
							continue repliesLoop;
						}
						break;
					}
				}
			}
			//endregion
		}
		//气泡提示
		if(blockCount > 0)
			Logger.toastOnBlock("拦截了" + blockCount + "条评论");
		//修改hook方法的返回值，使调用方拿到拦截后的列表
		param.setResult(replies);
	}
}
