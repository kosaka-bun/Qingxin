package de.honoka.android.xposed.qingxin.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * 禁止输出框出现换行符的文本监听器
 */
public class NoFeedTextWatcher implements TextWatcher {

	private EditText editText;

	private int lengthBefore;

	public NoFeedTextWatcher(EditText editText) {
		this.editText = editText;
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start,
	                              int count, int after) {
		lengthBefore = s.length();
	}

	@Override
	public void onTextChanged(CharSequence s, int start,
	                          int before, int count) {}

	@Override
	public void afterTextChanged(Editable s) {
		String str = s.toString();
		//如果字符减少了，就不必验证
		if(str.length() < lengthBefore) return;
		if(str.contains("\n")) {
			String replace = str.replace("\n", "");
			editText.setText(replace);
			editText.setSelection(replace.length());
		}
	}
}
