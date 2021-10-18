package de.honoka.android.xposed.qingxin.xposed.hook;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import de.honoka.android.xposed.qingxin.util.ExceptionUtils;
import de.honoka.android.xposed.qingxin.util.Logger;
import de.honoka.android.xposed.qingxin.xposed.XposedMain;
import de.honoka.android.xposed.qingxin.xposed.model.BlockRuleCache;
import de.robv.android.xposed.XC_MethodHook;
import lombok.SneakyThrows;

/**
 * OkHttp响应体解析方法hook
 */
public class ResponseBodyHook extends XC_MethodHook {

	private BlockRuleCache blockRuleCache;

	public ResponseBodyHook(BlockRuleCache blockRuleCache) {
		this.blockRuleCache = blockRuleCache;
	}

	@SneakyThrows
	@Override
	protected void afterHookedMethod(MethodHookParam param) {
		Object response = param.getResult();
		String str = "";
		if(response instanceof String) {
			str = ((String) response).trim();
		} else if(response instanceof byte[]) {
			byte[] bytes = (byte[]) response;
			str = new String(bytes, StandardCharsets.UTF_8).trim();
		}
		//判断是否是json
		if(str.startsWith("{")) {
			//将处理后的json修改过去
			String handledJson = handleJson(str);
			//根据返回值类型设定返回值
			if(response instanceof String) {
				param.setResult(handledJson);
			} else if(response instanceof byte[]) {
				param.setResult(handledJson.getBytes(StandardCharsets.UTF_8));
			}
			return;
		}
	}

	/**
	 * 依次用json去尝试执行多个过滤操作，一个成功即返回过滤后的值
	 */
	private String handleJson(String jsonStr) {
		//构建操作列表，依次操作，一个成功即返回过滤后的值
		List<Function<String, String>> functions = Arrays.asList(
				mainPageFilter
		);
		//由于不能确定json属于哪种数据，所以将所有过滤规则都试一遍，取有效的那一个
		//所以过滤器一定不能在一般情况下抛出异常
		for(Function<String, String> function : functions) {
			try {
				return function.apply(jsonStr);
			} catch(NullPointerException | ClassCastException ignore) {
				//ignore
				//Logger.testLog(ExceptionUtils.transfer(ignore));
			} catch(Throwable t) {
				Logger.testLog(ExceptionUtils.transfer(t));
			}
		}
		//均未成功，返回原值
		return jsonStr;
	}

	/**
	 * 首页推荐过滤器
	 */
	private final Function<String, String> mainPageFilter = json -> {
		//根据字符串提取出来的主对象，所有修改操作都基于它来完成
		JsonObject jo = JsonParser.parseString(json).getAsJsonObject();
		JsonArray items = jo.getAsJsonObject("data")
				.getAsJsonArray("items");
		//region 前置判断，判断json是不是首页推荐数据
		//首页推荐视频条目数不可能为0
		if(items.size() <= 0) throw new NullPointerException();
		//条目必须包含card_type和card_goto
		JsonObject testItem = items.get(0).getAsJsonObject();
		if(!testItem.has("card_type") ||
		   !testItem.has("card_goto"))
			throw new NullPointerException();
		//endregion
		//过滤、计数
		int blockCount = 0;
		for(Iterator<JsonElement> iterator = items.iterator();
			iterator.hasNext(); ) {
			JsonObject item = iterator.next().getAsJsonObject();
			//判断是否是首页推广（创作推广、游戏、会员购、轮播图、纪录片、番剧等）
			if(blockRuleCache.isMainPageItemPublicity(item)) {
				//是推广，判断屏蔽开关
				if(XposedMain.mainPreference.getBlockAllMainPagePublicity()) {
					iterator.remove();
					blockCount++;
					Logger.blockLog("首页推荐拦截【屏蔽所有推广】：" +
							getMainPageItemTitle(item));
					continue;
				}
			}
			//判断是不是轮播图
			if(item.get("card_goto").getAsString().equals("banner")) {
				//遍历轮播图项目
				JsonArray bannerItems = item.getAsJsonArray(
						"banner_item");
				for(Iterator<JsonElement> bannerItemIterator =
				    bannerItems.iterator(); bannerItemIterator.hasNext(); ) {
					JsonObject bannerItem = bannerItemIterator.next()
							.getAsJsonObject();
					if(blockRuleCache.isBlockBannerItem(bannerItem)) {
						bannerItemIterator.remove();
						blockCount++;
						Logger.blockLog("轮播图推荐拦截：" +
								getBannerItemTitle(bannerItem));
						continue;
					}
				}
				//轮播图不用再进行下面的判断，它不是一般的首页推荐项
				continue;
			}
			//判断是否是按规则应当屏蔽的内容（一般的首页推荐项目，即除了轮播图以外的其他项目）
			if(blockRuleCache.isBlockMainPageItem(item)) {
				iterator.remove();
				blockCount++;
				Logger.blockLog("首页推荐拦截【规则】：" + getMainPageItemTitle(item));
				continue;
			}
		}
		if(blockCount > 0)
			Logger.toastOnBlock("拦截了" + blockCount + "条首页推荐");
		String handledJson = jo.toString();
		//输出测试信息
		//Logger.testLog("输出json");
		//Logger.testLog(handledJson);
		//Logger.testLog("输出json完成");
		return handledJson;
	};

	/**
	 * 从首页推荐轮播图中的某一个项目中获取标题
	 */
	public static String getBannerItemTitle(JsonObject bannerItem) {
		//json解析非常容易抛异常，必须尽可能考虑解析错误的情况
		try {
			//查找包含banner的那个键
			Set<String> keys = bannerItem.keySet();
			for(String key : keys) {
				if(key.contains("banner")) {
					//这个键对应的值是一个jsonObject，里面的title就是标题
					JsonObject bannerInfo = bannerItem.getAsJsonObject(key);
					return bannerInfo.get("title").getAsString();
				}
			}
			return "";
		} catch(Throwable t) {
			return "";
		}
	}

	/**
	 * 从首页推荐项中获取标题
	 */
	public static String getMainPageItemTitle(JsonObject item) {
		try {
			switch(item.get("card_goto").getAsString()) {
				case "ad_web_s": {
					return item.getAsJsonObject("ad_info")
							.getAsJsonObject("creative_content")
							.get("title").getAsString();
				}
				case "banner":
					return "【轮播图】";
				case "av":
				case "bangumi":
				case "ad_av":
				default: {
					return item.get("title").getAsString();
				}
			}
		} catch(Throwable t) {
			return "";
		}
	}
}
