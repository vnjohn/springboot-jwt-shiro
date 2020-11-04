package com.demo.shiro;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * 实现 AuthenticationToken 接口按照自己的 JWT 方式来接收和校验 token 值
 * @author vnjohn
 */
public class JwtToken implements AuthenticationToken {

    private static final long serialVersionUID = 1L;
    private String token;

    public JwtToken(String token) {
        this.token = token;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }
}
