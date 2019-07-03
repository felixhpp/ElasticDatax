package com.example.demo.core.utils;

import com.example.demo.core.enums.ValueTypeEnum;

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
    private String MeteData = "";
    // 正负位
    private String PosNegNum = "0";
    // 大于小于位
    private String GltNum = "0";
    // 绝对值
    private String AbsoluteValue;
    private String FormatData = "";

    private ValueTypeEnum DataType = ValueTypeEnum.DefaultValue;
    private Boolean IsNumber = true;

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
        MeteData = value;
        String format = "";

        doFormatValue(value);
        if (IsNumber) {
            format = String.format("%s%s%s", PosNegNum, AbsoluteValue, GltNum);
        } else {
            format = AbsoluteValue;
        }

        FormatData = format;

        return format;
    }

    private void doFormatValue(String value) {
        if (value == "" && value == null)
            throw new IllegalArgumentException("value");

        value = value.trim();
        // 获取类型
        DataType = ValidHelper.ValidValueType(value);
        String formatStr = "";
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

        if (absolute != "" && IsNumber) {
            double curNum = Double.parseDouble(absolute);
            if (DataType == ValueTypeEnum.NegativeNumValue) {
                curNum = MaxValue - curNum;
            }
            DecimalFormat df = new DecimalFormat(getAccuracy());
            absolute = df.format(curNum);
        }

        PosNegNum = posNegNum;
        GltNum = gltNum;
        AbsoluteValue = absolute;
    }

    /**
     * 补0操作
     *
     * @return
     */
    private String getAccuracy() {
        String intgetStr = "";
        for (int i = 0; i < this.IntgerAcc; i++) {
            intgetStr = intgetStr + "0";
        }

        String decimalStr = "";
        for (int i = 0; i < this.DecimalAcc; i++) {
            decimalStr = decimalStr + "0";
        }

        return "" + intgetStr + "." + decimalStr + "";
    }

    /**
     * 获取最大值
     *
     * @return
     */
    private double getMaxValue() {
        String intgetStr = "";
        for (int i = 0; i < this.IntgerAcc; i++) {
            intgetStr = intgetStr + "9";
        }

        String decimalStr = "";
        for (int i = 0; i < this.DecimalAcc; i++) {
            decimalStr = decimalStr + "9";
        }

        String numstr = intgetStr + "." + decimalStr;

        return Double.parseDouble(numstr);
    }
}
