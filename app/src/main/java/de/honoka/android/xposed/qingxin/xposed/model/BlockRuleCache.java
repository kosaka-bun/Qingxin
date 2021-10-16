package de.honoka.android.xposed.qingxin.xposed.model;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import de.honoka.android.xposed.qingxin.entity.BlockRule;
import de.honoka.android.xposed.qingxin.util.PatternUtils;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class BlockRuleCache {

	private List<BlockRule> videoTitleList;

	private List<BlockRule> videoSubAreaList;

	private List<BlockRule> videoChannelList;

	private List<BlockRule> usernameList;

	private List<BlockRule> commentList;

	private List<BlockRule> danmakuList;

	private List<BlockRule> hotSearchWordList;

	private List<BlockRule> dongtaiList;

	/**
	 * 根据评论的内容，和评论作用域规则列表，判断评论是否应当拦截
	 * @param message 评论的文本内容
	 */
	public boolean isBlockCommentMessage(String message) {
		//根据评论作用域里的规则来判断
		for(BlockRule blockRule : getCommentList()) {
			switch(blockRule.getType()) {
				//按关键词过滤
				case BlockRule.RuleType.KEYWORD: {
					//在不区分大小写的情况下，消息是否包含这条规则的内容（关键词）
					if(StringUtils.containsIgnoreCase(message,
							blockRule.getContent())) {
						//包含则该评论应该被拦截
						return true;
					}
					break;
				}
				//正则过滤
				case BlockRule.RuleType.PATTERN: {
					if(PatternUtils.containsMatch(message,
							blockRule.getContent())) {
						return true;
					}
					break;
				}
			}
		}
		return false;
	}
}
