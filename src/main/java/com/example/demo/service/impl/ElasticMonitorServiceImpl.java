package com.example.demo.service.impl;

import com.dhcc.csmsearch.elasticsearch.common.ElasticsearchManage;
import com.example.demo.service.ElasticBulkService;
import com.example.demo.service.ElasticMonitorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * ES监控服务
 * @author felix
 */
@Service
public class ElasticMonitorServiceImpl implements ElasticMonitorService {
    private Logger logger = LoggerFactory.getLogger(ElasticBulkService.class);

    @Resource
    private ElasticsearchManage elasticsearchManage;

}
