package com.example.demo.monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.websocket.Session;


public class TailLogUtil extends Thread {

    private final int READ_SIZE = 128;		//日志文件默认读取大小，单位：KB
    private final int TIME_MILLS = 100;		//日志读取返回时间
    private final int READ_LINES = 10;		//日志读取每次返回的行数

    private BufferedReader reader;

    private Session session;

    private int lineCount = 0;

    public TailLogUtil(InputStream in, Session session) {
        this.reader = new BufferedReader(new InputStreamReader(in));
        this.session = session;
    }

    @Override
    public void run() {
        StringBuilder reportContent = new StringBuilder();
        long startTime = getTime();
        long tempMill;
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                reportContent.append(line).append("\r\n");
                tempMill = getTime();
                // 将实时日志通过WebSocket发送给客户端
                if (lineCount >= READ_LINES || tempMill - startTime >= TIME_MILLS) {
                    session.getBasicRemote().sendText(reportContent.toString());
                    startTime = tempMill;
                    lineCount = 0;
                    reportContent.setLength(0);
                } else {
                    lineCount++;
                }
            }
        } catch (IOException e) {
            System.out.println("thead raed file error:" + e);
        }
    }

    private long getTime() {
        return System.currentTimeMillis();
    }

}