package de.honoka.android.xposed.qingxin.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import de.honoka.android.xposed.qingxin.R;
import de.honoka.android.xposed.qingxin.util.AndroidUtils;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		AndroidUtils.setActionBarIcon(this);
	}
}
