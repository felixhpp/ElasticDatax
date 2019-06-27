package com.example.demo.service.impl;

import com.example.demo.mapper.primary.SqlBaseMapper;
import com.example.demo.service.SqlBaseMapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;

@Service
public class SqlBaseMapperServiceImpl implements SqlBaseMapperService {
    @Autowired
    SqlBaseMapper baseMapper;

    public List<LinkedHashMap<String, Object>> select(String sql){
        return baseMapper.select(sql);
    }
}
