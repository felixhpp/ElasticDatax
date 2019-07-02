package com.example.demo.service;

import java.util.LinkedHashMap;
import java.util.List;

public interface SqlBaseMapperService {
     List<LinkedHashMap<String, Object>> select(String sql);
}
