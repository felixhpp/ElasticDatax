package com.example.demo.core.utils;

import com.example.demo.core.enums.ValueTypeEnum;
import org.springframework.util.StringUtils;

import java.text.DecimalFormat;

public final class ValueFormat {
    public static void main(String args[]) {
        ValueTypeEnum type1 = ValidHelper.ValidValueType("11");
        ValueTypeEnum type2 = ValidHelper.ValidValueType("-1.11");
        ValueTypeEnum type3 = ValidHelper.ValidValueType(">1.11");
        System.out.println("Hello world!");
    }

    private int IntgerAcc = 3;       // 整数精度,默认为3
    private int DecimalAcc = 3;     // 小数精度，默认为3
    private String Accuracy;
    private double MaxValue;
    // 正负位
    private String PosNegNum = "0";
    // 大于小于位
    private String GltNum = "0";
    // 绝对值
    private String AbsoluteValue;
    private String FormatData = "";

    //private ValueTypeEnum DataType = ValueTypeEnum.DefaultValue;

    public ValueFormat() {
        Accuracy = getAccuracy();
        MaxValue = getMaxValue();
    }

    public ValueFormat(int intgerAcc, int decimalAcc) {
        this.IntgerAcc = intgerAcc;
        this.DecimalAcc = decimalAcc;
        Accuracy = getAccuracy();
        MaxValue = getMaxValue();
    }

    public String ToFormat(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        Boolean IsNumber = true;
        String format = "";


        value = value.trim();
        // 获取类型
        ValueTypeEnum DataType = ValidHelper.ValidValueType(value);
        String posNegNum = "1";
        String gltNum = "1";
        // 绝对值， 补0 后
        String absolute = "";
        switch (DataType) {
            case PositiveNumValue:  //正数
                posNegNum = "1";
                absolute = value.replace("+", "");
                break;
            case NegativeNumValue:  //负数
                posNegNum = "0";
                absolute = value.replace("-", "");
                break;
            case DefaultNumber:
                absolute = value;
                break;
            case GreaterThanNumValue:
                gltNum = "2";
                absolute = value.replace(">", "");
                break;
            case LessThanNumValue:
                gltNum = "0";
                absolute = value.replace("<", "");
                break;
            case IgnoreIntger:
                absolute = String.format("0%s", value);
                IsNumber = true;
                break;
            default:
                absolute = value;
                IsNumber = false;
                break;
        }

        if (!"".equals(absolute) && IsNumber) {
            double curNum = Double.parseDouble(absolute);
            if (DataType == ValueTypeEnum.NegativeNumValue) {
                curNum = MaxValue - curNum;
            }
            DecimalFormat df = new DecimalFormat(getAccuracy());
            absolute = df.format(curNum);
        }

        if (IsNumber) {
            format = String.format("%s%s%s", posNegNum, absolute, gltNum);
        } else {
            format = absolute;
        }

        FormatData = format;

        return format;
    }

    /**
     * 补0操作
     *
     * @return
     */
    private String getAccuracy() {
        StringBuilder intgetStr = new StringBuilder();
        for (int i = 0; i < this.IntgerAcc; i++) {
            intgetStr.append("0");
        }

        StringBuilder decimalStr = new StringBuilder();
        for (int i = 0; i < this.DecimalAcc; i++) {
            decimalStr.append("0");
        }

        return "" + intgetStr.toString() + "." + decimalStr.toString() + "";
    }

    /**
     * 获取最大值
     *
     * @return
     */
    private double getMaxValue() {
        StringBuilder numberStr = new StringBuilder();
        StringBuilder intgetStr = new StringBuilder();
        for (int i = 0; i < this.IntgerAcc; i++) {
            intgetStr.append("9");
        }

        StringBuilder decimalStr = new StringBuilder();
        for (int i = 0; i < this.DecimalAcc; i++) {
            decimalStr.append("9");
        }

        numberStr.append(intgetStr).append(".").append(decimalStr);

        return Double.parseDouble(numberStr.toString());
    }
}
