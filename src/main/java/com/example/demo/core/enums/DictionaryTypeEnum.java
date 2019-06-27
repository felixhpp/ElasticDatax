package com.example.demo.core.enums;

import com.example.demo.core.dics.AdmStatus;
import com.example.demo.core.exception.LogicException;

/**
 * 字典类型 枚举
 */
public enum  DictionaryTypeEnum {
    DEPARTMENT(1, com.example.demo.core.dics.Deptartment.class, "科室", CacheName.defaultCacheName,"dept_"),       // 科室字典
    SEX(2, com.example.demo.core.dics.Sex.class, "性别", CacheName.defaultCacheName,"sex_"),              // 性别
    NATIONAL(3, com.example.demo.core.dics.National.class, "国籍", CacheName.defaultCacheName, "national_"),         // 国籍
    ADM_TYPE(4, com.example.demo.core.dics.AdmType.class, "就诊类型", CacheName.defaultCacheName,"adm_type_"),         // 就诊类型

    DIAGNOSE_TYPE(5, com.example.demo.core.dics.DiagnoseType.class, "诊断类型", CacheName.defaultCacheName,"diagnose_type_"),   // 诊断类型
    DIAGNOSE_NAME(6, com.example.demo.core.dics.DiagnoseName.class, "诊断名称",CacheName.diagnoseCacheName, "diagnose_name_"),  // 诊断名称
    MARITAL(7, com.example.demo.core.dics.Marital.class, "婚姻状态", CacheName.defaultCacheName,"marital_"),  // 婚姻状态
    HOSPITAL(8, com.example.demo.core.dics.Hospital.class, "医院", CacheName.defaultCacheName,"hospital_"),

    ORDSER_ITEM(9, com.example.demo.core.dics.OrderItem.class, "医嘱项目",
            CacheName.defaultCacheName, "order_"),
    ORDER_TYPE(10, com.example.demo.core.dics.OrdType.class, "医嘱类型",
            CacheName.defaultCacheName, "order_type_"),
    ORDER_CATEAGE(11, com.example.demo.core.dics.OrdCateage.class, "医嘱大分类",
            CacheName.defaultCacheName, "order_cate_"),
    ORDER_STATUS(12, com.example.demo.core.dics.OrderStatus.class, "医嘱状态",
            CacheName.defaultCacheName, "order_status_"),
    DURATION(13, com.example.demo.core.dics.Duration.class, "疗程",
            CacheName.defaultCacheName, "duration_"),
    FREQ(14, com.example.demo.core.dics.Freq.class, "给药频次",
            CacheName.defaultCacheName, "freq_"),
    AdmStatus(15, com.example.demo.core.dics.AdmStatus.class, "就诊状态",
         CacheName.defaultCacheName, "freq_"),
    LisItem(16, com.example.demo.core.dics.LisItem.class, "检验项",
            CacheName.itemCaseName, "lisitem_"),
    PHDrgMaterial(17, com.example.demo.core.dics.PHDrgMaterial.class, "药学项",
            CacheName.itemCaseName, "phdrg_"),
    ;

    interface CacheName{
        String defaultCacheName = "dic_dicCache";
        String diagnoseCacheName = "dic_diagnoseCache";
        String itemCaseName = "dic_itemCache";
    }

    private int type;
    private Class name;
    private String desc;
    private String cacheName;
    /**
     * 缓存的key 前缀
     */
    private String cachePrefix;
    private String classNameString = "";

    private DictionaryTypeEnum(int type, Class name, String desc, String cacheName, String cachePrefix){
        this.type=type;
        this.name = name;
        this.desc = desc;
        this.cacheName = cacheName;
        this.cachePrefix = cachePrefix;
        classNameString = name.getName();       //由com.example.demo.core.dics.Hospital.class 转为"com.example.demo.core.dics.Hospital.class"
    }

    final public int getType() {
        return type;
    }

    final public String getClassName() {
        return classNameString;
    }

    final public String getDesc(){
        return desc;
    }
    final public String getCachePrefix(){
        return cachePrefix;
    }

    final public String getCacheName(){
        return cacheName;
    }

    public static DictionaryTypeEnum getByName(String name){
        for (DictionaryTypeEnum aparameter : values()) {
            if (aparameter.getClassName().equals(name)) {
                return aparameter;
            }
        }
        throw  new LogicException("XML name名称的字典不再枚举范围内, name:" + name);
    }

}
