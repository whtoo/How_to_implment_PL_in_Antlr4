package org.teachfx.antlr4.ep18r.vizvmr.ui.javafx;

import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;

/**
 * 调用栈视图组件
 *
 * <p>垂直列表显示调用栈帧信息</p>
 */
public class StackView extends VBox {

    private final TreeView<String> treeView;

    public StackView() {
        this.treeView = new TreeView<>();

        Label header = new Label("调用栈");
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #666666;");

        getChildren().addAll(header, treeView);
        setStyle("-fx-spacing: 5; -fx-padding: 10;");
    }

    public void updateStack(int[] callStack) {
        treeView.setRoot(null);

        if (callStack == null || callStack.length == 0) {
            TreeItem<String> root = new TreeItem<>("调用栈: 空");
            treeView.setRoot(root);
            return;
        }

        TreeItem<String> root = new TreeItem<>("调用栈");

        for (int i = callStack.length - 1; i >= 0; i--) {
            String frameInfo = String.format("Frame %d [PC=0x%04X]", i, callStack[i]);
            TreeItem<String> frameItem = new TreeItem<>(frameInfo);

            TreeItem<String> localsItem = new TreeItem<>("局部变量: 0");
            TreeItem<String> locals = new TreeItem<>("空");
            localsItem.getChildren().add(locals);
            frameItem.getChildren().add(localsItem);

            root.getChildren().add(frameItem);
        }

        treeView.setRoot(root);
    }
}
