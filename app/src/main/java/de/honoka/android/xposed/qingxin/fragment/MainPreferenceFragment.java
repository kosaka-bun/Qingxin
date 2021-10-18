package de.honoka.android.xposed.qingxin.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.widget.EditText;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import de.honoka.android.xposed.qingxin.R;
import de.honoka.android.xposed.qingxin.activity.AddRuleActivity;
import de.honoka.android.xposed.qingxin.common.Constant;
import de.honoka.android.xposed.qingxin.common.Singletons;
import de.honoka.android.xposed.qingxin.dao.BlockRuleDao;
import de.honoka.android.xposed.qingxin.entity.BlockRule;
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
		props = (Map<String, Object>) preferenceScreen.getSharedPreferences().getAll();
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
}
