package com.example.demo.mapper.primary;

import com.example.demo.entity.Department;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

public interface DeptMapper {
    List<Department> getAll();

    Department getByCode(String code) throws  Exception;
}
