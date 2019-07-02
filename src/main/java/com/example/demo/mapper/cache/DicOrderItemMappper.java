package com.example.demo.mapper.cache;

import com.example.demo.entity.DictionaryMap;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DicOrderItemMappper {
    // 医嘱名称
    List<DictionaryMap> getAllOrdItemName();
    DictionaryMap getOrdItemNameByCode(String code) throws  Exception;

    // 医嘱类型
    List<DictionaryMap> getAllPriority();
    DictionaryMap getPriorityByCode(String code) throws  Exception;

    // 医嘱状态
    List<DictionaryMap> getAllOrderStatus();
    DictionaryMap getOrderStatusByCode(String code) throws  Exception;

    // 频次
    List<DictionaryMap> getAllFreq();
    DictionaryMap getFreqByCode(String code) throws  Exception;

    // 用途
    List<DictionaryMap> getAllDuration();
    DictionaryMap getDurationByCode(String code) throws  Exception;

    // 药学项字典
    List<DictionaryMap> getAllPHDrgMaterialItm();
    DictionaryMap getPHDrgMaterialItmByCode(String code) throws  Exception;

    //用药途径
    List<DictionaryMap> getAllInstr();
    DictionaryMap getInstrByCode(String code) throws Exception;

    // 医嘱大类
    List<DictionaryMap> getAllOrdCategory();
    DictionaryMap getOrdCategoryByCode(String code) throws  Exception;

    // 医嘱子分类
    List<DictionaryMap> getAllChildCategory();
    DictionaryMap getChildCategoryByCode(String code) throws Exception;

    // 药品通用名
    List<DictionaryMap> getAllPHCGeneric();
    DictionaryMap getPHCGenericByCode(String code) throws  Exception;

    // 药品商品名
    List<DictionaryMap> getAllPHCGoods();
    DictionaryMap getPHCGoodsByCode(String code) throws  Exception;
}
