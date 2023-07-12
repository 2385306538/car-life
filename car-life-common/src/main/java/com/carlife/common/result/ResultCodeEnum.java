package com.carlife.common.result;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 枚举类：专门用于存放常量信息，用一个类管理常量
 */
@AllArgsConstructor
@NoArgsConstructor
public enum ResultCodeEnum {
    /**
     * 相当于调用枚举类的构造方法，和类的区别是名字可以自定义
     * 使用OK管理两个两个常量：“20000”，"操作成功"
     */
    /**
     * 相当于enum里面两个对象，一个对象称叫OK一个叫FAIL
     */
    OK("20000","操作成功"),
    FAIL("50000","操作失败"),


    USER_EXISTED("30001", "用户已存在"),
    USER_LOGIN_ERROR("30002", "登陆失败，账号或密码错误！"),
    USER_NOT_LOGGED_IN("3001", "用户未登录"),
    USER_ACCOUNT_FORBIDDEN("30003", "账号已被禁用"),
    USER_NOT_EXIST("30004", "用户不存在"),
    USER_ALREADY_BINDED("30005","用户已开户"),
    USER_CARD_ALREADY_BINDED("30006","银行卡已被绑定"),
    USER_VERIFY_CODE_ERROR("30007","验证码错误"),
    USER_LOGIN_ILLEGAL_ERROR("30008","用户非法登陆"),

    /* 系统错误：40001-49999 */
    SYSTEM_INNER_ERROR("40001", "系统繁忙，请稍后重试"),

    /* 数据错误：50001-599999 */
    RESULE_DATA_NONE("50001", "数据未找到"),
    DATA_IS_WRONG("50002", "数据有误"),
    DATA_ALREADY_EXISTED("50003", "数据已存在"),

    /* 接口错误：60001-69999 */
    INTERFACE_INNER_INVOKE_ERROR("60001", "内部系统接口调用异常"),
    INTERFACE_OUTTER_INVOKE_ERROR("60002", "外部系统接口调用异常"),
    INTERFACE_FORBID_VISIT("60003", "该接口禁止访问"),
    INTERFACE_ADDRESS_INVALID("60004", "接口地址无效"),
    INTERFACE_REQUEST_TIMEOUT("60005", "接口请求超时"),
    INTERFACE_EXCEED_LOAD("60006", "接口负载过高"),
    /* 权限错误：70001-79999 */
    PERMISSION_NO_ACCESS("70001", "无访问权限");
    /**
     * 响应的状态码
     */
    private String code;
    /**
     * 定义提示信息
     */
    private String message;

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
