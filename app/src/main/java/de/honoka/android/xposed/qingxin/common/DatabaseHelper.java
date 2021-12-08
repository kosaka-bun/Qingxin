package de.honoka.android.xposed.qingxin.common;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import de.honoka.android.xposed.qingxin.entity.BlockRule;
import lombok.SneakyThrows;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    public static final String DATABASE_NAME = "main.db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    private static final Class<?>[] entityClasses = {
            BlockRule.class
    };

    @SneakyThrows
    @Override
    public void onCreate(SQLiteDatabase database,
                         ConnectionSource connectionSource) {
        for(Class<?> entityClass : entityClasses) {
            TableUtils.createTable(connectionSource, entityClass);
        }
    }

    @SneakyThrows
    @Override
    public void onUpgrade(SQLiteDatabase database,
                          ConnectionSource connectionSource,
                          int oldVersion, int newVersion) {
        for(Class<?> entityClass : entityClasses) {
            TableUtils.dropTable(connectionSource, entityClass, true);
        }
        onCreate(database, connectionSource);
    }
}
