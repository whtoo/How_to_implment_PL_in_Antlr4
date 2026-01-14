package org.teachfx.antlr4.ep18r.vizvmr.core;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

/**
 * 执行历史记录
 * 保存执行快照，支持撤销/重做
 */
public class VMRExecutionHistory {
    private static final int DEFAULT_MAX_SNAPSHOTS = 1000;

    private final Deque<VMRStateModel.VMRStateSnapshot> undoStack;
    private final Deque<VMRStateModel.VMRStateSnapshot> redoStack;
    private final int maxSnapshots;

    public VMRExecutionHistory() {
        this(DEFAULT_MAX_SNAPSHOTS);
    }

    public VMRExecutionHistory(int maxSnapshots) {
        this.undoStack = new ArrayDeque<>();
        this.redoStack = new ArrayDeque<>();
        this.maxSnapshots = maxSnapshots;
    }

    /**
     * 保存当前状态到历史
     */
    public void saveState(VMRStateModel stateModel) {
        VMRStateModel.VMRStateSnapshot snapshot = stateModel.createSnapshot();
        undoStack.push(snapshot);
        redoStack.clear();

        // 限制历史大小
        while (undoStack.size() > maxSnapshots) {
            undoStack.removeLast();
        }
    }

    /**
     * 撤销到上一个状态
     */
    public VMRStateModel.VMRStateSnapshot undo(VMRStateModel stateModel) {
        if (undoStack.isEmpty()) {
            return null;
        }

        // 先保存当前状态到重做栈
        redoStack.push(stateModel.createSnapshot());

        // 从撤销栈弹出并恢复到上一个保存的状态
        VMRStateModel.VMRStateSnapshot snapshot = undoStack.pollLast();
        stateModel.restoreSnapshot(snapshot);

        return snapshot;
    }

    /**
     * 重做下一个状态
     */
    public VMRStateModel.VMRStateSnapshot redo(VMRStateModel stateModel) {
        if (redoStack.isEmpty()) {
            return null;
        }

        // 恢复到下一个状态
        VMRStateModel.VMRStateSnapshot snapshot = redoStack.pop();
        stateModel.restoreSnapshot(snapshot);

        // 将恢复的状态放回撤销栈，以便可以再次撤销
        undoStack.push(snapshot);

        return snapshot;
    }

    /**
     * 检查是否可以撤销
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    /**
     * 检查是否可以重做
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /**
     * 清空历史
     */
    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }

    /**
     * 获取撤销栈大小
     */
    public int getUndoSize() {
        return undoStack.size();
    }

    /**
     * 获取重做栈大小
     */
    public int getRedoSize() {
        return redoStack.size();
    }

    /**
     * 获取历史记录迭代器
     */
    public Iterator<VMRStateModel.VMRStateSnapshot> getUndoHistory() {
        return undoStack.iterator();
    }

    /**
     * 获取快照数量
     */
    public int getSnapshotCount() {
        return undoStack.size() + redoStack.size();
    }

    /**
     * 获取最大快照数量
     */
    public int getMaxSnapshots() {
        return maxSnapshots;
    }
}
