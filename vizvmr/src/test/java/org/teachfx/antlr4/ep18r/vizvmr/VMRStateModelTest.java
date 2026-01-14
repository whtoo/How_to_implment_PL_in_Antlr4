package org.teachfx.antlr4.ep18r.vizvmr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep18r.vizvmr.core.VMRStateModel;

import static org.junit.jupiter.api.Assertions.*;

/**
 * VMRStateModel 单元测试
 */
public class VMRStateModelTest {
    private VMRStateModel stateModel;

    @BeforeEach
    void setUp() {
        stateModel = new VMRStateModel(1024, 256, 100);
    }

    @Test
    void testInitialState() {
        assertEquals(org.teachfx.antlr4.ep18r.vizvmr.event.VMStateChangeEvent.State.CREATED, stateModel.getVMState());
        assertEquals(0, stateModel.getProgramCounter());
        assertEquals(0, stateModel.getExecutionSteps());
    }

    @Test
    void testRegisterSetAndGet() {
        stateModel.setRegister(1, 42);
        assertEquals(42, stateModel.getRegister(1));

        stateModel.setRegister(15, 1000);
        assertEquals(1000, stateModel.getRegister(15));
    }

    @Test
    void testRegisterZeroIsConstant() {
        stateModel.setRegister(0, 999);
        assertEquals(0, stateModel.getRegister(0)); // r0 should remain 0
    }

    @Test
    void testInvalidRegister() {
        assertThrows(IllegalArgumentException.class, () -> stateModel.setRegister(20, 100));
        assertThrows(IllegalArgumentException.class, () -> stateModel.getRegister(20));
    }

    @Test
    void testHeapWriteAndRead() {
        stateModel.writeHeap(100, 12345);
        assertEquals(12345, stateModel.readHeap(100));
    }

    @Test
    void testHeapBoundsCheck() {
        assertThrows(IndexOutOfBoundsException.class, () -> stateModel.writeHeap(2000, 100));
        assertThrows(IndexOutOfBoundsException.class, () -> stateModel.readHeap(2000));
    }

    @Test
    void testGlobalWriteAndRead() {
        stateModel.writeGlobal(10, 54321);
        assertEquals(54321, stateModel.readGlobal(10));
    }

    @Test
    void testGlobalBoundsCheck() {
        assertThrows(IndexOutOfBoundsException.class, () -> stateModel.writeGlobal(300, 100));
        assertThrows(IndexOutOfBoundsException.class, () -> stateModel.readGlobal(300));
    }

    @Test
    void testProgramCounter() {
        stateModel.setProgramCounter(100);
        assertEquals(100, stateModel.getProgramCounter());

        stateModel.setProgramCounter(200);
        assertEquals(200, stateModel.getProgramCounter());
    }

    @Test
    void testExecutionSteps() {
        assertEquals(0, stateModel.getExecutionSteps());

        stateModel.incrementExecutionStep();
        assertEquals(1, stateModel.getExecutionSteps());

        stateModel.incrementExecutionStep();
        assertEquals(2, stateModel.getExecutionSteps());
    }

    @Test
    void testVMStateChange() {
        assertEquals(org.teachfx.antlr4.ep18r.vizvmr.event.VMStateChangeEvent.State.CREATED, stateModel.getVMState());

        stateModel.setVMState(org.teachfx.antlr4.ep18r.vizvmr.event.VMStateChangeEvent.State.LOADED);
        assertEquals(org.teachfx.antlr4.ep18r.vizvmr.event.VMStateChangeEvent.State.LOADED, stateModel.getVMState());

        stateModel.setVMState(org.teachfx.antlr4.ep18r.vizvmr.event.VMStateChangeEvent.State.RUNNING);
        assertEquals(org.teachfx.antlr4.ep18r.vizvmr.event.VMStateChangeEvent.State.RUNNING, stateModel.getVMState());
    }

    @Test
    void testRegisterModificationTracking() {
        assertFalse(stateModel.isRegisterModified(1));

        stateModel.setRegister(1, 100);
        assertTrue(stateModel.isRegisterModified(1));

        stateModel.clearModifiedFlags();
        assertFalse(stateModel.isRegisterModified(1));
    }
}
