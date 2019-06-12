package com.example.demo.service;

import com.example.demo.entity.Department;

public interface DepartmentService {
    public Department getByCode(String code) throws Exception;
}
