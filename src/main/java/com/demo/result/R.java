/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package com.demo.result;

import java.util.HashMap;
import java.util.Map;

/**
 * 返回数据
 *
 * @author Mark vnjohn
 */
public class R extends HashMap<String, Object> {
	private static final long serialVersionUID = 1L;
	
	public R() {
		put("code", 200);
		put("message", "SUCCESS");
	}

	public static R checkError(String name) {
		return error(0, name+"不符合规定值");
	}
	
	public static R error() {
		return error(500, "未知异常，请联系管理员");
	}
	
	public static R error(String message) {
		return error(500, message);
	}

	public static R error(RE re) {
		return error(re.getCode(), re.getMsg());
	}
	
	public static R error(int code, String message) {
		R r = new R();
		r.put("code", code);
		r.put("message", message);
		return r;
	}

	public static R ok(String message) {
		R r = new R();
		r.put("code",200);
		r.put("message", message);
		return r;
	}
	
	public static R ok(Map<String, Object> map) {
		R r = new R();
		r.putAll(map);
		return r;
	}
	
	public static R ok() {
		return new R();
	}

	@Override
	public R put(String key, Object value) {
		super.put(key, value);
		return this;
	}
}
