package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LogMonitorController {
    @GetMapping(value = "log/monitor")
    public String home() {
        return "/web/monitor/logmonitor";
    }
}
