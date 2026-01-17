package org.teachfx.antlr4.common.visualization.event;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import org.teachfx.antlr4.common.visualization.ExecutionListener;
import org.teachfx.antlr4.common.visualization.StateChangeListener;
import org.teachfx.antlr4.common.visualization.EducationalHintListener;
import org.teachfx.antlr4.common.visualization.VMExecutionException;
import org.teachfx.antlr4.common.visualization.VMState;
import org.teachfx.antlr4.common.visualization.PerformanceMetrics;
import org.teachfx.antlr4.common.visualization.event.events.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RxJava事件流到传统监听器的适配器
 * 
 * <p>将RxEventBus中的事件流适配到传统的Listener接口（ExecutionListener,
 * StateChangeListener, EducationalHintListener），实现向后兼容。</p>
 * 
 * <p>支持两种适配模式：
 * 1. 主动适配：将Rx事件流映射到现有监听器
 * 2. 被动适配：将监听器调用转换为Rx事件</p>
 * 
 * @author TeachFX Team
 * @version 1.0
 * @since EP18R响应式事件系统
 */
public class RxListenerAdapter {
    private static final Logger logger = LogManager.getLogger(RxListenerAdapter.class);
    
    private final RxEventBus rxEventBus;
    private final CompositeDisposable disposables;
    private final Map<ExecutionListener, Disposable> executionListenerMap;
    private final Map<StateChangeListener, Disposable> stateChangeListenerMap;
    private final Map<EducationalHintListener, Disposable> educationalListenerMap;
    
    /**
     * 创建适配器
     * 
     * @param rxEventBus Rx事件总线实例
     */
    public RxListenerAdapter(RxEventBus rxEventBus) {
        this.rxEventBus = rxEventBus;
        this.disposables = new CompositeDisposable();
        this.executionListenerMap = new ConcurrentHashMap<>();
        this.stateChangeListenerMap = new ConcurrentHashMap<>();
        this.educationalListenerMap = new ConcurrentHashMap<>();
        
        logger.debug("RxListenerAdapter创建完成，连接到RxEventBus: {}", rxEventBus.getPublisherId());
    }
    
    // ==================== 主动适配：Rx事件流 → 传统监听器 ====================
    
    /**
     * 将ExecutionListener适配到Rx事件流
     * 
     * @param listener 传统执行监听器
     */
    public void adaptExecutionListener(ExecutionListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("ExecutionListener cannot be null");
        }
        
        if (executionListenerMap.containsKey(listener)) {
            logger.debug("ExecutionListener已适配，跳过: {}", listener);
            return;
        }
        
        CompositeDisposable listenerDisposables = new CompositeDisposable();
        
        // 适配指令执行事件
        Disposable instructionDisposable = rxEventBus.getEventStream(InstructionExecutedEvent.class)
            .subscribe(event -> {
                try {
                    listener.afterInstructionExecute(
                        event.getPC(),
                        event.getMnemonic() + " " + event.getOperands(),
                        null // 结果暂不提供
                    );
                } catch (Exception e) {
                    logger.warn("ExecutionListener.afterInstructionExecute处理错误: {}", e.getMessage());
                }
            });
        
        listenerDisposables.add(instructionDisposable);
        
        // 适配执行开始事件
        Disposable startDisposable = rxEventBus.getEventStream(ExecutionStartedEvent.class)
            .subscribe(event -> {
                try {
                    listener.executionStarted();
                } catch (Exception e) {
                    logger.warn("ExecutionListener.executionStarted处理错误: {}", e.getMessage());
                }
            });
        
        listenerDisposables.add(startDisposable);
        
        // 适配执行停止事件
        Disposable stopDisposable = rxEventBus.getEventStream(ExecutionFinishedEvent.class)
            .subscribe(event -> {
                try {
                    listener.executionStopped(event.getDescription());
                } catch (Exception e) {
                    logger.warn("ExecutionListener.executionStopped处理错误: {}", e.getMessage());
                }
            });
        
        listenerDisposables.add(stopDisposable);
        
        // 适配执行暂停事件
        Disposable pauseDisposable = rxEventBus.getEventStream(ExecutionPausedEvent.class)
            .subscribe(event -> {
                try {
                    listener.executionPaused();
                } catch (Exception e) {
                    logger.warn("ExecutionListener.executionPaused处理错误: {}", e.getMessage());
                }
            });
        
