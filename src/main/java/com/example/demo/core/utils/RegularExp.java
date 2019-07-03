package com.example.demo.core.utils;

/**
 * 正则表达式
 */
public final class RegularExp {
    /**
     * 正数
     */
    public static String IsPositiveNumValue = "(^[+][0-9]+$)|(^[+][0-9]+\\.?[0-9]+$)";

    /**
     * 负数
     */
    public static String IsNegativeNumValue = "(^[-][0-9]+$)|(^[-][0-9]+\\.?[0-9]+$)";

    /**
     * 正常的数
     */
    public static String IsNumber = "^\\d+(\\.\\d+)?$";

    /**
     * 大于
     */
    public static String IsGreaterThanNumValue = "(^[>][0-9]+$)|(^[>][0-9]+\\.?[0-9]+$)";

    /**
     * 小于
     */
    public static String IsLessThanNumValue = "(^[<][0-9]+$)|(^[<][0-9]+\\.?[0-9]+$)";

    /**
     * 忽略整数部分。如 .1  .11
     */
    public static String IsIgnoreIntger = "^\\.?[1-9]+$";
}
