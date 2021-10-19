package de.honoka.android.xposed.qingxin.xposed.hook;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import de.honoka.android.xposed.qingxin.util.ExceptionUtils;
import de.honoka.android.xposed.qingxin.util.Logger;
import de.honoka.android.xposed.qingxin.xposed.filter.MainPageFilter;
import lombok.SneakyThrows;

/**
 * OkHttp响应体解析方法hook
 */
public class ResponseBodyHook extends LateInitHook {

	@SneakyThrows
	@Override
	public void after(MethodHookParam param) {
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
				MainPageFilter.instance
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
}
