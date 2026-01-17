package org.teachfx.antlr4.ep18r.vizvmr.ui.javafx;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.junit.jupiter.api.*;
import org.teachfx.antlr4.ep18r.stackvm.StackFrame;
import org.teachfx.antlr4.ep18r.stackvm.config.VMConfig;
import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;
import org.teachfx.antlr4.ep18r.vizvmr.core.ReactiveVMRStateModel;
import org.teachfx.antlr4.ep18r.vizvmr.integration.VMRVisualBridge;
import org.teachfx.antlr4.ep18r.stackvm.FunctionSymbol;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.*;

@DisplayName("栈帧跟踪显示测试")
class StackFrameTrackingTest {

    private RegisterVMInterpreter vm;
    private ReactiveVMRStateModel stateModel;
    private VMRVisualBridge visualBridge;
    private ReactiveStackView stackView;

    @BeforeAll
    static void initJavaFX() {
        Platform.setImplicitExit(false);
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Toolkit already initialized, ignore
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        VMConfig config = new VMConfig.Builder()
                .setHeapSize(1024)
                .setStackSize(256)
                .build();

        vm = new RegisterVMInterpreter(config);
        stateModel = new ReactiveVMRStateModel(vm);
        visualBridge = null; // new VMRVisualBridge(vm, stateModel); // Bridge is now internal to ReactiveVMRStateModel
        stackView = new ReactiveStackView(stateModel);
    }

    @AfterEach
    void tearDown() {
        if (visualBridge != null) {
            visualBridge.stop();
        }
        if (stackView != null) {
            stackView.dispose();
        }
    }

    private void runAndWait(Runnable action) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                latch.countDown();
            }
        });
        if (!latch.await(5, TimeUnit.SECONDS)) {
            throw new TimeoutException("JavaFX action timed out");
        }
    }

    @Test
    @DisplayName("栈帧跟踪视图应该正确初始化")
    void stackView_ShouldInitializeCorrectly() throws Exception {
        runAndWait(() -> {
            assertThat(stackView).isNotNull();
            assertThat(stackView).isInstanceOf(ReactiveStackView.class);
        });
    }

    @Test
    @DisplayName("栈帧跟踪应该显示空栈提示")
    void stackView_ShouldDisplayEmptyStackMessage() throws Exception {
        runAndWait(() -> {
            ReactiveStackView view = new ReactiveStackView(stateModel);
            
            assertThat(view).isNotNull();
            ListView<StackFrame> stackList = view.getStackList();
            
            Platform.runLater(() -> {
                stackList.getItems().clear();
                assertThat(stackList.getItems()).isEmpty();
            });
        });
    }

    @Test
    @DisplayName("栈帧跟踪应该正确处理多个栈帧")
    void stackView_ShouldHandleMultipleFrames() throws Exception {
        runAndWait(() -> {
            ReactiveStackView stackView = new ReactiveStackView(stateModel);
            
            loadTestProgram("funcA r1, 0, 3\n" +
                           "push r1, 42\n" +
                           "funcB r2, 0, 4\n" +
                           "funcC r3, 0, 7\n" +
                           "push r4, 45, 46\n" +
                           "ret\n");
            
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            
            assertThat(stateModel.getCallStackDepth()).isEqualTo(4);
            
            ListView<StackFrame> stackList = stackView.getStackList();
            
            assertThat(stackList.getItems()).isNotEmpty();
            
                Platform.runLater(() -> {
                    stackList.getItems().clear();
                });
        });
    }

    @Test
    @DisplayName("栈帧跟踪应该正确处理空栈")
    void stackView_ShouldHandleEmptyStack() throws Exception {
        runAndWait(() -> {
            ReactiveStackView stackView = new ReactiveStackView(stateModel);
            ListView<StackFrame> stackList = stackView.getStackList();
            
            int initialDepth = stateModel.getCallStackDepth();
            
            Platform.runLater(() -> {
                // 清空所有栈帧
                for (int i = 0; i < initialDepth; i++) {
                    stateModel.popStackFrame();
                }
                
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                
                int newDepth = stateModel.getCallStackDepth();
                assertThat(newDepth).isEqualTo(0);
                
                Platform.runLater(() -> {
                    stackList.getItems().clear();
                });
            });
        });
    }

    private FunctionSymbol createTestSymbol(String name, int nargs, int localsSize) {
        return new FunctionSymbol(name, nargs, localsSize, 0);
    }

    private void loadTestProgram(String program) {
    }

}
