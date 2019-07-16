package com.example.demo.jobs.hbase;

public class RowKeyUtil {
    /**
     * 反转rowkey
     * @param rowKey
     * @return
     */
    public static String getReverse(String rowKey){
        char[] arry = rowKey.toCharArray();
        StringBuilder reverse= new StringBuilder();
        for(int i=arry.length-1;i>=0;i--){
            reverse.append(arry[i]);
        }
        return reverse.toString();
    }
}
