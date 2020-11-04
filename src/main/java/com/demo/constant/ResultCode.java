package com.demo.constant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

/**
 * @author vnjohn
 * @NAME: ResultCode
 * @DATE: 2020/8/3
 */
@Getter
@ApiModel(value = "响应信息类",description = "响应信息类")
public enum ResultCode {
    /** 请求参数错误*/
    PARAM_ERROR(0,"请求参数错误"),
    /** SUCCESS*/
    SUCCESS(200,"SUCCESS"),
    /** Token 失效*/
    TOKEN_INVALID(101,"token已失效，请重新登录"),
    /** 找不到资源*/
    NOT_FOUND(404,"找不到资源"),
    /** 业务逻辑异常*/
    LOGICAL_EXCEPTION(409,"业务逻辑异常"),
    /** 参数校验异常*/
    CHECKOUT_EXCEPTION(422,"参数校验异常"),
    /** 失败*/
    ERROR(500,"服务器内部错误");
    /**
     * 错误码
     */
    @ApiModelProperty(value = "状态码",name = "code",example = "0")
    private Integer key;
    /**
     * 错误描述
     */
    @ApiModelProperty(value = "错误描述",name = "message",example = "请求失败")
    private String value;

    /**
     * 构造函数
     * @param key
     * @param value
     */
    private ResultCode(Integer key, String value) {
        this.key = key;
        this.value = value;
    }
}
