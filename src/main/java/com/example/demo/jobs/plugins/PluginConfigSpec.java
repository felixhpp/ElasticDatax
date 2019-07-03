package com.example.demo.jobs.plugins;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public final class PluginConfigSpec<T> {
    /**
     * 插件名称
     */
    private final String name;

    /**
     * 插件类型
     */
    private final Class<T> type;

    /**
     * 是否必须
     */
    private final boolean required;

    /**
     * 默认值
     */
    private final T defaultValue;

    private String rawDefaultValue;

    private final Collection<PluginConfigSpec<?>> children;

    private PluginConfigSpec(final String name, final Class<T> type,
                             final T defaultValue, final boolean deprecated, final boolean required) {
        this(name, type, defaultValue, deprecated, required, Collections.emptyList());
    }

    private PluginConfigSpec(final String name, final Class<T> type,
                             final T defaultValue, final boolean deprecated, final boolean required,
                             final Collection<PluginConfigSpec<?>> children) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.required = required;
        if (!children.isEmpty() && !Map.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException("Only map type settings can have defined children.");
        }
        this.children = children;
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    public static PluginConfigSpec<Map<String, Object>> hashSetting(final String name) {
        return new PluginConfigSpec(name, Map.class, null, false, false);
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    public static PluginConfigSpec<Map<String, Object>> hashSetting(final String name, Map<String, Object> defaultValue, boolean deprecated, boolean required) {
        return new PluginConfigSpec(name, Map.class, defaultValue, deprecated, required);
    }

    public static PluginConfigSpec<String> stringSetting(final String name) {
        return new PluginConfigSpec<>(
                name, String.class, null, false, false
        );
    }

    public static PluginConfigSpec<String> stringSetting(final String name, final String defaultValue) {
        return new PluginConfigSpec<>(
                name, String.class, defaultValue, false, false
        );
    }

    public static PluginConfigSpec<String> requiredStringSetting(final String name) {
        return new PluginConfigSpec<>(name, String.class, null, false, true);
    }
}

