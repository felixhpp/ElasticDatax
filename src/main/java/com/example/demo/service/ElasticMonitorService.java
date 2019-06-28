package com.example.demo.service;

import io.searchbox.client.JestResult;

import java.util.List;

public interface ElasticMonitorService {
    JestResult health();
    JestResult nodesStats(List<String> nodes);
    JestResult nodesInfo(List<String> nodes);
}
