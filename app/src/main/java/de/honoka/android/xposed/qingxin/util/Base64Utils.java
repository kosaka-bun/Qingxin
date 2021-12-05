package de.honoka.android.xposed.qingxin.util;

import android.util.Base64;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Base64Utils {
	
	private static final Charset encoding = StandardCharsets.UTF_8;
	
	public static String encode(String str) {
		return Base64.encodeToString(str.getBytes(encoding), Base64.DEFAULT);
	}
	
	public static String decode(String base64) {
		return new String(Base64.decode(base64, Base64.DEFAULT), encoding);
	}
}
