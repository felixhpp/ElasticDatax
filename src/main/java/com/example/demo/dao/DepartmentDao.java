package com.example.demo.dao;

import com.example.demo.entity.Department;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DepartmentDao {
//    @Autowired
//    @Qualifier("cacheJdbcTemplate")
//    JdbcTemplate jdbcTemplate;

    @Autowired
    private com.example.demo.mapper.primary.DeptMapper deptMapper1;

//    @Autowired
//    private com.example.demo.mapper.cache.DeptMapper deptMapper;

//    @Transactional(readOnly = true)
//    public Department getByCode(String code) throws  Exception{
//        String sql = "select * from dept where code=?";
//        List<Department> departmentList = jdbcTemplate.query(sql, new Object[]{code}, new DeptRowMapper());
//        Department department = null;
//        if(!departmentList.isEmpty()){
//            department = departmentList.get(0);
//        }
//        return department;
//    }
    public Department getByCode(String code) throws  Exception{
        //deptMapper1.getByCode(code);
        return deptMapper1.getByCode(code);
    }
}

