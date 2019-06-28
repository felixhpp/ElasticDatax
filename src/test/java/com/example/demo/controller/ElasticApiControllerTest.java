package com.example.demo.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.junit.Assert.*;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test") // 加载特定配置文件
@AutoConfigureMockMvc
public class ElasticApiControllerTest {
    @Autowired
    private MockMvc mvc;

    @Test
    public void getMapping() {
    }

    @Test
    public void getMapping1() {
    }

    @Test
    public void getMapping2() {
    }

    @Test
    public void bulkTest() {
    }

    @Test
    public void bulk() {
    }

    @Test
    public void getPatientByRegNo() {
    }

    @Test
    public void convertTest() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/indice/convertTest");
        //request.param("name","laiminghai");
        mvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }
}