        listenerDisposables.add(pauseDisposable);
        
        // 适配执行错误事件
        Disposable errorDisposable = rxEventBus.getEventStream(ExecutionErrorEvent.class)
            .subscribe(event -> {
                try {
                    listener.executionError(new VMExecutionException(
                        "RxEventBus事件错误: " + event.getDescription(),
                        event.getError()
                    ));
                } catch (Exception e) {
                    logger.warn("ExecutionListener.executionError处理错误: {}", e.getMessage());
                }
            });
        
        listenerDisposables.add(errorDisposable);
        
        // 适配断点命中事件
        Disposable breakpointDisposable = rxEventBus.getEventStream(BreakpointHitEvent.class)
            .subscribe(event -> {
                try {
                    listener.breakpointHit(event.getPC(), event.getInstruction());
                } catch (Exception e) {
                    logger.warn("ExecutionListener.breakpointHit处理错误: {}", e.getMessage());
                }
            });
        
        listenerDisposables.add(breakpointDisposable);
        
        executionListenerMap.put(listener, listenerDisposables);
        disposables.add(listenerDisposables);
        
        logger.debug("ExecutionListener已适配到Rx事件流: {}", listener);
    }
    
    /**
     * 将StateChangeListener适配到Rx事件流
     */
    public void adaptStateChangeListener(StateChangeListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("StateChangeListener cannot be null");
        }
        
        if (stateChangeListenerMap.containsKey(listener)) {
            logger.debug("StateChangeListener已适配，跳过: {}", listener);
            return;
        }
        
        CompositeDisposable listenerDisposables = new CompositeDisposable();
        
        // 适配寄存器变化事件
        Disposable registerDisposable = rxEventBus.getEventStream(RegisterChangedEvent.class)
            .subscribe(event -> {
                try {
                    // StateChangeListener没有直接的寄存器变化方法
                    // 这里可以转换为更通用的状态变化通知
                    listener.vmStateChanged(null, null); // 简化处理
                } catch (Exception e) {
                    logger.warn("StateChangeListener处理寄存器变化错误: {}", e.getMessage());
                }
            });
        
        listenerDisposables.add(registerDisposable);
        
        // 适配内存变化事件
        Disposable memoryDisposable = rxEventBus.getEventStream(MemoryChangedEvent.class)
            .subscribe(event -> {
                try {
                    // 简化处理
                    listener.vmStateChanged(null, null);
                } catch (Exception e) {
                    logger.warn("StateChangeListener处理内存变化错误: {}", e.getMessage());
                }
            });
        
        listenerDisposables.add(memoryDisposable);
        
        // 适配PC变化事件
        Disposable pcDisposable = rxEventBus.getEventStream(ProgramCounterChangedEvent.class)
            .subscribe(event -> {
                try {
                    listener.vmStateChanged(null, null);
                } catch (Exception e) {
                    logger.warn("StateChangeListener处理PC变化错误: {}", e.getMessage());
                }
            });
        
        listenerDisposables.add(pcDisposable);
        
        stateChangeListenerMap.put(listener, listenerDisposables);
        disposables.add(listenerDisposables);
        
        logger.debug("StateChangeListener已适配到Rx事件流: {}", listener);
    }
    
    /**
     * 将EducationalHintListener适配到Rx事件流
     */
    public void adaptEducationalHintListener(EducationalHintListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("EducationalHintListener cannot be null");
        }
        
        if (educationalListenerMap.containsKey(listener)) {
            logger.debug("EducationalHintListener已适配，跳过: {}", listener);
            return;
        }
        
        CompositeDisposable listenerDisposables = new CompositeDisposable();
        
        // 适配教育提示事件
        Disposable hintDisposable = rxEventBus.getEventStream(EducationalHintEvent.class)
            .subscribe(event -> {
                try {
                    listener.hintAvailable(event.getHint(), EducationalHintListener.HintCategory.BASIC_OPERATION, 1);
                } catch (Exception e) {
                    logger.warn("EducationalHintListener处理错误: {}", e.getMessage());
                }
            });
        
        listenerDisposables.add(hintDisposable);
        
        educationalListenerMap.put(listener, listenerDisposables);
        disposables.add(listenerDisposables);
        
        logger.debug("EducationalHintListener已适配到Rx事件流: {}", listener);
    }
    
    /**
     * 取消适配ExecutionListener
     */
    public void removeExecutionListener(ExecutionListener listener) {
        if (listener == null) {
            return;
        }
        
        Disposable disposable = executionListenerMap.remove(listener);
        if (disposable != null) {
            disposable.dispose();
            disposables.remove(disposable);
            logger.debug("ExecutionListener适配已取消: {}", listener);
        }
    }
    
    /**
     * 取消适配StateChangeListener
     */
    public void removeStateChangeListener(StateChangeListener listener) {
        if (listener == null) {
            return;
        }
        
        Disposable disposable = stateChangeListenerMap.remove(listener);
        if (disposable != null) {
            disposable.dispose();
            disposables.remove(disposable);
            logger.debug("StateChangeListener适配已取消: {}", listener);
        }
    }
    
    /**
     * 取消适配EducationalHintListener
     */
    public void removeEducationalHintListener(EducationalHintListener listener) {
        if (listener == null) {
            return;
        }
        
        Disposable disposable = educationalListenerMap.remove(listener);
        if (disposable != null) {
            disposable.dispose();
            disposables.remove(disposable);
            logger.debug("EducationalHintListener适配已取消: {}", listener);
        }
    }
    
    /**
     * 取消所有适配
     */
    public void clearAllAdapters() {
        executionListenerMap.keySet().forEach(this::removeExecutionListener);
        stateChangeListenerMap.keySet().forEach(this::removeStateChangeListener);
        educationalListenerMap.keySet().forEach(this::removeEducationalHintListener);
        
        logger.debug("所有监听器适配已清除");
    }
    
    /**
     * 获取适配的ExecutionListener数量
     */
    public int getExecutionListenerCount() {
        return executionListenerMap.size();
    }
    
    /**
     * 获取适配的StateChangeListener数量
     */
    public int getStateChangeListenerCount() {
        return stateChangeListenerMap.size();
    }
    
    /**
     * 获取适配的EducationalHintListener数量
     */
    public int getEducationalHintListenerCount() {
        return educationalListenerMap.size();
    }
    
    /**
     * 释放所有资源
     */
    public void dispose() {
        clearAllAdapters();
        disposables.dispose();
        logger.debug("RxListenerAdapter资源已释放");
    }
    
    /**
     * 检查是否已释放
     */
    public boolean isDisposed() {
        return disposables.isDisposed();
    }
    
    // ==================== 被动适配：传统监听器调用 → Rx事件 ====================
    
    /**
     * 创建ExecutionListener包装器，将监听器调用转换为Rx事件
     */
    public ExecutionListener createRxExecutionListener() {
        return new ExecutionListener() {
            @Override
            public void afterInstructionExecute(int pc, String instruction, Object result) {
                InstructionExecutedEvent event = new InstructionExecutedEvent(
                    this, 0, pc, 0, instruction, "");
                rxEventBus.publish(event);
            }
            
            @Override
            public void executionStarted() {
                ExecutionStartedEvent event = new ExecutionStartedEvent(this, 0);
                rxEventBus.publish(event);
            }
            
            @Override
            public void executionStopped(String reason) {
                ExecutionFinishedEvent event = new ExecutionFinishedEvent(this, 0, reason);
                rxEventBus.publish(event);
            }
            
            @Override
            public void executionPaused() {
                ExecutionPausedEvent event = new ExecutionPausedEvent(this, 0);
                rxEventBus.publish(event);
            }
            
            @Override
            public void executionError(VMExecutionException exception) {
                ExecutionErrorEvent event = new ExecutionErrorEvent(this, 0, exception);
                rxEventBus.publish(event);
            }
            
            @Override
            public void breakpointHit(int pc, String instruction) {
                BreakpointHitEvent event = new BreakpointHitEvent(this, 0, pc, instruction);
                rxEventBus.publish(event);
            }
        };
    }
    
    /**
     * 创建StateChangeListener包装器
     */
    public StateChangeListener createRxStateChangeListener() {
        return new StateChangeListener() {
            @Override
            public void vmStateChanged(VMState.ExecutionState oldState, VMState.ExecutionState newState) {
                // 转换为ExecutionStateChangedEvent
                ExecutionStateChangedEvent event = new ExecutionStateChangedEvent(
                    this, 0, oldState, newState);
                rxEventBus.publish(event);
            }
        };
    }
}