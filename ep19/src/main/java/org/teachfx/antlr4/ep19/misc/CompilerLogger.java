package org.teachfx.antlr4.ep19.misc;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.function.Consumer;
import org.antlr.v4.runtime.misc.Interval;

/**
 * 编译器日志工具类，使用SLF4J打印编译错误和警告
 */
public class CompilerLogger {
    
    // 错误消息监听器
    private static Consumer<String> errorListener = null;
    
    /**
     * 设置错误监听器
     * @param listener 错误消息监听器，如果为null则不使用监听器
     */
    public static void setErrorListener(Consumer<String> listener) {
        errorListener = listener;
    }
    
    /**
     * 输出错误信息
     * @param ctx 解析上下文
     * @param message 错误消息
     */
    public static void error(ParserRuleContext ctx, String message) {
        Token start = ctx.getStart();
        int line = start.getLine();
        int charPositionInLine = start.getCharPositionInLine();
        
        // 获取源文本
        String sourceText = "";
        try {
            if (ctx.start != null && ctx.stop != null && ctx.start.getInputStream() != null) {
                Interval interval = new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
                sourceText = ctx.start.getInputStream().getText(interval);
                // 如果源文本太长，进行截断
                if (sourceText.length() > 50) {
                    sourceText = sourceText.substring(0, 47) + "...";
                }
            }
        } catch (Exception e) {
            // 忽略获取源文本时的异常
        }
        
        // 获取源文件名
        String sourceFile = "unknown";
        if (start.getTokenSource() != null && start.getTokenSource().getSourceName() != null) {
            sourceFile = start.getTokenSource().getSourceName();
        }
        
        // 格式化错误消息
        String formattedMessage = String.format("错误[%s:%d:%d]: %s，源码: '%s'", 
                                               sourceFile, line, charPositionInLine, 
                                               message, sourceText);
        
        // 如果设置了错误监听器，将错误消息发送给监听器
        if (errorListener != null) {
            errorListener.accept(formattedMessage);
        }
        
        // 获取调用者的Logger
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        Logger logger = LoggerFactory.getLogger(caller.getClassName());
        
        logger.error("错误[{}:{}:{}]: {}，源码: '{}'", sourceFile, line, charPositionInLine, message, sourceText);
    }
    
    /**
     * 输出警告信息
     * @param ctx 解析上下文
     * @param message 警告消息
     */
    public static void warning(ParserRuleContext ctx, String message) {
        Token start = ctx.getStart();
        int line = start.getLine();
        int charPositionInLine = start.getCharPositionInLine();
        
        // 获取源文本
        String sourceText = "";
        try {
            if (ctx.start != null && ctx.stop != null && ctx.start.getInputStream() != null) {
                Interval interval = new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
                sourceText = ctx.start.getInputStream().getText(interval);
                // 如果源文本太长，进行截断
                if (sourceText.length() > 50) {
                    sourceText = sourceText.substring(0, 47) + "...";
                }
            }
        } catch (Exception e) {
            // 忽略获取源文本时的异常
        }
        
        // 获取源文件名
        String sourceFile = "unknown";
        if (start.getTokenSource() != null && start.getTokenSource().getSourceName() != null) {
            sourceFile = start.getTokenSource().getSourceName();
        }
        
        // 获取调用者的Logger
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        Logger logger = LoggerFactory.getLogger(caller.getClassName());
        
        logger.warn("警告[{}:{}:{}]: {}，源码: '{}'", sourceFile, line, charPositionInLine, message, sourceText);
    }
    
    /**
     * 输出信息级别的日志
     * @param message 日志消息
     */
    public static void info(String message) {
        // 获取调用者的Logger
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        Logger logger = LoggerFactory.getLogger(caller.getClassName());
        
        logger.info("信息: {}", message);
    }
    
    /**
     * 输出调试信息
     * @param message 调试消息
     */
    public static void debug(String message) {
        // 获取调用者的Logger
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        Logger logger = LoggerFactory.getLogger(caller.getClassName());
        
        logger.debug("调试: {}", message);
    }
    
    /**
     * 输出跟踪级别的日志，用于非常详细的调试
     * @param message 跟踪消息
     */
    public static void trace(String message) {
        // 获取调用者的Logger
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        Logger logger = LoggerFactory.getLogger(caller.getClassName());
        
        logger.trace("跟踪: {}", message);
    }
    
    /**
     * 为给定类创建新的Logger实例
     * @param clazz 类
     * @return 日志记录器
     */
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }
}
