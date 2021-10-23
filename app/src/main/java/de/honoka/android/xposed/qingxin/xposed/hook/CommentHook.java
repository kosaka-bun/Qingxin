package de.honoka.android.xposed.qingxin.xposed.hook;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.honoka.android.xposed.qingxin.util.Logger;
import de.honoka.android.xposed.qingxin.util.TextUtils;
import de.robv.android.xposed.XposedHelpers;
import lombok.SneakyThrows;

import static de.honoka.android.xposed.qingxin.xposed.XposedMain.blockRuleCache;

/**
 * 评论拦截逻辑
 */
@SuppressWarnings("unchecked")
public class CommentHook extends LateInitHook {

	/**
	 * 显示拦截日志时，最多只显示被拦截的评论的多少个字符
	 */
	public static final int BLOCK_LOG_LENGTH_LIMIT = 50;

	@SneakyThrows
	@Override
	public void after(MethodHookParam param) {
		//verbose test log
		//Logger.testLog("Hook到方法：" + param.method.toString());
		//方法返回值可能是List或ReplyInfo
		if(((Method) param.method).getReturnType().equals(List.class)) {
			//拿到评论区数据列表
			List<Object> replies = (List<Object>) param.getResult();
			//转换为可修改列表
			replies = new ArrayList<>(replies);
			//拦截评论数
			int blockCount = 0;
			//遍历每一个评论
			for(Iterator<Object> iterator = replies.iterator();
			    iterator.hasNext(); ) {
				//replyInfo
				Object reply = iterator.next();
				//判断是否应当拦截
				if(checkReplyInfo(reply)) {
					//移除这条评论
					iterator.remove();
					blockCount++;
					//判断下一条评论
					continue;
				}
			}
			//气泡提示
			if(blockCount > 0)
				Logger.toastOnBlock("拦截了" + blockCount + "条评论");
			//修改hook方法的返回值，使调用方拿到拦截后的列表
			param.setResult(replies);
			return;
		} else {
			//返回值是ReplyInfo
			Object reply = param.getResult();
			//判断是否应当拦截
			if(checkReplyInfo(reply)) {
				Logger.toastOnBlock("拦截了1条评论");
				//修改返回值
				param.setResult(XposedHelpers.callStaticMethod(reply.getClass(),
						"getDefaultInstance"));
				return;
			}
		}
	}

	/**
	 * 判断replyInfo是否应当被拦截（对blockCache中方法的再封装，添加日志功能）
	 */
	private boolean checkReplyInfo(Object reply) {
		//根据内容判断是否应当拦截
		Object content = XposedHelpers.callMethod(reply,
				"getContent");
		String message = XposedHelpers.callMethod(content,
				"getMessage").toString();
		if(blockRuleCache.isBlockCommentMessage(message)) {
			Logger.blockLog("评论拦截：" + TextUtils.singleLine(
					message, BLOCK_LOG_LENGTH_LIMIT));
			return true;
		}
		//根据用户名判断是否应当拦截
		Object member = XposedHelpers.callMethod(reply,
				"getMember");
		String username = XposedHelpers.callMethod(member,
				"getName").toString();
		if(blockRuleCache.isBlockUsername(username)) {
			Logger.blockLog("评论拦截（按用户名）：" +
					TextUtils.singleLine(message, BLOCK_LOG_LENGTH_LIMIT) +
					"\n用户名：" + username);
			return true;
		}
		return false;
	}
}