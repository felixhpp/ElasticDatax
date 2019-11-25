package com.example.demo.service;

import com.example.demo.core.entity.BulkCaseRequestBody;
import com.example.demo.core.entity.BulkResponseBody;
import com.example.demo.core.enums.ElasticTypeEnum;
import com.example.demo.jobs.analysis.CaseRecodrXmlBean;

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

    BulkResponseBody bulkCase(List<BulkCaseRequestBody> caseRequestBodies);

    BulkResponseBody bulkCaseTest(CaseRecodrXmlBean bean, ElasticTypeEnum typeEnum);

}
