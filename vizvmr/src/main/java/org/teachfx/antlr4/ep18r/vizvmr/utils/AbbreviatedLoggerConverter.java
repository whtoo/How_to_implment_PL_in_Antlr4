package org.teachfx.antlr4.ep18r.vizvmr.utils;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.PatternFormatter;
import org.apache.logging.log4j.core.pattern.PatternParser;

/**
 * 自定义Log4j2模式转换器，将logger名称的包名路径缩写为每个包的首字母
 *
 * 示例：
 *   org.teachfx.antlr4.ep18r.vizvmr.UnifiedVizVMRLauncher → o.t.a.e.v.UnifiedVizVMRLauncher
 *   org.teachfx.antlr4.ep18r.vizvmr.ui.javafx.RegisterView → o.t.a.e.v.u.j.RegisterView
 */
@Plugin(name = "AbbreviatedLoggerConverter", category = "Converter")
@ConverterKeys({ "al" })
public class AbbreviatedLoggerConverter extends LogEventPatternConverter {

    private static final String ABBREVIATED_LOGGER_CONVERTER = "AbbreviatedLoggerConverter";

    protected AbbreviatedLoggerConverter() {
        super(ABBREVIATED_LOGGER_CONVERTER, "logger");
    }

    /**
     * 获取AbbreviatedLoggerConverter实例
     */
    public static AbbreviatedLoggerConverter newInstance(final String[] options) {
        return new AbbreviatedLoggerConverter();
    }

    @Override
    public void format(LogEvent event, StringBuilder toAppendTo) {
        String loggerName = event.getLoggerName();
        String abbreviatedName = abbreviatePackageName(loggerName);
        toAppendTo.append(abbreviatedName);
    }

    /**
     * 将logger名称的包名路径缩写为每个包的首字母
     *
     * @param loggerName 完整的logger名称
     * @return 缩写后的logger名称
     */
    private String abbreviatePackageName(String loggerName) {
        if (loggerName == null || loggerName.isEmpty()) {
            return "";
        }

        int lastDotIndex = loggerName.lastIndexOf('.');

        if (lastDotIndex == -1) {
            // 没有包名，直接返回类名
            return loggerName;
        }

        String packageName = loggerName.substring(0, lastDotIndex);
        String className = loggerName.substring(lastDotIndex + 1);

        // 将包名按点分割，取每个包的首字母
        String[] packages = packageName.split("\\.");
        StringBuilder abbreviated = new StringBuilder();

        for (String pkg : packages) {
            if (pkg.length() > 0) {
                abbreviated.append(pkg.charAt(0));
                abbreviated.append('.');
            }
        }

        // 添加类名（全名）
        abbreviated.append(className);

        return abbreviated.toString();
    }
}
