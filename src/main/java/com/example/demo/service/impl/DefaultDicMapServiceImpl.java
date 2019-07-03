package com.example.demo.service.impl;

import com.example.demo.core.bean.ConvertConfigBean;
import com.example.demo.core.entity.ESBulkModel;
import com.example.demo.core.enums.DictionaryTypeEnum;
import com.example.demo.core.enums.ElasticTypeEnum;
import com.example.demo.core.utils.*;
import com.example.demo.jobs.ConvertPipeline;
import com.example.demo.entity.DictionaryMap;
import com.example.demo.service.DefaultDicMapService;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认的字典获取
 * @author felix
 */
@Service
public class DefaultDicMapServiceImpl implements DefaultDicMapService {
    @Autowired
    private com.example.demo.mapper.cache.DictionaryMapMapper dictionaryMapMapper;

    @Autowired
    private com.example.demo.mapper.cache.DicOrderItemMappper dicOrderItemMappper;
    private static EhCacheCacheManager cacheCacheManager= SpringUtils.getBean(EhCacheCacheManager.class);
    private static CacheManager cacheManager = cacheCacheManager.getCacheManager();
    private static final String DicBusinessFieldcode = "00001";
    private static final Logger log = LoggerFactory.getLogger(DefaultDicMapServiceImpl.class);

    private static ConcurrentHashMap<String, Cache> cacheMap = new ConcurrentHashMap<>();
    @Autowired
    private ConvertConfigBean mapperBean;
    /**
     * 通过code获取字典
     * @param code
     * @return
     * @throws Exception
     */
    public String getDicNnameByCode(String code, DictionaryTypeEnum typeEnum) throws Exception {
        if(code == null || code == ""){
            return "";
        }

        DictionaryMap dictionaryMap = null;
        String name = "";
        String cacheName = typeEnum.getCacheName();
        Cache cache = cacheMap.get(cacheName);
        if(cache == null){
            cache = cacheManager.getCache(cacheName);
            if(cache == null){
                log.error("******没有发现" + cacheName + "名称的缓存");
                return "";
            }
            cacheMap.put(cacheName, cache);
        }

        //去除code中的域名
        if(code.startsWith(DicBusinessFieldcode)){
            code = code.replace(DicBusinessFieldcode, "");
        }

        String key = typeEnum.getCachePrefix() + code;
        Element value = cache != null ? cache.get(key): null;

        if(value != null){
            Object o = value.getObjectValue();
            name = o != null ? o.toString() : "";
        }else {
            log.error("*******" + typeEnum.getCacheName() + "缓存里没有"+key+", 从数据库拿数据");
            String newCode = DicBusinessFieldcode + "_" + code;

            switch (typeEnum){
                case DIAGNOSE_NAME:
                    dictionaryMap = dictionaryMapMapper.getDiagnoseByCode(newCode);
                    break;
                case DEPARTMENT:
                    dictionaryMap = dictionaryMapMapper.getDeptByCode(newCode);
                    break;
                case SEX:
                    dictionaryMap = dictionaryMapMapper.getSexByCode(newCode);
                    break;
                case MARITAL:
                    dictionaryMap = dictionaryMapMapper.getMaritalByCode(newCode);
                    break;
                case NATIONAL:
                    dictionaryMap = dictionaryMapMapper.getNationByCode(newCode);
                    break;
                case HOSPITAL:
                    dictionaryMap = dictionaryMapMapper.getHospitalByCode(newCode);
                    break;
                case DIAGNOSE_TYPE:
                    dictionaryMap = dictionaryMapMapper.getDiagnoseTypeByCode(newCode);
                    break;
                case ADM_TYPE:
                    dictionaryMap = dictionaryMapMapper.getAdmTypeByCode(newCode);
                    break;
                case AdmStatus:
                    dictionaryMap = dictionaryMapMapper.getAdmStatusByCode(newCode);
                    break;
                case LisItem:
                    dictionaryMap = dictionaryMapMapper.getLisItemByCode(newCode);
                    break;
                case ORDSER_ITEM:           //医嘱相关字典， 使用dicOrderItemMappper
                    dictionaryMap = dicOrderItemMappper.getOrdItemNameByCode(newCode);
                    break;
                case ORDER_CATEAGE:     //医嘱大分类
                    dictionaryMap = dicOrderItemMappper.getOrdCategoryByCode(newCode);
                    break;
                case ORDER_TYPE:
                    dictionaryMap = dicOrderItemMappper.getPriorityByCode(newCode);
                    break;
                case ORDER_STATUS:
                    dictionaryMap = dicOrderItemMappper.getOrderStatusByCode(newCode);
                    break;
                case DURATION:
                    dictionaryMap = dicOrderItemMappper.getDurationByCode(newCode);
                    break;
                case FREQ:
                    dictionaryMap = dicOrderItemMappper.getFreqByCode(newCode);
                    break;
                case PHDrgMaterial:         // 药学项
                    dictionaryMap = dicOrderItemMappper.getPHDrgMaterialItmByCode(newCode);
                    break;
                case InstrUsage:            // 用药途径
                    dictionaryMap = dicOrderItemMappper.getInstrByCode(newCode);
                    break;
                case OrdChildCategory:
                    dictionaryMap = dicOrderItemMappper.getChildCategoryByCode(newCode);
                    break;
                case PHCGeneric:        // 药品通用名
                    // 通用名需要把域名去掉
                    dictionaryMap = dicOrderItemMappper.getPHCGenericByCode(code);
                    break;
                case PHCGoods:          // 药品商品名
                    // 商品名需要把域名去掉
                    dictionaryMap = dicOrderItemMappper.getPHCGoodsByCode(code);
                    break;
            }
            if(dictionaryMap != null){

                String curDicName = dictionaryMap.getDicName();
                if(curDicName.startsWith(DicBusinessFieldcode)){
                    curDicName = curDicName.replace(DicBusinessFieldcode, "");
                }

                cache.put(new Element(key, curDicName));
                name = curDicName;
            }else { // 防止字典表没有时重复查询问题
                cache.put(new Element(key, ""));
                name = "";
            }
            cache.flush();
        }

        return name;
    }

