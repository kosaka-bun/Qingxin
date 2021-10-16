package de.honoka.android.xposed.qingxin.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import de.honoka.android.xposed.qingxin.R;
import de.honoka.android.xposed.qingxin.dao.BlockRuleDao;
import de.honoka.android.xposed.qingxin.entity.BlockRule;
import de.honoka.android.xposed.qingxin.util.AndroidUtils;
import de.honoka.android.xposed.qingxin.util.NoFeedTextWatcher;

public class AddRuleActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_rule);

		AndroidUtils.setActionBarIcon(this);
		//禁止内容输出框出现换行符
		EditText editText = findViewById(R.id.rule_content);
		editText.addTextChangedListener(new NoFeedTextWatcher(editText));
		//为按钮添加监听
		findViewById(R.id.add_button).setOnClickListener(onAddListener);
	}

	private View.OnClickListener onAddListener = view -> {
		//设置为不可点击
		view.setEnabled(false);
		//规则内容
		String content = ((EditText) findViewById(R.id.rule_content))
				.getText().toString().trim();
		if(content.equals("")) {
			Toast.makeText(this, "内容不能为空",
					Toast.LENGTH_LONG).show();
			view.setEnabled(true);
			return;
		}
		//类型
		String type;
		if(((RadioButton) findViewById(R.id.type_keyword_radio)).isChecked())
			type = BlockRule.RuleType.KEYWORD;
		else if(((RadioButton) findViewById(R.id.type_pattern_radio)).isChecked()) {
			type = BlockRule.RuleType.PATTERN;
			//正则表达式不区分大小写
			content = "(?i)" + content;
			//验证正则表达式
			try {
				Pattern.compile(content);
			} catch(PatternSyntaxException pse) {
				//语法错误
				Toast.makeText(this, "正则表达式语法不正确",
						Toast.LENGTH_LONG).show();
				view.setEnabled(true);
				return;
			}
		} else {
			//未选择类型
			Toast.makeText(this, "请选择类型",
					Toast.LENGTH_LONG).show();
			view.setEnabled(true);
			return;
		}
		//region 作用域
		//视频标题
		boolean videoTitle = ((CheckBox) findViewById(R.id.video_title_checkbox))
				.isChecked();
		//视频分区
		boolean videoSubArea = ((CheckBox) findViewById(
				R.id.video_sub_area_checkbox)).isChecked();
		//视频频道
		boolean videoChannel = ((CheckBox) findViewById(
				R.id.video_channel_checkbox)).isChecked();
		//用户名
		boolean username = ((CheckBox) findViewById(R.id.username_checkbox))
				.isChecked();
		//评论
		boolean comment = ((CheckBox) findViewById(R.id.comment_checkbox))
				.isChecked();
		//弹幕
		boolean danmaku = ((CheckBox) findViewById(R.id.danmaku_checkbox))
				.isChecked();
		//热搜
		boolean hotSearchWord = ((CheckBox) findViewById(
				R.id.hot_search_word_checkbox)).isChecked();
		//动态
		boolean dongtai = ((CheckBox) findViewById(R.id.dongtai_checkbox))
				.isChecked();
		//endregion
		//region 绑定数据，保存数据
		BlockRule blockRule = new BlockRule()
				.setContent(content)
				.setType(type)
				.setVideoTitle(videoTitle)
				.setVideoSubArea(videoSubArea)
				.setVideoChannel(videoChannel)
				.setUsername(username)
				.setComment(comment)
				.setDanmaku(danmaku)
				.setHotSearchWord(hotSearchWord)
				.setDongtai(dongtai);
		try {
			BlockRuleDao blockRuleDao = new BlockRuleDao(this);
			blockRuleDao.getDao().create(blockRule);
			Toast.makeText(this, "保存成功",
					Toast.LENGTH_LONG).show();
			//保存成功后关闭activity
			finish();
		} catch(Throwable t) {
			Toast.makeText(this, "保存失败",
					Toast.LENGTH_LONG).show();
			view.setEnabled(true);
		}
		//endregion
	};
}
