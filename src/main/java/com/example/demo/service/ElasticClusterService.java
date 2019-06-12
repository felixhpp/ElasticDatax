package com.example.demo.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public interface ElasticClusterService {
    public boolean createIndex(String index, String type) throws Exception;
    public boolean bulk(String index, String type, List<Object> dataList) throws IOException, InterruptedException;
}
