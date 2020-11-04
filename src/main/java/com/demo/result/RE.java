package com.demo.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RE {
    /** 用户不存在*/
    USER_NOT_EXIST(10001, "用户不存在"),
    /** 未设置密码*/
    NO_SET_PWD(10002, "未设置密码"),
    /** 密码错误*/
    PWD_ERR(10003, "密码错误"),
    /** 旧密码为空*/
    OLD_PWD_IS_NULL(10004, "旧密码为空"),
    /** 旧密码错误*/
    OLD_PWD_ERR(10005, "旧密码错误"),
    /** 等待扫码*/
    WAIT_SCAN(20001, "等待扫码"),
    /** 二维码过期*/
    QR_CODE_EXPIRES(20002, "二维码过期"),
    /** 未绑定微信*/
    NOT_BIND_WX(20003, "未绑定微信"),
    /** 已绑定微信*/
    ALREADY_BIND_WX(20004, "已绑定微信"),
    /** 未绑定微信*/
    GET_ACCESS_TOKEN(30001, "获取 accessToken 失败");

    private Integer code;
    private String msg;

}
