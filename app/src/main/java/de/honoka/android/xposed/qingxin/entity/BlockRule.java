package de.honoka.android.xposed.qingxin.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 屏蔽规则
 */
@DatabaseTable(tableName = "block_rule")
@Data
@Accessors(chain = true)
public class BlockRule {

    /**
     * UUID
     */
    @DatabaseField(id = true, unique = true)
    private String id;

    /**
     * 规则类型（关键词，正则表达式）
     */
    @DatabaseField
    private String type;

    /**
     * 规则内容
     */
    @DatabaseField
    private String content;

    //region 作用域

    /**
     * 视频标题
     */
    @DatabaseField
    private Boolean videoTitle;

    /**
     * 视频分区
     */
    @DatabaseField
    private Boolean videoSubArea;

    /**
     * 视频频道
     */
    @DatabaseField
    private Boolean videoChannel;

    /**
     * 用户名
     */
    @DatabaseField
    private Boolean username;

    /**
     * 评论
     */
    @DatabaseField
    private Boolean comment;

    /**
     * 弹幕
     */
    @DatabaseField
    private Boolean danmaku;

    /**
     * 热搜
     */
    @DatabaseField
    private Boolean hotSearchWord;

    /**
     * 动态（不知道b站动态这种东西英文该叫啥）
     */
    @DatabaseField
    private Boolean dongtai;

    //endregion

    public interface RuleType {

        String KEYWORD = "keyword", PATTERN = "pattern";
    }

    public interface Region {

        String VIDEO_TITLE = "videoTitle";

        String VIDEO_SUB_AREA = "videoSubArea";

        String VIDEO_CHANNEL = "videoChannel";

        String USERNAME = "username";

        String COMMENT = "comment";

        String DANMAKU = "danmaku";

        String HOT_SEARCH_WORD = "hotSearchWord";

        String DONGTAI = "dongtai";
    }
}
