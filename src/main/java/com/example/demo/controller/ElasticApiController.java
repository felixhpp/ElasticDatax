package com.example.demo.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONReader;
import com.example.demo.core.entity.*;
import com.example.demo.service.ElasticBulkService;
import com.example.demo.core.utils.ResultUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * elasticsearch api
 * @author felix
 */
@RequestMapping(path = "indice", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@RestController
@Api("Elasticsearch相关的api")
public class ElasticApiController {

    private Logger logger = LoggerFactory.getLogger(ElasticApiController.class);

    @Autowired
    ElasticBulkService elasticBulkService;

    /**
     * 集成平台API调用接口，批量导入ES数据
     *
     * @param content
     * @return
     */
    @ApiOperation(value = "批量导入ES数据", notes = "批量导入ES数据")
    @ApiImplicitParam(name = "content", value = "请求json字符串", paramType = "String", required = true, dataType = "String")
    @PostMapping(path = "bulk")
    public RestResult bulk(String content) {
        long startTime = System.currentTimeMillis();
        BulkRequestBody requetsBody = JSONObject.parseObject(content, BulkRequestBody.class);
        String dataStr = requetsBody.getData();
        String theme = requetsBody.getTheme();
        BulkResponseBody result = new BulkResponseBody();
        try {
            if (StringUtils.isEmpty(dataStr)) {
                result.setResultCode("-1");
                result.setResultContent("content参数中data信息为null");
            }
            if (StringUtils.isEmpty(theme)) {
                result.setResultCode("-1");
                result.setResultContent("content参数中theme信息为null");
            }

            List<Map<String, Object>> bulkData = JSONArray.parseObject(dataStr, ArrayList.class);
            result = elasticBulkService.bulk(theme, bulkData);
            long endTime = System.currentTimeMillis();
            logger.info("====bulk [" + theme + "] finish：" + result.getResultContent() + "  tool:" + (endTime - startTime) + "ms");
        } catch (Exception e) {
            result.setResultCode("-1");
            result.setResultContent("请求异常，错误信息:" + e.getMessage());
        }

        return ResultUtil.success(result);
    }

    /**
     * 批量导入数据，数据通过写入body的当时传输
     *
     * @param content
     * @return
     */
    @PostMapping(path = "bulkBody")
    public RestResult bulk(@Valid @RequestBody BodyContent content) {
        BulkRequestBody requetsBody = JSONObject.parseObject(content.getContent(), BulkRequestBody.class);
        String dataStr = requetsBody.getData();
        String theme = requetsBody.getTheme();
        List<Map<String, Object>> bulkData = strToMap(dataStr);
        BulkResponseBody result = elasticBulkService.bulk(theme, bulkData);
        return ResultUtil.success(result);
    }

    /**
     * 添加单
     *
     * @param body
     * @return
     */
    @PostMapping(path = "bulk/casenote")
    public RestResult bulk(@RequestBody List<BulkCaseRequestBody> body) {
        if(body.size() == 0) {
            return ResultUtil.error("body is null");
        }

        long startTime = System.currentTimeMillis();
        BulkResponseBody responseBody = elasticBulkService.bulkCase(body);
        long endTime = System.currentTimeMillis();
        logger.info("=====bulk [ " + body.get(0).getDocumentTypeDesc() + " ],tool：" + (endTime - startTime) + "ms，message:" + responseBody.getResultContent());
        return ResultUtil.success(responseBody);
    }

    @ApiOperation(value = "根据登记号获取病人基本信息", notes = "根据登记号获取病人基本信息")
    @ApiImplicitParam(name = "regNo", value = "登记号", paramType = "path", required = true, dataType = "String")
    @GetMapping(path = "getPatient/{regNo}")
    public RestResult getPatientByRegNo(@PathVariable(name = "regNo") String regNo) throws IOException {
        return ResultUtil.success(elasticBulkService.getPatientByRegNo(regNo));
    }

    /**
     * 将json数组字符串解析成List<Map<String, Object>>， 效率高过fastjson JSONArray
     *
     * @param dataJsonStr
     * @return
     */
    private List<Map<String, Object>> strToMap(String dataJsonStr) {
        JSONReader reader = new JSONReader(new StringReader(dataJsonStr));//已流的方式处理，这里很快
        reader.startArray();
        List<Map<String, Object>> rsList = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = null;
        int i = 0;
        while (reader.hasNext()) {
            i++;
            reader.startObject();//这边反序列化也是极速
            map = new HashMap<String, Object>();
            while (reader.hasNext()) {
                String arrayListItemKey = reader.readString();
                String arrayListItemValue = reader.readObject().toString();
                map.put(arrayListItemKey, arrayListItemValue);
            }
            rsList.add(map);
            reader.endObject();
        }
        reader.endArray();

        return rsList;
    }
}
