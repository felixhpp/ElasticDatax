package com.example.demo.core.utils;

import com.example.demo.core.enums.ValueTypeEnum;

import javax.validation.constraints.NotNull;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidHelper {
    /**
     * 验证类型
     *
     * @param value
     * @return
     */
    public static ValueTypeEnum ValidValueType(@NotNull String value) {
        //初始化字符串
        String str = value.trim();

        ValueTypeEnum type = ValueTypeEnum.DefaultValue;

        if (Valid(str, RegularExp.IsNumber)) {
            type = ValueTypeEnum.DefaultNumber;
        }
        //是否为正数
        else if (Valid(str, RegularExp.IsPositiveNumValue)) {
            type = ValueTypeEnum.PositiveNumValue;
        } else if (Valid(str, RegularExp.IsNegativeNumValue)) {
            type = ValueTypeEnum.NegativeNumValue;
        } else if (Valid(str, RegularExp.IsGreaterThanNumValue)) {
            type = ValueTypeEnum.GreaterThanNumValue;
        } else if (Valid(str, RegularExp.IsLessThanNumValue)) {
            type = ValueTypeEnum.LessThanNumValue;
        } else if (Valid(str, RegularExp.IsIgnoreIntger)) {
            type = ValueTypeEnum.IgnoreIntger;
        }

        return type;
    }

    /**
     * 正则表达式验证
     *
     * @param str
     * @param regexStr
     * @return
     */
    public static Boolean Valid(@NotNull String str, @NotNull String regexStr) {
        Pattern r = Pattern.compile(regexStr);
        Matcher m = r.matcher(str);

        return m.matches();
    }
}
