package org.teachfx.antlr4.common.visualization.ui;

import java.awt.Color;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 主题管理器
 * 支持外观定制和主题切换
 */
public class ThemeManager {
    public enum Theme {
        LIGHT("浅色主题"),
        DARK("深色主题"),
        EDUCATIONAL("教育主题"),
        HIGH_CONTRAST("高对比度主题");
        
        private final String description;
        
        Theme(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    private Theme currentTheme;
    private final ConcurrentHashMap<Theme, ThemeConfig> themes;
    
    public ThemeManager() {
        this.currentTheme = Theme.LIGHT;
        this.themes = new ConcurrentHashMap<>();
        initializeThemes();
    }
    
    private void initializeThemes() {
        // 浅色主题
        themes.put(Theme.LIGHT, new ThemeConfig(
            Color.WHITE,                    // 背景色
            Color.BLACK,                    // 前景色
            new Color(240, 240, 240),      // 面板背景
            new Color(51, 153, 255),       // 高亮色
            new Color(200, 200, 200),      // 边框色
            new Color(0, 120, 215),        // 链接色
            new Color(255, 255, 200),      // 教育高亮
            new Color(200, 255, 200)       // 成功色
        ));
        
        // 深色主题
        themes.put(Theme.DARK, new ThemeConfig(
            new Color(43, 43, 43),        // 背景色
            Color.WHITE,                    // 前景色
            new Color(60, 60, 60),         // 面板背景
            new Color(100, 149, 237),      // 高亮色
            new Color(100, 100, 100),      // 边框色
            new Color(66, 165, 245),       // 链接色
            new Color(255, 204, 0),        // 教育高亮
            new Color(76, 175, 80)         // 成功色
        ));
        
        // 教育主题
        themes.put(Theme.EDUCATIONAL, new ThemeConfig(
            new Color(255, 253, 240),      // 背景色 (暖色)
            new Color(33, 37, 41),         // 前景色
            new Color(248, 245, 240),      // 面板背景
            new Color(255, 193, 7),        // 高亮色 (醒目)
            new Color(222, 184, 135),      // 边框色
            new Color(0, 123, 255),        // 链接色
            new Color(255, 152, 0),        // 教育高亮
            new Color(40, 167, 69)         // 成功色
        ));
        
        // 高对比度主题
        themes.put(Theme.HIGH_CONTRAST, new ThemeConfig(
            Color.WHITE,                    // 背景色
            Color.BLACK,                    // 前景色
            Color.WHITE,                    // 面板背景
            Color.RED,                      // 高亮色
            Color.BLACK,                    // 边框色
            Color.BLUE,                     // 链接色
            Color.YELLOW,                   // 教育高亮
            Color.GREEN                     // 成功色
        ));
    }
    
    public Theme getCurrentTheme() {
        return currentTheme;
    }
    
    public void setCurrentTheme(Theme theme) {
        if (theme != null && themes.containsKey(theme)) {
            this.currentTheme = theme;
        }
    }
    
    public ThemeConfig getThemeConfig() {
        return themes.get(currentTheme);
    }
    
    public Color getBackgroundColor() {
        return getThemeConfig().backgroundColor;
    }
    
    public Color getForegroundColor() {
        return getThemeConfig().foregroundColor;
    }
    
    public Color getPanelBackgroundColor() {
        return getThemeConfig().panelBackgroundColor;
    }
    
    public Color getHighlightColor() {
        return getThemeConfig().highlightColor;
    }
    
    public Color getBorderColor() {
        return getThemeConfig().borderColor;
    }
    
    public Color getLinkColor() {
        return getThemeConfig().linkColor;
    }
    
    public Color getEducationalHighlightColor() {
        return getThemeConfig().educationalHighlightColor;
    }
    
    public Color getSuccessColor() {
        return getThemeConfig().successColor;
    }
    
    public void addCustomTheme(Theme theme, ThemeConfig config) {
        themes.put(theme, config);
    }
    
    public boolean hasTheme(Theme theme) {
        return themes.containsKey(theme);
    }
    
    public static class ThemeConfig {
        public final Color backgroundColor;
        public final Color foregroundColor;
        public final Color panelBackgroundColor;
        public final Color highlightColor;
        public final Color borderColor;
        public final Color linkColor;
        public final Color educationalHighlightColor;
        public final Color successColor;
        
        public ThemeConfig(Color backgroundColor, Color foregroundColor, Color panelBackgroundColor,
                        Color highlightColor, Color borderColor, Color linkColor,
                        Color educationalHighlightColor, Color successColor) {
            this.backgroundColor = backgroundColor;
            this.foregroundColor = foregroundColor;
            this.panelBackgroundColor = panelBackgroundColor;
            this.highlightColor = highlightColor;
            this.borderColor = borderColor;
            this.linkColor = linkColor;
            this.educationalHighlightColor = educationalHighlightColor;
            this.successColor = successColor;
        }
    }
}