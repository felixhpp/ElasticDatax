package com.example.demo.bean;

import java.io.File;

public class ProjectPath {
    //获取项目的根路径
    public final static String classPath;

    static {
        //获取的是classpath路径，适用于读取resources下资源
        classPath = ProjectPath.class.getClassLoader().getResource("").getPath();
    }
    /**
     * 项目根目录
     */
    public static String getRootPath() {
        return RootPath("");
    }

    /**
     * 自定义追加路径
     * ProjectPath.getRootPath("userImg/test.txt");
     */
    public static String getRootPath(String u_path) {
        return RootPath(u_path);
    }

    /**
     * 私有处理方法
     *
     */
    private static String RootPath(String u_path) {
        String rootPath = "";
        //windows下
        if ("\\".equals(File.separator)) {
            rootPath = classPath + u_path;
            rootPath = rootPath.replaceAll("/", "\\\\");
            if (rootPath.substring(0, 1).equals("\\")) {
                rootPath = rootPath.substring(1);
            }
        }
        //linux下
        if ("/".equals(File.separator)) {
            //System.out.println(classPath);
            rootPath = classPath + u_path;
            rootPath = rootPath.replaceAll("\\\\", "/");
        }
        return rootPath;
    }

}
