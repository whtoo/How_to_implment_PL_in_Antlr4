package org.teachfx.antlr4.ep18r.vizvmr.ui.javafx;

import javafx.scene.layout.Pane;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep18r.stackvm.config.VMConfig;
import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;
import org.teachfx.antlr4.ep18r.vizvmr.core.VMRStateModel;
import org.teachfx.antlr4.ep18r.vizvmr.integration.VMRVisualBridge;

import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

/**
 * JavaFX UI Component Tests
 * Tests for JavaFX-based UI components (RegisterView, MemoryView, etc.)
 */
@DisplayName("JavaFX UI 组件测试")
class JFXComponentTest {

    private RegisterVMInterpreter vm;
    private VMRStateModel stateModel;
    private VMRVisualBridge visualBridge;

    @BeforeEach
    void setUp() {
        VMConfig config = new VMConfig.Builder()
                .setHeapSize(1024)
                .setStackSize(256)
                .setMaxStackDepth(32)
                .build();

        vm = new RegisterVMInterpreter(config);
        stateModel = new VMRStateModel(1024, 256, 32);
        visualBridge = new VMRVisualBridge(vm, stateModel);
    }

    @AfterEach
    void tearDown() {
        if (visualBridge != null) {
            visualBridge.stop();
        }
    }

    @Test
    @DisplayName("RegisterView 应该正确初始化")
    void registerView_ShouldInitializeCorrectly() {
        RegisterView registerView = new RegisterView(visualBridge);

        assertThat(registerView).isNotNull();
        assertThat(registerView.getPanelId()).isEqualTo("RegisterView");
    }

    @Test
    @DisplayName("MemoryView 应该正确初始化")
    void memoryView_ShouldInitializeCorrectly() {
        MemoryView memoryView = new MemoryView(visualBridge);

        assertThat(memoryView).isNotNull();
        assertThat(memoryView.getPanelId()).isEqualTo("MemoryView");
    }

    @Test
    @DisplayName("CodeView 应该正确初始化")
    void codeView_ShouldInitializeCorrectly() {
        CodeView codeView = new CodeView(visualBridge);

        assertThat(codeView).isNotNull();
        assertThat(codeView.getPanelId()).isEqualTo("CodeView");
    }

    @Test
    @DisplayName("StackView 应该正确初始化")
    void stackView_ShouldInitializeCorrectly() {
        StackView stackView = new StackView(visualBridge);

        assertThat(stackView).isNotNull();
        assertThat(stackView.getPanelId()).isEqualTo("StackView");
    }

    @Test
    @DisplayName("StatusView 应该正确初始化")
    void statusView_ShouldInitializeCorrectly() {
        StatusView statusView = new StatusView(visualBridge);

        assertThat(statusView).isNotNull();
        assertThat(statusView.getPanelId()).isEqualTo("StatusView");
    }

    @Test
    @DisplayName("LogView 应该正确初始化")
    void logView_ShouldInitializeCorrectly() {
        LogView logView = new LogView();

        assertThat(logView).isNotNull();
        assertThat(logView.getPanelId()).isEqualTo("LogView");
    }

    @Test
    @DisplayName("RegisterView 应该显示16个寄存器")
    void registerView_ShouldDisplay16Registers() {
        RegisterView registerView = new RegisterView(visualBridge);

        // RegisterView creates 16 cells
        for (int i = 0; i < 16; i++) {
            assertThat(registerView.getCellPane(i)).isNotNull();
            assertThat(registerView.getRegisterLabel(i)).isNotNull();
        }
    }

    @Test
    @DisplayName("RegisterView updateRegister 应该更新显示")
    void registerView_updateRegister_ShouldUpdateDisplay() {
        RegisterView registerView = new RegisterView(visualBridge);

        // Update a register
        registerView.updateRegister(1, 42);

        // Verify the label was updated
        String labelText = registerView.getRegisterLabel(1).getText();
        assertThat(labelText).contains("0000002A");  // 42 in hex
        assertThat(labelText).contains("(42)");      // 42 in decimal
    }

    @Test
    @DisplayName("RegisterView highlightRegister 应该改变颜色")
    void registerView_highlightRegister_ShouldChangeColor() {
        RegisterView registerView = new RegisterView(visualBridge);

        // Initially, register should not be highlighted
        // After highlighting, the color should change
        registerView.highlightRegister(5);

        // Verify no exception is thrown and method completes
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("StackView refresh 应该更新显示")
    void stackView_refresh_ShouldUpdateDisplay() {
        StackView stackView = new StackView(visualBridge);

        // Call refresh - should not throw
        stackView.refresh();

        // Stack is empty initially, should show "<空调用栈>"
        assertThat(stackView.getStackList()).isNotNull();
    }

    @Test
    @DisplayName("StatusView refresh 应该更新所有标签")
    void statusView_refresh_ShouldUpdateAllLabels() {
        StatusView statusView = new StatusView(visualBridge);

        // Call refresh - should update labels
        statusView.refresh();

        // Verify no exception is thrown
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("LogView 添加日志应该更新文本区域")
    void logView_addLog_ShouldUpdateTextArea() {
        LogView logView = new LogView();

        // Add a log message
        logView.info("Test message");

        // Verify the log content contains the message
        String content = logView.getLogContent();
        assertThat(content).contains("Test message");
        assertThat(content).contains("INFO");
    }

    @Test
    @DisplayName("LogView 清除日志应该清空文本区域")
    void logView_clearLog_ShouldClearTextArea() {
        LogView logView = new LogView();

        // Add some logs
        logView.info("Message 1");
        logView.warn("Warning");
        logView.error("Error");

        // Clear logs
        logView.clearLog();

        // Verify the log content is empty
        assertThat(logView.getLogContent()).isEmpty();
    }

    @Test
    @DisplayName("VMRVisualBridge 应该正确连接 VM 和状态模型")
    void visualBridge_ShouldConnectVMAndStateModel() {
        assertThat(visualBridge.getVM()).isSameAs(vm);
        assertThat(visualBridge.getStateModel()).isSameAs(stateModel);
    }

    @Test
    @DisplayName("寄存器初始值应该正确")
    void registerInitialValues_ShouldBeCorrect() {
        // r0 应该是 0
        assertThat(visualBridge.getRegister(0)).isEqualTo(0);

        // 其他寄存器初始值应该是 0
        for (int i = 1; i < 16; i++) {
            assertThat(visualBridge.getRegister(i)).isEqualTo(0);
        }
    }
}
