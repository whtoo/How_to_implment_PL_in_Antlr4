package org.teachfx.antlr4.ep18r.vizvmr.util;

import java.io.*;
import java.util.Properties;

/**
 * 配置持久化工具
 * 保存和加载可视化配置
 */
public class ConfigPersistence {
    private static final String CONFIG_FILE = "vizvmr.properties";

    private final Properties properties;
    private final String configPath;

    public ConfigPersistence() {
        this.properties = new Properties();
        this.configPath = getConfigDirectory() + File.separator + CONFIG_FILE;
    }

    /**
     * 保存配置
     */
    public void saveConfig() throws IOException {
        try (OutputStream output = new FileOutputStream(configPath)) {
            properties.store(output, "VizVMR Configuration");
        }
    }

    /**
     * 加载配置
     */
    public void loadConfig() throws IOException {
        File configFile = new File(configPath);
        if (configFile.exists()) {
            try (InputStream input = new FileInputStream(configFile)) {
                properties.load(input);
            }
        }
    }

    /**
     * 设置字符串配置
     */
    public void setString(String key, String value) {
        properties.setProperty(key, value);
    }

    /**
     * 获取字符串配置
     */
    public String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * 设置整数配置
     */
    public void setInt(String key, int value) {
        properties.setProperty(key, String.valueOf(value));
    }

    /**
     * 获取整数配置
     */
    public int getInt(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * 设置布尔配置
     */
    public void setBoolean(String key, boolean value) {
        properties.setProperty(key, String.valueOf(value));
    }

    /**
     * 获取布尔配置
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }

    /**
     * 获取配置文件路径
     */
    public String getConfigPath() {
        return configPath;
    }

    /**
     * 清空配置
     */
    public void clear() {
        properties.clear();
    }

    private String getConfigDirectory() {
        String userHome = System.getProperty("user.home");
        String configDir = userHome + File.separator + ".vizvmr";
        File dir = new File(configDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return configDir;
    }

    // ==================== 常用配置键 ====================

    public static final String KEY_WINDOW_WIDTH = "window.width";
    public static final String KEY_WINDOW_HEIGHT = "window.height";
    public static final String KEY_WINDOW_MAXIMIZED = "window.maximized";
    public static final String KEY_REMEMBER_BREAKPOINTS = "debug.rememberBreakpoints";
    public static final String KEY_MAX_HISTORY_SIZE = "history.maxSize";
    public static final String KEY_AUTO_REFRESH = "ui.autoRefresh";
    public static final String KEY_REFRESH_INTERVAL = "ui.refreshInterval";

    // ==================== 默认配置 ====================

    public void setDefaults() {
        setInt(KEY_WINDOW_WIDTH, 1200);
        setInt(KEY_WINDOW_HEIGHT, 800);
        setBoolean(KEY_WINDOW_MAXIMIZED, true);
        setBoolean(KEY_REMEMBER_BREAKPOINTS, true);
        setInt(KEY_MAX_HISTORY_SIZE, 1000);
        setBoolean(KEY_AUTO_REFRESH, true);
        setInt(KEY_REFRESH_INTERVAL, 100);
    }
}
