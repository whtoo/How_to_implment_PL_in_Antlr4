package org.teachfx.antlr4.common.visualization.ui;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 数据绑定系统
 * 连接VM状态和UI显示
 */
public class DataBinding {
    private final ConcurrentHashMap<String, Binding<?>> bindings;
    private final VisualPanelBase panel;
    private volatile boolean enabled;
    
    public DataBinding(VisualPanelBase panel) {
        this.panel = panel;
        this.bindings = new ConcurrentHashMap<>();
        this.enabled = true;
    }
    
    /**
     * 创建属性绑定
     */
    public <T> PropertyBinding<T> bindProperty(String propertyName, Supplier<T> getter, Consumer<T> setter) {
        PropertyBinding<T> binding = new PropertyBinding<>(propertyName, getter, setter);
        bindings.put(propertyName, binding);
        return binding;
    }
    
    /**
     * 创建事件绑定
     */
    public <T> EventBinding<T> bindEvent(String eventName, Consumer<T> handler) {
        EventBinding<T> binding = new EventBinding<>(eventName, handler);
        bindings.put(eventName, binding);
        return binding;
    }
    
    /**
     * 创建计算属性绑定
     */
    public <T> ComputedBinding<T> bindComputed(String propertyName, Supplier<T> computer, String... dependencies) {
        ComputedBinding<T> binding = new ComputedBinding<>(propertyName, computer, dependencies);
        bindings.put(propertyName, binding);
        return binding;
    }
    
    /**
     * 获取绑定
     */
    @SuppressWarnings("unchecked")
    public <T> Binding<T> getBinding(String name) {
        return (Binding<T>) bindings.get(name);
    }
    
    /**
     * 更新所有绑定
     */
    public void updateAll() {
        if (!enabled) {
            return;
        }
        
        panel.safeUpdateUI(() -> {
            bindings.values().forEach(Binding::update);
        });
    }
    
    /**
     * 更新指定绑定
     */
    public void updateBinding(String name) {
        if (!enabled) {
            return;
        }
        
        Binding<?> binding = bindings.get(name);
        if (binding != null) {
            panel.safeUpdateUI(binding::update);
        }
    }
    
    /**
     * 触发依赖更新
     */
    public void triggerDependencies(String propertyName) {
        bindings.values().stream()
            .filter(binding -> binding instanceof ComputedBinding)
            .map(binding -> (ComputedBinding<?>) binding)
            .filter(computed -> computed.hasDependency(propertyName))
            .forEach(computed -> updateBinding(computed.getPropertyName()));
    }
    
    /**
     * 启用/禁用绑定
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * 检查是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * 清理绑定
     */
    public void cleanup() {
        bindings.clear();
    }
    
    /**
     * 绑定接口
     */
    public interface Binding<T> {
        String getPropertyName();
        void update();
        T getValue();
        void setValue(T value);
    }
    
    /**
     * 属性绑定实现
     */
    public static class PropertyBinding<T> implements Binding<T> {
        private final String propertyName;
        private final Supplier<T> getter;
        private final Consumer<T> setter;
        private T cachedValue;
        
        public PropertyBinding(String propertyName, Supplier<T> getter, Consumer<T> setter) {
            this.propertyName = propertyName;
            this.getter = getter;
            this.setter = setter;
            this.cachedValue = null;
        }
        
        @Override
        public String getPropertyName() {
            return propertyName;
        }
        
        @Override
        public void update() {
            T newValue = getter.get();
            if (!equals(newValue, cachedValue)) {
                cachedValue = newValue;
                setter.accept(newValue);
            }
        }
        
        @Override
        public T getValue() {
            return cachedValue != null ? cachedValue : getter.get();
        }
        
        @Override
        public void setValue(T value) {
            if (setter != null) {
                setter.accept(value);
                cachedValue = value;
            }
        }
        
        private boolean equals(T a, T b) {
            return a == null ? b == null : a.equals(b);
        }
    }
    
    /**
     * 事件绑定实现
     */
    public static class EventBinding<T> implements Binding<T> {
        private final String eventName;
        private final Consumer<T> handler;
        private T lastEvent;
        
        public EventBinding(String eventName, Consumer<T> handler) {
            this.eventName = eventName;
            this.handler = handler;
            this.lastEvent = null;
        }
        
        @Override
        public String getPropertyName() {
            return eventName;
        }
        
        @Override
        public void update() {
        }
        
        @Override
        public T getValue() {
            return lastEvent;
        }
        
        @Override
        public void setValue(T value) {
            if (handler != null) {
                handler.accept(value);
                lastEvent = value;
            }
        }
        
        public void trigger(T event) {
            setValue(event);
        }
    }
    
    /**
     * 计算属性绑定实现
     */
    public static class ComputedBinding<T> implements Binding<T> {
        private final String propertyName;
        private final Supplier<T> computer;
        private final String[] dependencies;
        private T cachedValue;
        
        public ComputedBinding(String propertyName, Supplier<T> computer, String[] dependencies) {
            this.propertyName = propertyName;
            this.computer = computer;
            this.dependencies = dependencies != null ? dependencies : new String[0];
            this.cachedValue = null;
        }
        
        @Override
        public String getPropertyName() {
            return propertyName;
        }
        
        @Override
        public void update() {
            T newValue = computer.get();
            if (!equals(newValue, cachedValue)) {
                cachedValue = newValue;
            }
        }
        
        @Override
        public T getValue() {
            return cachedValue != null ? cachedValue : computer.get();
        }
        
        @Override
        public void setValue(T value) {
        }
        
        public boolean hasDependency(String propertyName) {
            for (String dependency : dependencies) {
                if (dependency.equals(propertyName)) {
                    return true;
                }
            }
            return false;
        }
        
        private boolean equals(T a, T b) {
            return a == null ? b == null : a.equals(b);
        }
    }
}