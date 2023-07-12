package com.carlife.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 控制层公共的响应对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {
    /**
     * 状态码
     */
    private String code;
    /**
     * 提示信息
     */
    private String message;
    /**
     * 响应数据
     */
    private T data;

    /**
     * 成功响应对象
     */
    public static Result ok(){
        return new Result(ResultCodeEnum.OK.getCode(), ResultCodeEnum.OK.getMessage(),null);
    }
    public static Result ok(String message){
        return new Result(ResultCodeEnum.OK.getCode(),message,null);
    }
    /**
     * 静态方法如果有泛型需要单独设置该方法安定泛型
     * @param t
     * @return
     */
    public static<T>  Result<T> ok(T t){
        return new Result(ResultCodeEnum.OK.getCode(), ResultCodeEnum.OK.getMessage(),t);
    }
    public static<T>  Result<T> ok(String message, T t){
        return new Result(ResultCodeEnum.OK.getCode(),message,t);
    }

    /**
     * 失败响应对象
     */
    public static Result fail(){
        return new Result(ResultCodeEnum.FAIL.getCode(),ResultCodeEnum.FAIL.getMessage(),null);
    }
    public static Result fail(String message){
        return new Result(ResultCodeEnum.FAIL.getCode(),message,null);
    }
    /**
     * 静态方法如果有泛型需要单独设置该方法安定泛型
     * @param t
     * @return
     */
    public static<T>  Result<T> fail(T t){
        return new Result(ResultCodeEnum.FAIL.getCode(), ResultCodeEnum.FAIL.getMessage(),t);
    }
    public static<T>  Result<T> fail(String message, T t){
        return new Result(ResultCodeEnum.FAIL.getCode(),message,t);
    }

    public static<T> Result<T> other(String code, String message, T t){
        return new Result<>(code, message, t);
    }

    public static<T> Result<T> other(String code, String message){
        return new Result(code, message, null);
    }
}
