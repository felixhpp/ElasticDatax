package com.example.demo.mapper.JdbcTempleat;

import com.example.demo.entity.DictionaryMap;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 行映射
 */
public class DeptRowMapper implements RowMapper<DictionaryMap> {

    @Override
    public DictionaryMap mapRow(ResultSet resultSet, int i) throws SQLException {
        DictionaryMap department = new DictionaryMap();
        department.setDicCode(resultSet.getString("code"));
        department.setDicName(resultSet.getString("descname"));
        return department;
    }

}
