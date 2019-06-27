package com.example.demo.mapper.primary;

import com.example.demo.entity.DictionaryMap;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DictionaryMapMapper {
    List<DictionaryMap> getAll();
    DictionaryMap getByCode(String code) throws  Exception;

}
