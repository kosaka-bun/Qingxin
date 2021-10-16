package de.honoka.android.xposed.qingxin.xposed.model;

import java.util.List;

import de.honoka.android.xposed.qingxin.entity.BlockRule;
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
}
