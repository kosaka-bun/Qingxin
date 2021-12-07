package de.honoka.android.xposed.qingxin.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.google.gson.reflect.TypeToken;
import com.j256.ormlite.dao.Dao;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.honoka.android.xposed.qingxin.R;
import de.honoka.android.xposed.qingxin.activity.AddRuleActivity;
import de.honoka.android.xposed.qingxin.activity.MainActivity;
import de.honoka.android.xposed.qingxin.common.Constant;
import de.honoka.android.xposed.qingxin.common.Singletons;
import de.honoka.android.xposed.qingxin.dao.BlockRuleDao;
import de.honoka.android.xposed.qingxin.entity.BlockRule;
import de.honoka.android.xposed.qingxin.util.Base64Utils;
import de.honoka.android.xposed.qingxin.util.CodeUtils;
import de.honoka.android.xposed.qingxin.util.FilePermissionUtils;
import de.honoka.android.xposed.qingxin.util.NoFeedTextWatcher;
import lombok.SneakyThrows;

@SuppressLint("DefaultLocale")
@SuppressWarnings({"unchecked", "deprecation"})
public class MainPreferenceFragment extends PreferenceFragment {

    /**
     * 配置缓存
     */
    private Map<String, Object> props;

    private Preference.OnPreferenceChangeListener preferenceChangeListener;

    public static final String MAIN_PROP_FILENAME = "main_prop.json";

    private BlockRuleDao blockRuleDao;

