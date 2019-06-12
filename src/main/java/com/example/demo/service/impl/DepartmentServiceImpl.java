package com.example.demo.service.impl;

import com.example.demo.dao.DepartmentDao;
import com.example.demo.entity.Department;
import com.example.demo.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class DepartmentServiceImpl implements DepartmentService {
    @Autowired
    DepartmentDao departmentDao;

    @Cacheable(key="'dept_'+#code",value="deptCache")
    public Department getByCode(String code) throws Exception {
        System.err.println("缓存里没有"+code+",所以这边没有走缓存，从数据库拿数据");
        return departmentDao.getByCode(code);
    }
}
