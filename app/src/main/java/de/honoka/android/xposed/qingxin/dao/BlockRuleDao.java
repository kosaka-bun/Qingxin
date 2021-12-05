package de.honoka.android.xposed.qingxin.dao;

import android.content.Context;

import com.j256.ormlite.dao.Dao;

import java.util.List;

import de.honoka.android.xposed.qingxin.common.DatabaseHelper;
import de.honoka.android.xposed.qingxin.entity.BlockRule;
import lombok.SneakyThrows;

public class BlockRuleDao {

    private final Dao<BlockRule, String> dao;

    @SneakyThrows
    public BlockRuleDao(Context context) {
        dao = new DatabaseHelper(context).getDao(BlockRule.class);
    }

    public Dao<BlockRule, String> getDao() {
        return dao;
    }

    /**
     * 按规则内容模糊查找
     */
    @SneakyThrows
    public List<BlockRule> findByContentLike(String searchWord) {
        String like = "%" + searchWord + "%";
        return dao.queryBuilder().where().like("content", like)
                .query();
    }

    /**
     * 查找某个作用域的所有规则
     */
    @SneakyThrows
    public List<BlockRule> getListOfRegion(String region) {
        return dao.queryForEq(region, true);
    }

    @SneakyThrows
    public List<BlockRule> findAll() {
        return dao.queryForAll();
    }
}
