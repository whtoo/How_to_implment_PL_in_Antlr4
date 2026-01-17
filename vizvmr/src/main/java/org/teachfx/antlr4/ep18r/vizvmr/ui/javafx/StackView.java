package org.teachfx.antlr4.ep18r.vizvmr.ui.javafx;

import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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
    private final ScrollPane scrollPane;

    public StackView() {
        this.treeView = new TreeView<>();
        this.treeView.setShowRoot(false);
        this.treeView.setCellFactory(tv -> new javafx.scene.control.TreeCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    setText(item);
                    setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 12px;");
                } else {
                    setText(null);
                    setGraphic(null);
                }
            }
        });

        this.scrollPane = new ScrollPane(treeView);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        Label header = new Label("调用栈");
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #666666;");

        getChildren().addAll(header, scrollPane);
        setStyle("-fx-spacing: 5; -fx-padding: 10;");
        setMaxHeight(Double.MAX_VALUE);
        setMaxWidth(Double.MAX_VALUE);
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
