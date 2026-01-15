package org.teachfx.antlr4.ep18r.stackvm;

/**
 * 虚拟机日志记录器
 * 提供统一的日志接口，支持不同的日志级别和trace控制
 */
public class Logger {
    public enum Level {
        ERROR,
        WARN,
        INFO,
        DEBUG,
        TRACE
    }
    
    private static Level currentLevel = Level.INFO;
    private static boolean traceEnabled = false;
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    
    private final String className;
    
    private Logger(String className) {
        this.className = className;
    }
    
    /**
     * 获取指定类的日志记录器
     */
    public static Logger getLogger(Class<?> clazz) {
        return new Logger(clazz.getSimpleName());
    }
    
    /**
     * 获取指定名称的日志记录器
     */
    public static Logger getLogger(String name) {
        return new Logger(name);
    }
    
    /**
     * 设置全局日志级别
     */
    public static void setLevel(Level level) {
        currentLevel = level;
    }
    
    /**
     * 启用或禁用trace模式
     */
    public static void setTraceEnabled(boolean enabled) {
        traceEnabled = enabled;
        if (enabled && currentLevel.ordinal() < Level.DEBUG.ordinal()) {
            currentLevel = Level.DEBUG;
        }
    }
    
    /**
     * 检查trace模式是否启用
     */
    public static boolean isTraceEnabled() {
        return traceEnabled;
    }
    
    /**
     * 记录错误信息（总是显示）
     */
    public void error(String format, Object... args) {
        log(Level.ERROR, format, args);
    }
    
    /**
     * 记录错误信息和异常（总是显示）
     */
    public void error(String message, Throwable throwable) {
        log(Level.ERROR, message + ": " + throwable.getMessage());
        if (traceEnabled) {
            throwable.printStackTrace(System.err);
        }
    }
    
    /**
     * 记录警告信息（总是显示）
     */
    public void warn(String format, Object... args) {
        log(Level.WARN, format, args);
    }
    
    /**
     * 记录普通信息（默认显示）
     */
    public void info(String format, Object... args) {
        log(Level.INFO, format, args);
    }
    
    /**
     * 记录调试信息（仅在trace模式显示）
     */
    public void debug(String format, Object... args) {
        if (traceEnabled) {
            log(Level.DEBUG, format, args);
        }
    }
    
    /**
     * 记录详细跟踪信息（仅在trace模式显示）
     */
    public void trace(String format, Object... args) {
        if (traceEnabled) {
            log(Level.TRACE, format, args);
        }
    }
    
    /**
     * 记录诊断信息（兼容现有代码，相当于debug）
     */
    public void diagnostic(String format, Object... args) {
        debug("[DIAGNOSTIC] " + format, args);
    }
    
    /**
     * 记录CPU跟踪信息（兼容现有代码）
     */
    public void cpuTrace(String format, Object... args) {
        if (traceEnabled) {
            log(Level.TRACE, "[CPU TRACE] " + format, args);
        }
    }
    
    /**
     * 记录调用跟踪信息（兼容现有代码）
     */
    public void callTrace(String format, Object... args) {
        if (traceEnabled) {
            log(Level.TRACE, "[CALL] " + format, args);
        }
    }
    
    /**
     * 记录返回跟踪信息（兼容现有代码）
     */
    public void retTrace(String format, Object... args) {
        if (traceEnabled) {
            log(Level.TRACE, "[RET] " + format, args);
        }
    }
    
    /**
     * 记录栈帧信息（兼容现有代码）
     */
    public void stackFrameTrace(String format, Object... args) {
        if (traceEnabled) {
            log(Level.TRACE, "[StackFrame] " + format, args);
        }
    }
    
    /**
     * 记录内存操作信息（兼容现有代码）
     */
    public void memoryTrace(String format, Object... args) {
        if (traceEnabled) {
            log(Level.TRACE, format, args); // 格式已包含[SW_F]或[STRUCT]前缀
        }
    }
    
    /**
     * 程序输出（PRINT指令使用，总是显示到标准输出）
     */
    public static void programOutput(Object value) {
        System.out.println(value);
    }
    
    private void log(Level level, String format, Object... args) {
        if (shouldLog(level)) {
            String message = String.format(format, args);
            String coloredMessage = addColor(level, message);
            String timestamp = getTimestamp();
            String logLine = String.format("%s [%s] %s - %s", 
                timestamp, level.name(), className, coloredMessage);
            
            if (level == Level.ERROR || level == Level.WARN) {
                System.err.println(logLine);
            } else {
                System.out.println(logLine);
            }
        }
    }
    
    private boolean shouldLog(Level level) {
        if (level == Level.ERROR || level == Level.WARN) {
            return true;
        }
        return level.ordinal() <= currentLevel.ordinal();
    }
    
    private String addColor(Level level, String message) {
        if (System.console() == null) {
            return message; // 非控制台环境不添加颜色
        }
        
        switch (level) {
            case ERROR: return ANSI_RED + message + ANSI_RESET;
            case WARN: return ANSI_YELLOW + message + ANSI_RESET;
            case INFO: return ANSI_GREEN + message + ANSI_RESET;
            case DEBUG: return ANSI_CYAN + message + ANSI_RESET;
            case TRACE: return ANSI_PURPLE + message + ANSI_RESET;
            default: return message;
        }
    }
    
    private String getTimestamp() {
        return java.time.LocalTime.now().toString();
    }
}