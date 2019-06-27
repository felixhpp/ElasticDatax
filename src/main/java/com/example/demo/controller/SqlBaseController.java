package com.example.demo.controller;

import com.example.demo.core.entity.RestResult;
import com.example.demo.core.utils.ResultUtil;
import com.example.demo.service.SqlBaseMapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/sqltest")
public class SqlBaseController {
    @Autowired
    SqlBaseMapperService sqlBaseMapperService;

    @GetMapping(path = "exeselect")
    public RestResult exeSelectSql(String sql) {
        return ResultUtil.success(sqlBaseMapperService.select(sql));
    }
}
