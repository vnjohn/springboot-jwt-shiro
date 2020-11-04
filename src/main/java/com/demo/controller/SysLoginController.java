/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package com.demo.controller;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.demo.constant.CommonConstant;
import com.demo.entity.SysUserEntity;
import com.demo.result.R;
import com.demo.service.SysUserService;
import com.demo.util.JwtUtil;
import com.demo.util.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录相关
 *
 * @author Mark vnjohn
 */
@RestController
public class SysLoginController {
	@Autowired
	private SysUserService sysUserService;
	@Autowired
	private RedisUtils redisUtils;

	
	/**
	 * 登录
	 */
	@RequestMapping(value = "/sys/login", method = RequestMethod.POST)
	public R login(String username, String password) {
		SysUserEntity userEntity = sysUserService.getOne(new QueryWrapper<SysUserEntity>().eq("username", username));
		if (userEntity.getStatus().equals(0)) {
			return R.error("账号已被锁定,请联系管理员");
		}else if(!userEntity.getPassword().equals(password)){
			return R.error("账号或密码不正确");
		}
		return userInfo(userEntity);
	}

	@GetMapping(value = "/sys/logout")
	public R sysLogout(HttpServletRequest request, HttpServletResponse response) {
		//用户退出逻辑
		String token = request.getHeader(CommonConstant.Authorization);
		if (StringUtils.isEmpty(token)) {
			return R.error();
		}
		String username = JwtUtil.getUsername(token);
		SysUserEntity sysUser = sysUserService.getOne(new QueryWrapper<SysUserEntity>().eq("username", username));
		if (sysUser != null) {
//			log.info(" 用户名:  " + sysUser.getRealName() + ",退出成功！ ");
			//清空用户Token缓存
			redisUtils.delete(CommonConstant.PREFIX_USER_TOKEN + token);
			//清空用户权限缓存：权限Perms和角色集合
			redisUtils.delete(CommonConstant.LOGIN_USER_CACHERULES_ROLE + username);
			redisUtils.delete(CommonConstant.LOGIN_USER_CACHERULES_PERMISSION + username);
			return R.ok();
		} else {
			return R.error();
		}
	}

	/**
	 * 用户信息
	 *
	 * @param sysUser
	 * @return
	 */
	private R userInfo(SysUserEntity sysUser) {
		String password = sysUser.getPassword();
		String username = sysUser.getUsername();
		// 生成token
		String token = JwtUtil.sign(username, password);
		// 设置超时时间
		redisUtils.set(CommonConstant.PREFIX_USER_TOKEN + token, token, JwtUtil.EXPIRE_TIME / 1000);
		// 获取用户部门信息
		JSONObject obj = new JSONObject();
		obj.put("token", token);
		obj.put("userInfo", sysUser);
		return R.ok(obj);
	}
	
}
