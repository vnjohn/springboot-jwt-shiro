/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package com.demo.shiro;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.demo.entity.SysMenuEntity;
import com.demo.entity.SysUserEntity;
import com.demo.mapper.SysMenuMapper;
import com.demo.mapper.SysUserMapper;
import com.demo.constant.CommonConstant;
import com.demo.mapper.SysUserMapper;
import com.demo.util.IPUtils;
import com.demo.util.JwtUtil;
import com.demo.util.RedisUtils;
import com.demo.util.SpringContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;

/**
 * 认证
 * @author vnjohn
 */
@Component
@Slf4j
public class UserRealm extends AuthorizingRealm {
	/** 超级管理员ID */
	public static final int SUPER_ADMIN = 1;
    @Autowired
    private SysUserMapper SysUserEntityDao;
    @Autowired
    private SysMenuMapper sysMenuDao;

    @Autowired
	private RedisUtils redisUtils;

	/**
	 * 必须重写此方法，不然Shiro会报错
	 */
	@Override
	public boolean supports(AuthenticationToken token) {
		return token instanceof JwtToken;
	}

    /**
     * 授权(验证权限时调用)
     */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		SysUserEntity user = (SysUserEntity)principals.getPrimaryPrincipal();
		Long userId = user.getUserId();
		
		List<String> permsList;
		
		//系统管理员，拥有最高权限 如果 userId 是为 1 的话就不会过滤用户下所属的菜单
		if(userId == SUPER_ADMIN){
			List<SysMenuEntity> menuList = sysMenuDao.selectList(null);
			permsList = new ArrayList<>(menuList.size());
			for(SysMenuEntity menu : menuList){
				permsList.add(menu.getPerms());
			}
		}else{
			permsList = SysUserEntityDao.queryAllPerms(userId);
		}

		//用户权限列表
		Set<String> permsSet = new HashSet<>();
		for(String perms : permsList){
			if(StringUtils.isBlank(perms)){
				continue;
			}
			permsSet.addAll(Arrays.asList(perms.trim().split(",")));
		}
		
		SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
		info.setStringPermissions(permsSet);
		return info;
	}

	/**
	 * 认证(登录时调用)
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(
			AuthenticationToken authcToken) throws AuthenticationException {
		String token = (String) authcToken.getCredentials();
		if (token == null) {
			log.info("————————身份认证失败——————————IP地址:  " + IPUtils.getIpAddr(SpringContextUtils.getHttpServletRequest()));
			throw new AuthenticationException("token为空!");
		}
		// 校验token有效性
		SysUserEntity loginUser = this.checkUserTokenIsEffect(token);
		return new SimpleAuthenticationInfo(loginUser, token, getName());
	}

	/**
	 * 校验token的有效性
	 *
	 * @param token
	 */
	public SysUserEntity checkUserTokenIsEffect(String token) throws AuthenticationException {
		// 解密获得username，用于和数据库进行对比
		String username = JwtUtil.getUsername(token);
		if (username == null) {
			throw new AuthenticationException("token非法无效!");
		}
		// 查询用户信息
		SysUserEntity loginUser = new SysUserEntity();
		SysUserEntity SysUserEntity = SysUserEntityDao.selectOne(new QueryWrapper<SysUserEntity>().eq("username", username));
		if (SysUserEntity == null) {
			throw new AuthenticationException("用户不存在!");
		}
		// 校验token是否超时失效 & 或者账号密码是否错误
		if (!jwtTokenRefresh(token, username, SysUserEntity.getPassword())) {
			throw new AuthenticationException("Token失效请重新登录!");
		}
		// 判断用户状态
		if (!SysUserEntity.getStatus().equals(1)) {
			throw new AuthenticationException("账号已被删除,请联系管理员!");
		}
		BeanUtils.copyProperties(SysUserEntity, loginUser);
		return loginUser;
	}

	/**
	 * JWTToken 刷新生命周期 （解决用户一直在线操作，提供Token失效问题）
	 * 1、登录成功后将用户的JWT生成的Token作为k、v存储到cache缓存里面(这时候k、v值一样)
	 * 2、当该用户再次请求时，通过JWTFilter层层校验之后会进入到doGetAuthenticationInfo进行身份验证
	 * 3、当该用户这次请求JWTToken值还在生命周期内，则会通过重新PUT的方式k、v都为Token值，缓存中的token值生命周期时间重新计算(这时候k、v值一样)
	 * 4、当该用户这次请求jwt生成的token值已经超时，但该token对应cache中的k还是存在，则表示该用户一直在操作只是JWT的token失效了，程序会给token对应的k映射的v值重新生成JWTToken并覆盖v值，该缓存生命周期重新计算
	 * 5、当该用户这次请求jwt在生成的token值已经超时，并在cache中不存在对应的k，则表示该用户账户空闲超时，返回用户信息已失效，请重新登录。
	 * 6、每次当返回为true情况下，都会给Response的Header中设置Authorization，该Authorization映射的v为cache对应的v值。
	 * 7、注：当前端接收到Response的Header中的Authorization值会存储起来，作为以后请求token使用
	 * 参考方案：https://blog.csdn.net/qq394829044/article/details/82763936
	 * @param userName
	 * @param passWord
	 * @return
	 */
	public boolean jwtTokenRefresh(String token, String userName, String passWord) {
		String cacheToken = String.valueOf(redisUtils.get(CommonConstant.PREFIX_USER_TOKEN + token));
		if (StringUtils.isNotEmpty(cacheToken)) {
			// 校验token有效性
			if (!JwtUtil.verify(cacheToken, userName, passWord)) {
				String newAuthorization = JwtUtil.sign(userName, passWord);
				// 设置超时时间
				redisUtils.set(CommonConstant.PREFIX_USER_TOKEN + token, newAuthorization,JwtUtil.EXPIRE_TIME / 1000);
			} else {
				// 设置超时时间
				redisUtils.set(CommonConstant.PREFIX_USER_TOKEN + token, cacheToken, JwtUtil.EXPIRE_TIME / 1000);
			}
			return true;
		}
		return false;
	}
}
