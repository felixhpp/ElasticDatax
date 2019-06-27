package com.example.demo.mapper.primary;

import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;

@Repository
public interface SqlBaseMapper {
    List<LinkedHashMap<String, Object>> select(String sql);
}
