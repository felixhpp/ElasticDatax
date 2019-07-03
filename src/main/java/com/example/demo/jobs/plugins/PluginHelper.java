package com.example.demo.jobs.plugins;

import java.util.*;

/**
 * plugins 帮助类
 * @author felix
 */
public final class PluginHelper {
    /**
     * 增加字段
     */
    public static final PluginConfigSpec<Map<String, Object>> ADD_FIELD_CONFIG =
            PluginConfigSpec.hashSetting("addField");
    /**
     * 设置id字段
     */
    public static final PluginConfigSpec<String> ID_CONFIG =
            PluginConfigSpec.stringSetting("idField");

    /**
     * 设置parent字段
     */
    public static final PluginConfigSpec<String> PARENT_CONFIG =
            PluginConfigSpec.stringSetting("parentField");

    public static Collection<PluginConfigSpec<?>> commonInputSettings() {
        return Arrays.asList(ADD_FIELD_CONFIG, ID_CONFIG, PARENT_CONFIG);
    }

    public static Collection<PluginConfigSpec<?>> commonInputSettings(Collection<PluginConfigSpec<?>> settings) {
        return combineSettings(settings, commonInputSettings());
    }

    public static Collection<PluginConfigSpec<?>> commonOutputSettings() {
        return Arrays.asList(ID_CONFIG, PARENT_CONFIG);
    }

    public static Collection<PluginConfigSpec<?>> commonOutputSettings(Collection<PluginConfigSpec<?>> settings) {
        return combineSettings(settings, commonOutputSettings());
    }

    public static Collection<PluginConfigSpec<?>> commonFilterSettings() {
        return Arrays.asList(ADD_FIELD_CONFIG, ID_CONFIG);
    }

    public static Collection<PluginConfigSpec<?>> commonFilterSettings(Collection<PluginConfigSpec<?>> settings) {
        return combineSettings(settings, commonFilterSettings());
    }

    @SuppressWarnings("rawtypes")
    private static Collection<PluginConfigSpec<?>> combineSettings(
            Collection<PluginConfigSpec<?>> providedSettings,
            Collection<PluginConfigSpec<?>> commonSettings) {
        List<PluginConfigSpec<?>> settings = new ArrayList<>(providedSettings);
        for (PluginConfigSpec pcs : commonSettings) {
            if (!settings.contains(pcs)) {
                settings.add(pcs);
            }
        }
        return settings;
    }
}
