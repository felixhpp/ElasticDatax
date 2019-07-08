package com.example.demo.jobs.converter;

import com.example.demo.core.utils.SpringUtils;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.omg.CORBA.PUBLIC_MEMBER;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * ES 输出源
 */
public class ElasticSearchOutput extends Output {
    private static final String outputType = "elasticsearch";

    private BulkProcessor bulkProcessor;
    /**
     * 索引名称
     */
    private String index;
    /**
     * 类型名称
     */
    private String type;

    /**
     * id
     */
    private String id;

    /**
     * parent
     */
    private String parent;

    /**
     * routing
     */
    private String routing;

    /**
     * sourceMap
     */
    private Map sourceMap;

    public ElasticSearchOutput() {
        super(outputType);

        bulkProcessor = SpringUtils.getBean(BulkProcessor.class);
    }
    public ElasticSearchOutput(String index, String type) {
        super(outputType);
        this.index = index;
        this.type = type;
        this.bulkProcessor = SpringUtils.getBean(BulkProcessor.class);
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getRouting() {
        return routing;
    }

    public void setRouting(String routing) {
        this.routing = routing;
    }

    public Map getSourceMap() {
        return sourceMap;
    }

    public void setSourceMap(Map sourceMap) {
        this.sourceMap = sourceMap;
    }

    /**
     * 重写父类execute()
     */
    @Override
    public void execute(){
        IndexRequest request = new IndexRequest(this.index, this.type, this.id)
                .source(this.sourceMap);
        if(!StringUtils.isEmpty(this.parent)){
            request.parent(this.parent);
        }
        if(bulkProcessor != null){
            bulkProcessor.add(request);
        }
    }
}
