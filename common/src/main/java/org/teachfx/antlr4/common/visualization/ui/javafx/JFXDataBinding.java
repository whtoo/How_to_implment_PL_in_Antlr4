package org.teachfx.antlr4.common.visualization.ui.javafx;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.binding.*;
import javafx.collections.*;
import javafx.event.*;
import javafx.util.*;

import java.util.*;
import java.util.function.*;

/**
 * JavaFX数据绑定系统
 * 提供响应式数据绑定功能
 */
public class JFXDataBinding {
    
    /**
     * 创建一个可观察属性
     */
    public static <T> ObjectProperty<T> createProperty(T initialValue) {
        return new SimpleObjectProperty<>(initialValue);
    }
    
    /**
     * 创建一个字符串可观察属性
     */
    public static StringProperty createStringProperty(String initialValue) {
        return new SimpleStringProperty(initialValue);
    }
    
    /**
     * 创建一个整数可观察属性
     */
    public static IntegerProperty createIntegerProperty(int initialValue) {
        return new SimpleIntegerProperty(initialValue);
    }
    
    /**
     * 创建一个双精度浮点可观察属性
     */
    public static DoubleProperty createDoubleProperty(double initialValue) {
        return new SimpleDoubleProperty(initialValue);
    }
    
    /**
     * 创建一个布尔可观察属性
     */
    public static BooleanProperty createBooleanProperty(boolean initialValue) {
        return new SimpleBooleanProperty(initialValue);
    }
    
    /**
     * 创建一个可观察列表
     */
    public static <T> ObservableList<T> createObservableList(Collection<T> items) {
        ObservableList<T> list = FXCollections.observableArrayList();
        if (items != null) {
            list.addAll(items);
        }
        return list;
    }
    
    /**
     * 创建一个可观察映射
     */
    public static <K, V> ObservableMap<K, V> createObservableMap(Map<K, V> map) {
        ObservableMap<K, V> observableMap = FXCollections.observableHashMap();
        if (map != null) {
            observableMap.putAll(map);
        }
        return observableMap;
    }
    
    /**
     * 绑定两个属性（单向）
     */
    public static <T> void bind(Property<T> source, Property<T> target) {
        target.bind(source);
    }
    
    /**
     * 绑定两个属性（双向）
     */
    public static <T> void bindBidirectional(Property<T> prop1, Property<T> prop2) {
        prop1.bindBidirectional(prop2);
    }
    
    /**
     * 创建计算属性（基于其他属性计算）
     */
    public static <T, R> ReadOnlyObjectProperty<R> computed(
            ReadOnlyProperty<T> source,
            Function<T, R> mapper) {
        
        return new ReadOnlyObjectWrapper<R>() {
            {
                bind(new SimpleObjectProperty<>(mapper.apply(source.getValue())));
                source.addListener((obs, oldVal, newVal) -> {
                    setValue(mapper.apply(newVal));
                });
            }
        };
    }
    
    /**
     * 创建条件绑定
     */
    public static <T> void bindWhen(
            Property<Boolean> condition,
            Property<T> target,
            T trueValue,
            T falseValue) {
        
        target.setValue(condition.getValue() ? trueValue : falseValue);
        
        condition.addListener((obs, oldVal, newVal) -> {
            target.setValue(newVal ? trueValue : falseValue);
        });
    }
}