    /**
     * 获取全部性别字段
     * @return
     */
    public List<DictionaryMap> getAllDicMap(DictionaryTypeEnum typeEnum){
        List<DictionaryMap> dictionaryMaps = null;
        switch (typeEnum){
            case DEPARTMENT:
                dictionaryMaps = dictionaryMapMapper.getAllDept();
                break;
            case SEX:
                dictionaryMaps = dictionaryMapMapper.getAllSex();
                break;
            case MARITAL:
                dictionaryMaps = dictionaryMapMapper.getAllMarital();
                break;
            case NATIONAL:
                dictionaryMaps = dictionaryMapMapper.getAllNation();
                break;
            case HOSPITAL:
                dictionaryMaps = dictionaryMapMapper.getAllHospital();
                break;
            case DIAGNOSE_NAME:
                dictionaryMaps = dictionaryMapMapper.getAllDiagnose();
                break;
            case DIAGNOSE_TYPE:
                dictionaryMaps = dictionaryMapMapper.getAllDiagnoseType();
                break;
            case ADM_TYPE:
                dictionaryMaps = dictionaryMapMapper.getAllAdmType();
                break;
            case AdmStatus:
                dictionaryMaps = dictionaryMapMapper.getAllAdmStatus();
                break;
            case LisItem:
                dictionaryMaps = dictionaryMapMapper.getAllLisItem();
                break;
            case ORDSER_ITEM:           //医嘱相关字典， 使用dicOrderItemMappper
                dictionaryMaps = dicOrderItemMappper.getAllOrdItemName();
                break;
            case ORDER_CATEAGE:     //医嘱大分类
                dictionaryMaps = dicOrderItemMappper.getAllOrdCategory();
                break;
            case ORDER_TYPE:
                dictionaryMaps = dicOrderItemMappper.getAllPriority();
                break;
            case ORDER_STATUS:
                dictionaryMaps = dicOrderItemMappper.getAllOrderStatus();
                break;
            case DURATION:
                dictionaryMaps = dicOrderItemMappper.getAllDuration();
                break;
            case FREQ:
                dictionaryMaps = dicOrderItemMappper.getAllFreq();
                break;
            case PHDrgMaterial:
                dictionaryMaps = dicOrderItemMappper.getAllPHDrgMaterialItm();
                break;
            case InstrUsage:
                dictionaryMaps = dicOrderItemMappper.getAllInstr();
                break;
            case OrdChildCategory:
                dictionaryMaps = dicOrderItemMappper.getAllChildCategory();
                break;
            case PHCGeneric:        // 药品通用名
                dictionaryMaps = dicOrderItemMappper.getAllPHCGeneric();
                break;
            case PHCGoods:  // 商品名
                dictionaryMaps = dicOrderItemMappper.getAllPHCGoods();
                break;
        }

        return dictionaryMaps;
    }

    public List<ESBulkModel> test(List<Map<String, Object>> maps, ElasticTypeEnum elasticTypeEnum) throws Exception {
        List<ESBulkModel> newObj = ConvertPipeline.convertToBulkModels(elasticTypeEnum, maps,
                mapperBean.getOnMapper());

        return newObj;
    }

}
