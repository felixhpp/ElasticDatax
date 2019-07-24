package com.example.demo.jobs.hbase;

import com.example.demo.core.entity.ESBulkModel;
import org.hibernate.validator.constraints.EAN;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量导入HBase model
 *
 * @author felix
 */
public final class HBaseBulkModel {
    private String rowKey;

    private String table;

    private List<ESBulkModel> models;

    public HBaseBulkModel(String rowKey, String table){
        this.rowKey = rowKey;
        this.table = table;
        this.models = new ArrayList<>();
    }

    public String getRowKey() {
        return rowKey;
    }

    public void setRowKey(String rowKey) {
        this.rowKey = rowKey;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public List<ESBulkModel> getModels() {
        return models;
    }

    public void setModels(List<ESBulkModel> models) {
        this.models = models;
    }
}
