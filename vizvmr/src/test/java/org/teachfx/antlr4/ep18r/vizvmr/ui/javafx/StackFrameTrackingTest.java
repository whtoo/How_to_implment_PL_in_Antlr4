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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.*;

@DisplayName("栈帧跟踪显示测试")
class StackFrameTrackingTest {

    private RegisterVMInterpreter vm;
    private VMRStateModel stateModel;
    private VMRVisualBridge visualBridge;
    private ReactiveStackView stackView;

    @BeforeEach
    void setUp() throws Exception {
        Platform.startup(() -> {});

        VMConfig config = new VMConfig.Builder()
                .setHeapSize(1024)
                .setStackSize(256)
                .setMaxCallStackDepth(32)
                .build();

        vm = new RegisterVMInterpreter(config);
        stateModel = new VMRStateModel(vm);
        visualBridge = new VMRVisualBridge(vm, stateModel);
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
            ListView<String> stackList = view.getStackList();
            
            Platform.runLater(() -> {
                stackList.getItems().clear();
                assertThat(stackList.getItems()).isEmpty();
            }, Throwable::printStackTrace);
        });
    }

    @Test
    @DisplayName("栈帧应该正确加载和显示反汇编代码")
    void stackView_ShouldDisplayDisassembledCode() throws Exception {
        runAndWait(() -> {
            // 加载测试程序
            loadTestProgram("li r1, 42\n" +
                           "add r2, 42\n" +
                           "add r3, 42\n" +
                           "halt\n");
            
            ReactiveCodeView codeView = new ReactiveCodeView(stateModel);
            codeView.setInstructions(visualBridge.getDisAssembler());
            
            assertThat(codeView).isNotNull();
        });
    }

    @Test
    @DisplayName("栈帧应该正确模拟函数调用并显示")
    void stackView_ShouldSimulateFunctionCalls() throws Exception {
        runAndWait(() -> {
            ReactiveStackView stackView = new ReactiveStackView(stateModel);
            
            stateModel.start();
            Thread.sleep(100);
            
            stateModel.stop();
            
            assertThat(stateModel.getCallStackDepth()).isGreaterThan(0);
        });
    }

    @Test
    @DisplayName("栈帧列表应该正确解析StackFrame数据")
    void stackView_ShouldParseStackFrameData() throws Exception {
        runAndWait(() -> {
            ReactiveStackView stackView = new ReactiveStackView(stateModel);
            
            stateModel.start();
            Thread.sleep(100);
            
            stateModel.stop();
            
            StackFrame[] callStack = stateModel.getCallStack();
            
            assertThat(callStack).isNotNull();
            assertThat(callStack.length).isGreaterThan(0);
            
            if (callStack.length > 0 && callStack[0] != null) {
                StackFrame frame = callStack[0];
                
                assertThat(frame.getFunctionSymbol()).isNotNull();
                assertThat(frame.getReturnAddress()).isNotNull();
                assertThat(frame.getFrameBasePointer()).isNotNull();
                assertThat(frame.getSavedCallerRegisters()).isNotNull();
            }
        });
    }

    @Test
    @DisplayName("栈帧Cell应该正确渲染函数信息")
    void stackView_ShouldRenderFrameInformation() throws Exception {
        runAndWait(() -> {
            ReactiveStackView stackView = new ReactiveStackView(stateModel);
            
            // 模拟栈帧数据
            stateModel.pushStackFrame(new StackFrame(
                createTestSymbol("main", 0, 0),
                0x100,
                new int[]{1, 2, 3}
            ));
            
            Thread.sleep(100);
            
            ListView<String> stackList = stackView.getStackList();
            
            assertThat(stackList.getItems()).isNotEmpty();
            assertThat(stackList.getItems().get(0)).isNotNull();
            
            String firstItem = stackList.getItems().get(0);
            assertThat(firstItem).contains("函数: main");
            assertThat(firstItem).contains("返回: 0x0100");
            assertThat(firstItem).contains("帧基址: 0x0100");
            
            Platform.runLater(() -> {
                stackList.getItems().clear();
            }, Throwable::printStackTrace);
        });
    }

    @Test
    @DisplayName("栈帧Cell应该显示寄存器信息")
    void stackView_ShouldDisplayRegisterInformation() throws Exception {
        runAndWait(() -> {
            ReactiveStackView stackView = new ReactiveStackView(stateModel);
            
            // 模拟栈帧数据
            stateModel.pushStackFrame(new StackFrame(
                createTestSymbol("testFunc", 1, 2, 0),
                0x100,
                new int[]{10, 20, 30, 40, 50, 60}
            ));
            
            Thread.sleep(100);
            
            ListView<String> stackList = stackView.getStackList();
            
            assertThat(stackList.getItems()).isNotEmpty();
            
            String frameText = stackList.getItems().get(0);
            
            // 验证函数信息
            assertThat(frameText).contains("函数: testFunc");
            assertThat(frameText).contains("返回: 0x0100");
            
            // 验证保存的寄存器（应该显示5个寄存器：r1-r3, r5-r7, ra-r1）
            assertThat(frameText).contains("r1=0x0000000A (10)");
            assertThat(frameText).contains("r2=0x00000014 (20)");
            assertThat(frameText).contains("r3=0x0000001E (30)");
            assertThat(frameText).contains("r5=0x00000028 (40)");
            assertThat(frameText).contains("r7=0x00000032 (50)");
            assertThat(frameText).contains("ra=0x0000003C (60)");
            
            Platform.runLater(() -> {
                stackList.getItems().clear();
            }, Throwable::printStackTrace);
        });
    }

    @Test
    @DisplayName("栈帧跟踪应该正确处理函数返回")
    void stackView_ShouldHandleFunctionReturn() throws Exception {
        runAndWait(() -> {
            ReactiveStackView stackView = new ReactiveStackView(stateModel);
            
            // 调用主函数并等待返回
            loadTestProgram("li r1, 0\n" +
                           "push r1, 42\n" +
                           "add r2, 43\n" +
                           "ret\n");
            
            Thread.sleep(100);
            
            assertThat(stateModel.getCallStackDepth()).isEqualTo(0);
            
            Platform.runLater(() -> {
                stackList.getItems().clear();
            }, Throwable::printStackTrace);
        });
    }

    @Test
    @DisplayName("栈帧跟踪应该显示深度信息")
    void stackView_ShouldDisplayDepthInformation() throws Exception {
        runAndWait(() -> {
            ReactiveStackView stackView = new ReactiveStackView(stateModel);
            
            loadTestProgram("li r1, 42\n" +
                           "push r1, 42\n" +
                           "push r2, 43\n" +
                           "push r3, 44\n" +
                           "push r4, 45\n" +
                           "ret\n");
            
            Thread.sleep(200);
            
            assertThat(stateModel.getCallStackDepth()).isEqualTo(4);
            
            ListView<String> stackList = stackView.getStackList();
            
            assertThat(stackList.getItems()).isNotEmpty();
            assertThat(stackList.getItems().get(0)).contains("深度: [0]");
            assertThat(stackList.getItems().get(1)).contains("深度: [1]");
            assertThat(stackList.getItems().get(2)).contains("深度: [2]");
            assertThat(stackList.getItems().get(3)).contains("深度: [3]");
            assertThat(stackList.getItems().get(4)).contains("深度: [4]");
        });
    }

    @Test
    @DisplayName("栈帧跟踪应该正确处理深度变化")
    void stackView_ShouldHandleDepthChanges() throws Exception {
        runAndWait(() -> {
            ReactiveStackView stackView = new ReactiveStackView(stateModel);
            
            int initialDepth = stateModel.getCallStackDepth();
            assertThat(initialDepth).isEqualTo(4);
            
            Platform.runLater(() -> {
                // 弹出所有栈帧
                for (int i = 4; i >= 0; i--) {
                    stateModel.popStackFrame();
                }
                
                Thread.sleep(100);
                
                int newDepth = stateModel.getCallStackDepth();
                assertThat(newDepth).isEqualTo(0);
                
                Platform.runLater(() -> {
                    stackList.getItems().clear();
                }, Throwable::printStackTrace);
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
            
            Thread.sleep(150);
            
            assertThat(stateModel.getCallStackDepth()).isEqualTo(4);
            
            ListView<String> stackList = stackView.getStackList();
            
            assertThat(stackList.getItems()).isNotEmpty();
            assertThat(stackList.getItems()).get(0)).contains("函数: funcA");
            assertThat(stackList.getItems().get(1)).contains("函数: funcB");
            assertThat(stackList.getItems().get(2)).contains("函数: funcC");
            assertThat(stackList.getItems().get(3)).contains("函数: funcC");
            
            Platform.runLater(() -> {
                stackList.getItems().clear();
            }, Throwable::printStackTrace);
        });
    }

    @Test
    @DisplayName("栈帧跟踪应该正确处理空栈")
    void stackView_ShouldHandleEmptyStack() throws Exception {
        runAndWait(() -> {
            ReactiveStackView stackView = new ReactiveStackView(stateModel);
            
            int initialDepth = stateModel.getCallStackDepth();
            assertThat(initialDepth).isNotZero();
            
            Platform.runLater(() -> {
                // 清空所有栈帧
                for (int i = 0; i < initialDepth; i++) {
                    stateModel.popStackFrame();
                }
                
                Thread.sleep(100);
                
                int newDepth = stateModel.getCallStackDepth();
                assertThat(newDepth).isEqualTo(0);
                
                Platform.runLater(() -> {
                    stackList.getItems().clear();
                }, Throwable::printStackTrace);
            });
        });
    }

    private FunctionSymbol createTestSymbol(String name, int nargs, int localsSize) {
        return new FunctionSymbol(name, nargs, localsSize, 0, new int[nargs + 1]);
    }

    private void loadTestProgram(String program) throws Exception {
        visualBridge.loadCode(new java.io.ByteArrayInputStream(program.getBytes()));
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
}
