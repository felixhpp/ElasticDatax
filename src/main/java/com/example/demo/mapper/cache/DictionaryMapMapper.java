package com.example.demo.mapper.cache;

import com.example.demo.entity.DictionaryMap;
import net.sf.ehcache.transaction.xa.EhcacheXAException;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DictionaryMapMapper {
    // 性别
    List<DictionaryMap> getAllSex();
    DictionaryMap getSexByCode(String code) throws  Exception;

    // 婚育状态
    List<DictionaryMap> getAllMarital();
    DictionaryMap getMaritalByCode(String code) throws  Exception;

    // 民族
    List<DictionaryMap> getAllNation();
    DictionaryMap getNationByCode(String code) throws Exception;

    // 科室
    List<DictionaryMap> getAllDept();
    DictionaryMap getDeptByCode(String code) throws Exception;

    // 医院
    List<DictionaryMap> getAllHospital();
    DictionaryMap getHospitalByCode(String code) throws Exception;

    // 诊断
    List<DictionaryMap> getAllDiagnose();
    DictionaryMap getDiagnoseByCode(String code) throws Exception;

    // 诊断类型
    List<DictionaryMap> getAllDiagnoseType();
    DictionaryMap getDiagnoseTypeByCode(String code) throws Exception;

    // 就诊类型
    List<DictionaryMap> getAllAdmType();
    DictionaryMap getAdmTypeByCode(String code) throws Exception;

    // 就诊状态
    List<DictionaryMap> getAllAdmStatus();
    DictionaryMap getAdmStatusByCode(String code) throws Exception;

    // 检验项目
    List<DictionaryMap> getAllLisItem();
    DictionaryMap getLisItemByCode(String code) throws Exception;
}
