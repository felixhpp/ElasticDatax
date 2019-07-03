package com.example.demo.jobs.plugins;

import java.util.Collection;

/**
 * 数据同步插件
 */
public interface Plugin {
    Collection<PluginConfigSpec<?>> configSchema();
    default String getName(){
        DbDataxPlugin annotation = getClass().getDeclaredAnnotation(DbDataxPlugin.class);
        return (annotation != null && !annotation.name().equals(""))
                ? annotation.name()
                : getClass().getName();
    }

    String getId();
}
