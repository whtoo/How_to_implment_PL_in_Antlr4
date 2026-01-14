package org.teachfx.antlr4.ep18r.vizvmr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep18r.vizvmr.core.VMRStateModel;
import org.teachfx.antlr4.ep18r.vizvmr.core.VMRExecutionHistory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * VMRExecutionHistory 单元测试
 */
public class VMRExecutionHistoryTest {
    private VMRStateModel stateModel;
    private VMRExecutionHistory history;

    @BeforeEach
    void setUp() {
        stateModel = new VMRStateModel(1024, 256, 100);
        history = new VMRExecutionHistory(100);
    }

    @Test
    void testInitialHistoryState() {
        assertFalse(history.canUndo());
        assertFalse(history.canRedo());
        assertEquals(0, history.getUndoSize());
        assertEquals(0, history.getRedoSize());
    }

    @Test
    void testSaveState() {
        stateModel.setRegister(1, 42);
        stateModel.setProgramCounter(100);
        history.saveState(stateModel);

        assertTrue(history.canUndo());
        assertFalse(history.canRedo());
        assertEquals(1, history.getUndoSize());
    }

    @Test
    void testUndo() {
        stateModel.setRegister(1, 42);
        history.saveState(stateModel);

        // 不保存中间状态，直接改变
        stateModel.setRegister(1, 100);

        VMRStateModel.VMRStateSnapshot restored = history.undo(stateModel);
        assertNotNull(restored);
        assertEquals(42, stateModel.getRegister(1));

        assertTrue(history.canRedo());
        assertFalse(history.canUndo());
    }

    @Test
    void testRedo() {
        stateModel.setRegister(1, 42);
        history.saveState(stateModel);

        stateModel.setRegister(1, 100);
        history.saveState(stateModel);

        // 第一次 undo 恢复到 42
        history.undo(stateModel);
        assertEquals(42, stateModel.getRegister(1));

        // 第二次 redo 应该恢复到 100
        VMRStateModel.VMRStateSnapshot restored = history.redo(stateModel);
        assertNotNull(restored);
        assertEquals(100, stateModel.getRegister(1));
    }

    @Test
    void testClear() {
        stateModel.setRegister(1, 42);
        history.saveState(stateModel);

        stateModel.setRegister(1, 100);
        history.saveState(stateModel);

        history.clear();

        assertFalse(history.canUndo());
        assertFalse(history.canRedo());
        assertEquals(0, history.getSnapshotCount());
    }

    @Test
    void testMaxSnapshotsLimit() {
        VMRExecutionHistory smallHistory = new VMRExecutionHistory(3);

        for (int i = 0; i < 10; i++) {
            stateModel.setRegister(1, i);
            smallHistory.saveState(stateModel);
        }

        // 应该只保留最近的3个快照
        assertEquals(3, smallHistory.getUndoSize());
        assertEquals(3, smallHistory.getSnapshotCount());
    }
}
