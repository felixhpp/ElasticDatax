package com.example.demo.service;

import java.util.concurrent.Future;

public interface TaskService {
    public void excutVoidTask(int i);
    public Future<String> excuteValueTask(int i) throws InterruptedException;
}
