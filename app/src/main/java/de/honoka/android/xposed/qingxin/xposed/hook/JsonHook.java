package de.honoka.android.xposed.qingxin.xposed.hook;

import com.google.gson.JsonParser;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import de.honoka.android.xposed.qingxin.util.ExceptionUtils;
import de.honoka.android.xposed.qingxin.util.Logger;
import de.honoka.android.xposed.qingxin.xposed.filter.HotSearchFilter;
import de.honoka.android.xposed.qingxin.xposed.filter.MainPageFilter;
import de.honoka.android.xposed.qingxin.xposed.filter.SearchBarFilter;
import lombok.SneakyThrows;

/**
 * json提取的hook
 */
public class JsonHook extends LateInitHook {

	/**
	 * 处理json时需要使用的过滤器
	 */
	List<Function<String, String>> filters = Arrays.asList(
			new MainPageFilter(),
			new HotSearchFilter(),
			new SearchBarFilter()
	);

	@SneakyThrows
	@Override
	public void before(MethodHookParam param) {
		//根据参数类型获得json字符串
		String jsonStr;
		if(param.args[0] instanceof String) {
			jsonStr = (String) param.args[0];
		} else if(param.args[0] instanceof char[]) {
			jsonStr = new String((char[]) param.args[0]);
		} else {
			return;
		}
		//判断是否是jsonObject
		jsonStr = jsonStr.trim();
		if(!jsonStr.startsWith("{")) return;
		//检验json语法是否正确
		try {
			JsonParser.parseString(jsonStr);
		} catch(Throwable t) {
			return;
		}
		//处理并修改
		if(param.args[0] instanceof String) {
			param.args[0] = handleJson(jsonStr);
		} else if(param.args[0] instanceof char[]) {
			param.args[0] = handleJson(jsonStr).toCharArray();
		}
	}

	/**
	 * 依次用json去尝试执行多个过滤操作，一个成功即返回过滤后的值
	 */
	private String handleJson(String jsonStr) {
		//构建操作列表，依次操作，一个成功即返回过滤后的值
		//由于不能确定json属于哪种数据，所以将所有过滤规则都试一遍，取有效的那一个
		//所以过滤器一定不能在一般情况下抛出异常
		for(Function<String, String> filter : filters) {
			try {
				return filter.apply(jsonStr);
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
}
