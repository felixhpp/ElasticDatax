package com.example.demo.mapper.JdbcTempleat;

import com.example.demo.entity.Department;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 行映射
 */
public class DeptRowMapper implements RowMapper<Department> {

    @Override
    public Department mapRow(ResultSet resultSet, int i) throws SQLException {
        Department department = new Department();
        department.setCode(resultSet.getString("code"));
        department.setName(resultSet.getString("descname"));
        return department;
    }

}
