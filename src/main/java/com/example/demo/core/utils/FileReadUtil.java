package com.example.demo.core.utils;

import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;

public class FileReadUtil {
    /**
     * 获取项目配置文件路径， 默认从项目根目录conf 文件中获取
     * @return
     * @throws FileNotFoundException
     */
    public static String getConfigDir()throws FileNotFoundException {
        String dicName = "conf";
        String userDir = System.getProperty("user.dir") + File.separator + dicName + File.separator;

        File file = new File(userDir);
        if(!file.exists()){
            userDir = new File(ResourceUtils.getURL("classpath:" + dicName).getPath())
                    + File.separator;

        }
        System.out.println("=======================base dir path:" + userDir);
        return userDir;
    }
}
