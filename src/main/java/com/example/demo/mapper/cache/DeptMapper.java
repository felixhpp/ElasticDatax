package com.example.demo.mapper.cache;

import com.example.demo.entity.Department;
import org.springframework.stereotype.Component;

import java.util.List;

public interface DeptMapper {
    List<Department> getAll();

    Department getByCode(String code) throws  Exception;
}