    private Preference rulesCountTextPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //加载xml文件
        addPreferencesFromResource(R.xml.preference_main);
        //从文件中加载配置信息
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        props = (Map<String, Object>) preferenceScreen.getSharedPreferences()
                .getAll();
        //加载后先直接写出到文件
        writeOutProps(Singletons.gson.toJson(props));
        //为所有配置项注册配置修改监听器
        preferenceChangeListener = (preference, newValue) -> {
            //将修改的值更新到缓存
            props.put(preference.getKey(), newValue);
            //将缓存写出到json文件中
            String json = Singletons.gson.toJson(props);
            writeOutProps(json);
            //发送更新广播
            sendPreferenceUpdateBroadcast(json);
            //region 测试
            //Log.i("TAG", json);
            //endregion
            return true;
        };
        registerOnChangeListener(preferenceScreen);
        //显示规则数
        blockRuleDao = new BlockRuleDao(getContext());
        rulesCountTextPreference = preferenceScreen
                .findPreference("rules_count_text");
        refreshRulesCount();
        //添加规则项目跳转
        preferenceScreen.findPreference("add_rules")
                .setOnPreferenceClickListener(preference -> {
                    Intent intent = new Intent(getContext(), AddRuleActivity.class);
                    getContext().startActivity(intent);
                    return true;
                });
        //删除项目点击监听
        preferenceScreen.findPreference("remove_rules")
                .setOnPreferenceClickListener(removeRuleOptionListener);
        //导入规则项目点击监听
        preferenceScreen.findPreference("import_rules")
                .setOnPreferenceClickListener(importRulesOptionListener);
        //导出规则项目点击监听
        preferenceScreen.findPreference("export_rules")
                .setOnPreferenceClickListener(exportRulesOptionListener);
    }

    /**
     * 递归为配置组中的每一个配置项注册监听器
     */
    private void registerOnChangeListener(PreferenceGroup preferenceGroup) {
        for(int i = 0; i < preferenceGroup.getPreferenceCount(); i++) {
            Preference preference = preferenceGroup.getPreference(i);
            //如果配置组中又包含一个配置组，就先为这个配置组中的每一项注册监听器
            if(preference instanceof PreferenceGroup) {
                registerOnChangeListener((PreferenceGroup) preference);
            } else {
                preference.setOnPreferenceChangeListener(preferenceChangeListener);
            }
        }
    }

    /**
     * 将配置写出到文件中
     */
    @SneakyThrows
    private synchronized void writeOutProps(String propJson) {
        try(FileOutputStream fileOutputStream = getContext().openFileOutput(
                MAIN_PROP_FILENAME, Context.MODE_PRIVATE)) {
            fileOutputStream.write(propJson.getBytes(StandardCharsets.UTF_8));
        }
    }

    @SneakyThrows
    private void refreshRulesCount() {
        rulesCountTextPreference.setTitle("当前规则数：" +
                blockRuleDao.getDao().countOf());
    }

    @Override
    public void onResume() {
        refreshRulesCount();
        super.onResume();
    }

    /**
     * 删除项目点击监听器
     */
    private final Preference.OnPreferenceClickListener
            removeRuleOptionListener = preference -> {
        Context context = getContext();
        //初始化一个编辑框
        final EditText editText = new EditText(context);
        editText.addTextChangedListener(new NoFeedTextWatcher(editText));
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(context)
                .setTitle("输入搜索关键词").setView(editText);
        //确定搜索
        inputDialog.setPositiveButton("确定", (dialog, which) -> {
            //根据输入的内容查找
            String searchWord = editText.getText().toString().trim();
            if(searchWord.equals("")) {
                Toast.makeText(context, "关键词不能为空",
                        Toast.LENGTH_LONG).show();
                return;
            }
            BlockRuleDao blockRuleDao = new BlockRuleDao(context);
            List<BlockRule> result = blockRuleDao.findByContentLike(searchWord);
            //若没有匹配规则
            if(result.size() <= 0) {
                Toast.makeText(context, "没有找到匹配的规则",
                        Toast.LENGTH_LONG).show();
                return;
            }
            //有匹配规则，新建另一个dialog询问是否删除
            AlertDialog.Builder confirmDialog = new AlertDialog.Builder(context)
                    .setCancelable(false)
                    .setTitle("删除规则").setMessage(String.format(
                            "共找到%d条匹配的记录，是否删除？", result.size()));
            //确定删除
            confirmDialog.setPositiveButton("确定", (dialog1, which1) -> {
                try {
                    blockRuleDao.getDao().delete(result);
                    Toast.makeText(context, "删除成功",
                            Toast.LENGTH_LONG).show();
                    //刷新条目数
                    refreshRulesCount();
                } catch(Throwable t) {
                    Toast.makeText(context, "删除失败",
                            Toast.LENGTH_LONG).show();
                }
            });
            confirmDialog.setNegativeButton("取消", (dialog1, which1) -> {});
            confirmDialog.show();
        });
        inputDialog.show();
        return true;
    };

    /**
     * 发送更新广播
     */
    private void sendPreferenceUpdateBroadcast(String json) {
        Intent intent = new Intent();
        intent.setAction(Constant.UPDATE_BROADCAST_ACTION);
        intent.putExtra("type", Constant.UpdateType.MAIN_PREFERENCE);
        intent.putExtra("data", json);
        getContext().sendBroadcast(intent);
    }

    /**
     * 导入规则项目点击监听器
     */
    private final Preference.OnPreferenceClickListener
            importRulesOptionListener = preference -> {
        MainActivity mainActivity = (MainActivity) getActivity();
        FilePermissionUtils filePermissionUtils = new FilePermissionUtils(
                mainActivity);
        //判断是否拥有权限
        if(filePermissionUtils.hasPermissions()) {
            openFilePickerForImport();
        } else {
            //添加权限处理完成后的回调
            Runnable callback = (CodeUtils.ThrowsRunnable) () -> {
                //移除回调
                mainActivity.onPermissionsResultCallBacks.remove(Constant
                        .RequestCode.IMPORT_RULES_PERMISSION);
                //判断是否已拥有权限
                if(!filePermissionUtils.hasPermissions()) {
                    Toast.makeText(mainActivity, "导入规则需要文件读取权限，" +
                            "请先为清心授予文件读取权限", Toast.LENGTH_LONG).show();
                    return;
                }
                openFilePickerForImport();
            };
            mainActivity.onPermissionsResultCallBacks.put(Constant.RequestCode
                    .IMPORT_RULES_PERMISSION, callback);
            //申请权限
            filePermissionUtils.requestPermissions(Constant.RequestCode
                    .IMPORT_RULES_PERMISSION);
        }
        return true;
    };

    /**
     * 导出规则项目点击监听器
     */
    private final Preference.OnPreferenceClickListener
            exportRulesOptionListener = preference -> {
        MainActivity mainActivity = (MainActivity) getActivity();
        List<BlockRule> ruleList = blockRuleDao.findAll();
        if(ruleList.size() <= 0) {
            Toast.makeText(mainActivity, "没有规则可导出",
                    Toast.LENGTH_SHORT).show();
            return true;
        }
        //确认导出
        AlertDialog.Builder confirmDialog = new AlertDialog.Builder(mainActivity)
                .setCancelable(false).setTitle("导出规则")
                .setMessage(String.format("共%d条规则，是否导出？",
                        ruleList.size()));
        confirmDialog.setPositiveButton("确定", (dialog1, which1) -> {
            String filePath = mainActivity.getExternalFilesDir(null)
                    .getAbsolutePath() + "/rules_export/";
            DateFormat dateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd_HH_mm_ss", Locale.CHINA);
            File file = new File(filePath + dateFormat.format(
                    new Date()) + ".json");
            exportRules(ruleList, file);
            AlertDialog.Builder successDialog = new AlertDialog.Builder(
                    mainActivity).setCancelable(false).setTitle("导出成功");
            successDialog.setMessage(ruleList.size() + "条规则已导出到" +
                    file.getAbsolutePath() + "\n由于文件当前所在的位置会在" +
                    "清心被卸载后被删除，建议将此文件移动到其他地方保存");
            successDialog.setPositiveButton("确定", (dialog2, which2) -> {});
            successDialog.show();
        });
        confirmDialog.setNegativeButton("取消", (dialog1, which1) -> {});
        confirmDialog.show();
        return true;
    };

    /**
     * 打开文件选择器，选择文件导入规则
     */
    public void openFilePickerForImport() {
        MainActivity mainActivity = (MainActivity) getActivity();
        //打开文件选择器
        Intent getContentIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getContentIntent.addCategory(Intent.CATEGORY_OPENABLE);
        //默认打开数据目录下的rules_export文件夹
        Uri pathToOpen = FileProvider.getUriForFile(mainActivity,
                de.honoka.android.xposed.qingxin.provider.FileProvider
                        .class.getName(),
                new File(mainActivity.getExternalFilesDir(
                        null).getAbsolutePath() + "/rules_export"));
        getContentIntent.setDataAndType(pathToOpen, "application/json");
        //添加文件选择后的回调
        mainActivity.onActivityResultCallBacks.put(Constant.RequestCode
                .IMPORT_RULES_FILE_PICK, intent -> {
            //移除回调
            mainActivity.onActivityResultCallBacks.remove(Constant.RequestCode
                    .IMPORT_RULES_FILE_PICK);
            //这里不能用Logger来发出toast信息！Logger是使用被hook的应用来发送的
            //而这里在执行的时候，并没有启动被hook的应用
            if(intent == null) {
                Toast.makeText(mainActivity, "未选择文件",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            //读取文件
            List<BlockRule> ruleList;
            Uri uri = intent.getData();
            try(InputStream is = mainActivity.getContentResolver()
                    .openInputStream(uri)) {
                byte[] bytes = IOUtils.toByteArray(is);
                String text = new String(bytes, StandardCharsets.UTF_8);
                //解码
                text = Base64Utils.decode(text);
                //解析
                Type type = new TypeToken<List<BlockRule>>() {}.getType();
                ruleList = Singletons.gson.fromJson(text, type);
            } catch(Throwable t) {
                Log.e("Qingxin", "无法解析所选择的文件", t);
                Toast.makeText(mainActivity, "无法解析所选择的文件",
                        Toast.LENGTH_LONG).show();
                return;
            }
            //确认导入
            AlertDialog.Builder confirmDialog =
                    new AlertDialog.Builder(mainActivity)
                    .setCancelable(false).setTitle("导入规则")
                    .setMessage(String.format("共%d条规则，是否导入？",
                            ruleList.size()));
            confirmDialog.setPositiveButton("确定", (dialog1, which1) -> {
                importRules(ruleList);
                Toast.makeText(mainActivity, "导入成功",
                        Toast.LENGTH_LONG).show();
                refreshRulesCount();
            });
            confirmDialog.setNegativeButton("取消", (dialog1, which1) -> {});
            confirmDialog.show();
        });
        //打开文件夹
        mainActivity.startActivityForResult(getContentIntent,
                Constant.RequestCode.IMPORT_RULES_FILE_PICK);
    }

    @SneakyThrows
    private void importRules(List<BlockRule> ruleList) {
        Dao<BlockRule, String> dao = blockRuleDao.getDao();
        for(BlockRule blockRule : ruleList) {
            dao.createOrUpdate(blockRule);
        }
    }

    @SneakyThrows
    private void exportRules(List<BlockRule> ruleList, File file) {
        if(!file.exists()) {
            FileUtils.touch(file);
        }
        try(Writer writer = new FileWriter(file)) {
            String json = Singletons.gson.toJson(ruleList);
            //编码
            json = Base64Utils.encode(json);
            writer.append(json);
        }
    }
}
