package com.example.demo.service;

import com.example.demo.core.entity.BulkCaseRequestBody;
import com.example.demo.core.entity.BulkResponseBody;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Elasticsearch 服务
 * @author felix
 */
public interface ElasticBulkService {
    BulkResponseBody bulk(String theme, List<Map<String, Object>> dataList);
    BulkResponseBody bulk(String theme, String dataJsonStr);

    /**
     * 批量向ES导入自动补全数据
     * @return
     */
    BulkResponseBody bulkSuggestion();

    String getPatientByRegNo(String regNo) throws IOException;

    BulkResponseBody bulkCase(List<BulkCaseRequestBody> caseRequestBodies);

}
