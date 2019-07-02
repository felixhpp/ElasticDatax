package com.example.demo.core.enums;

import com.example.demo.core.exception.LogicException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 字典类型 枚举
 */
public enum  DictionaryTypeEnum {
    DEPARTMENT(1, "Deptartment", "科室",
            CacheName.defaultCacheName,"dept_"),       // 科室字典
    SEX(2, "Sex", "性别",
            CacheName.defaultCacheName,"sex_"),              // 性别
    NATIONAL(3, "National", "国籍",
            CacheName.defaultCacheName, "national_"),         // 国籍
    ADM_TYPE(4, "AdmType", "就诊类型",
            CacheName.defaultCacheName,"adm_type_"),         // 就诊类型

    DIAGNOSE_TYPE(5, "DiagnoseType", "诊断类型",
            CacheName.defaultCacheName,"diagnose_type_"),   // 诊断类型
    DIAGNOSE_NAME(6, "DiagnoseName", "诊断名称",
            CacheName.diagnoseCacheName, "diagnose_name_"),  // 诊断名称
    MARITAL(7, "Marital", "婚姻状态",
            CacheName.defaultCacheName,"marital_"),  // 婚姻状态
    HOSPITAL(8, "Hospital", "医院",
            CacheName.defaultCacheName,"hospital_"),

    ORDSER_ITEM(9, "OrderItem", "医嘱项目",
            CacheName.itemCaseName, "order_"),
    ORDER_TYPE(10, "OrdType", "医嘱类型",
            CacheName.defaultCacheName, "order_type_"),
    ORDER_CATEAGE(11, "OrdCateage", "医嘱大分类",
            CacheName.defaultCacheName, "order_cate_"),
    ORDER_STATUS(12, "OrderStatus", "医嘱状态",
            CacheName.defaultCacheName, "order_status_"),
    DURATION(13, "Duration", "疗程",
            CacheName.defaultCacheName, "duration_"),
    FREQ(14, "Freq", "给药频次",
            CacheName.defaultCacheName, "freq_"),
    AdmStatus(15, "AdmStatus", "就诊状态",
         CacheName.defaultCacheName, "freq_"),
    LisItem(16, "LisItem", "检验项",
            CacheName.itemCaseName, "lisitem_"),
    PHDrgMaterial(17, "PHDrgMaterial", "药学项",
            CacheName.itemCaseName, "phdrg_"),
    InstrUsage(18, "InstrUsage", "用药途径",
            CacheName.itemCaseName, "usage_"),
    OrdChildCategory(19, "OrdChildCategory", "医嘱子分类",
            CacheName.itemCaseName, "childcate_"),
    PHCGeneric(20, "PHCGeneric", "药品通用名",
                     CacheName.itemCaseName, "phcgeneric_"),
    PHCGoods(21, "PHCGoods", "药品商品名称",
            CacheName.itemCaseName, "phcgoods_"),
    ;
    private static final Logger logger = LoggerFactory.getLogger(DictionaryTypeEnum.class);
    interface CacheName{
        String defaultCacheName = "dic_dicCache";
        String diagnoseCacheName = "dic_diagnoseCache";
        String itemCaseName = "dic_itemCache";
    }

    private int type;
    private String name;
    private String desc;
    private String cacheName;
    /**
     * 缓存的key 前缀
     */
    private String cachePrefix;

    private DictionaryTypeEnum(int type, String name, String desc, String cacheName, String cachePrefix){
        this.type=type;
        this.name = name;
        this.desc = desc;
        this.cacheName = cacheName;
        this.cachePrefix = cachePrefix;
    }

    final public int getType() {
        return type;
    }

    final public String getName() {
        return name;
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
        if(name == null || name ==""){
            return null;
        }
        for (DictionaryTypeEnum aparameter : values()) {
            if (aparameter.getName().equals(name)) {
                return aparameter;
            }
        }
        logger.error("XML name名称的字典不再枚举范围内, name:" + name);
        return null;
    }

}
