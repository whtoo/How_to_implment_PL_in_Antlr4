package org.teachfx.antlr4.ep18r.vizvmr.ui;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep18r.stackvm.config.VMConfig;
import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;
import org.teachfx.antlr4.ep18r.vizvmr.core.VMRStateModel;
import org.teachfx.antlr4.ep18r.vizvmr.integration.VMRVisualBridge;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.*;

/**
 * MainFrame Swing UI Integration Tests
 * 测试 MainFrame 与 VMRVisualBridge 的集成
 */
@DisplayName("MainFrame UI 集成测试")
class MainFrameUITest {

    private RegisterVMInterpreter vm;
    private VMRStateModel stateModel;
    private VMRVisualBridge visualBridge;
    private MainFrame mainFrame;

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
        mainFrame = new MainFrame(visualBridge);
    }

    @AfterEach
    void tearDown() {
        if (visualBridge != null) {
            visualBridge.stop();
        }
        if (mainFrame != null) {
            mainFrame.setVisible(false);
        }
    }

    @Test
    @DisplayName("MainFrame 应该正确初始化")
    void mainFrame_ShouldInitializeCorrectly() {
        assertThat(mainFrame).isNotNull();
        assertThat(mainFrame.getTitle()).contains("EP18R");
    }

    @Test
    @DisplayName("面板 getter 应该返回正确实例")
    void panelGetters_ShouldReturnCorrectInstances() {
        assertThat(mainFrame.getRegisterPanel()).isNotNull();
        assertThat(mainFrame.getMemoryPanel()).isNotNull();
        assertThat(mainFrame.getCodePanel()).isNotNull();
        assertThat(mainFrame.getStackPanel()).isNotNull();
        assertThat(mainFrame.getControlPanel()).isNotNull();
        assertThat(mainFrame.getStatusPanel()).isNotNull();
    }

    @Test
    @DisplayName("VMRVisualBridge 应该正确连接 VM 和状态模型")
    void visualBridge_ShouldConnectVMAndStateModel() {
        assertThat(visualBridge.getVM()).isSameAs(vm);
        assertThat(visualBridge.getStateModel()).isSameAs(stateModel);
    }

    @Test
    @DisplayName("代码加载后 VMRVisualBridge 状态应该更新")
    void codeLoad_ShouldUpdateBridgeState() throws Exception {
        InputStream input = getClass().getClassLoader().getResourceAsStream("t.vmr");
        assertThat(input).isNotNull();

        boolean hasErrors = visualBridge.loadCode(input);
        assertThat(hasErrors).isFalse();
        
        assertThat(vm.getCode()).isNotNull();
        assertThat(vm.getCodeSize()).isGreaterThan(0);
    }

    @Test
    @DisplayName("控制流程应该正确工作")
    void controlFlow_ShouldWorkCorrectly() throws Exception {
        // 加载代码
        InputStream input = getClass().getClassLoader().getResourceAsStream("t.vmr");
        assertThat(input).isNotNull();

        boolean hasErrors = visualBridge.loadCode(input);
        assertThat(hasErrors).isFalse();

        // 开始
        visualBridge.start();
        assertThat(visualBridge.isRunning()).isTrue();

        // 暂停
        visualBridge.pause();
        assertThat(visualBridge.isPaused()).isTrue();

        // 继续
        visualBridge.resume();
        assertThat(visualBridge.isPaused()).isFalse();

        // 停止
        visualBridge.stop();
        assertThat(visualBridge.isRunning()).isFalse();
    }

    @Test
    @DisplayName("单步执行模式设置应该工作")
    void stepMode_ShouldBeSettable() {
        // 验证 VM 支持单步模式
        vm.setStepMode(true);
        assertThat(vm.isStepMode()).isTrue();
        
        vm.setStepMode(false);
        assertThat(vm.isStepMode()).isFalse();
    }

    @Test
    @DisplayName("执行完成应该触发回调")
    void executionFinished_ShouldTriggerCallback() throws Exception {
        InputStream input = getClass().getClassLoader().getResourceAsStream("mov_test.vmr");
        assertThat(input).isNotNull();

        boolean hasErrors = visualBridge.loadCode(input);
        assertThat(hasErrors).isFalse();

        visualBridge.start();
        
        // 等待执行完成（mov_test.vmr 很小，应该很快完成）
        int maxWait = 5000;
        int waited = 0;
        while (visualBridge.isRunning() && waited < maxWait) {
            Thread.sleep(100);
            waited += 100;
        }

        assertThat(visualBridge.isRunning()).isFalse();
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
