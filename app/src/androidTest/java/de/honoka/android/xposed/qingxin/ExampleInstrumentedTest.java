package de.honoka.android.xposed.qingxin;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import de.honoka.android.xposed.qingxin.common.Singletons;
import de.honoka.android.xposed.qingxin.dao.BlockRuleDao;
import de.honoka.android.xposed.qingxin.entity.BlockRule;
import de.honoka.android.xposed.qingxin.service.MainPreferenceService;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    private Context appContext;

    @Test
    public void useAppContext() throws Throwable {
        // Context of the app under test.
        appContext = InstrumentationRegistry.getInstrumentation()
                .getTargetContext();
        assertEquals("de.honoka.android.xposed.qingxin",
                appContext.getPackageName());

        //test1();
        //test2();
        test3();
    }

    private void test1() throws Throwable {
        BlockRuleDao blockRuleDao = new BlockRuleDao(appContext);
        blockRuleDao.getDao().create(new BlockRule().setContent("test"));
        Log.i("TAG", blockRuleDao.getDao().queryForAll().toString());
    }

    private void test2() throws Throwable {
        BlockRuleDao blockRuleDao = new BlockRuleDao(appContext);
        Log.i("TAG", Singletons.gson.toJson(blockRuleDao.getDao().queryForAll()));
    }

    private void test3() {
        Log.i("TAG", new MainPreferenceService(appContext)
                .getPreference().toString());
    }
}
