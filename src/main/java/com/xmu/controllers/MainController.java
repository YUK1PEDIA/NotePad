package com.xmu.controllers;

import com.xmu.NotePadApplication;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.Optional;
import java.util.Stack;

public class MainController {
    // 存放当前文件
    private File currentFile;
    // undoStack用于实现撤销
    private Stack<String> undoStack = new Stack<>();
    private boolean ignoreChange = false; // 用于标记是否忽略变化事件，防止重复压入栈
    private boolean isSave; // 记录用户是否保存了文件
    private boolean isChange;

    @FXML
    private MenuItem autoWrapMenuItem;

    @FXML
    private TextArea textArea;

    @FXML
    private Label positionLabel;

    @FXML
    private Label encodingLabel;

    // 初始化
    @FXML
    public void initialize() {
        // 初始化右键菜单
        ContextMenu contextMenu = new ContextMenu();

        // 创建菜单项
        MenuItem copyItem = new MenuItem("复制");
        MenuItem pasteItem = new MenuItem("粘贴");
        MenuItem cutItem = new MenuItem("剪切");
        MenuItem selectAllItem = new MenuItem("全选");

        // 为菜单项添加事件处理程序
        copyItem.setOnAction(event -> copyText());
        pasteItem.setOnAction(event -> pasteText());
        cutItem.setOnAction(event -> cutText());
        selectAllItem.setOnAction(event -> selectAllText());

        // 将菜单项添加到右键菜单
        contextMenu.getItems().addAll(copyItem, pasteItem, cutItem, selectAllItem);

        // 设置TextArea的右键菜单
        textArea.setContextMenu(contextMenu);


        textArea.setWrapText(false); // 默认不自动换行
        encodingLabel.setText("UTF-8"); // 编码为UTF-8

        // 设置监听器，当光标位置改变时更新行列号
        textArea.caretPositionProperty().addListener((obs, oldPos, newPos) -> updateCaretPosition());


        // 将MainController放到controllers容器中，给另外两个controller调用
        NotePadApplication.controllers.put(this.getClass().getSimpleName(), this);
        // 监听TextArea的文本变化
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!ignoreChange) {
                saveToUndoStack(oldValue);
            }
            // 初始化isChange和isSave，确保第一次打开记事本能提示用户保存
            isChange = true;
            isSave = false;
        });
    }

    // 新建
    @FXML
    void New() {
        // 若未保存并且文本发生变化
        if (!isSave && isChange) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("新建");
            alert.setHeaderText("当前文件未保存");
            alert.setContentText("是否保存当前文件？");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Save();
            } else if (result.isPresent() && result.get() == ButtonType.CANCEL) {
                alert.close();
                textArea.clear();
                return; // 取消新建操作
            }
        }

        // 清空文本并准备编辑新文件
        textArea.setText("");
        // 重置状态
        isSave = false;
        isChange = false;
    }

    // 打开
    @FXML
    void Open() {
        // 弹出对话框
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("打开");
        // 只能打开txt文本内容
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("文本文件 (*.txt)", "*.txt"));
        // 读入打开的内容
        currentFile = fileChooser.showOpenDialog(new Stage());

        if (currentFile != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(currentFile))){
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                // 将文本内容加载到文本区域中
                textArea.setText(content.toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // 保存
    @FXML
    void Save() {
        // 文件已保存
        isSave = true;
        isChange = false;
        // 获取用户输入的文本内容
        String content = textArea.getText();
        if (currentFile != null) {
            saveToFile(currentFile, content);
        } else {
            // 弹出对话框
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("保存");
            // 设置文件扩展名过滤器，用于限制用户只能选择.txt文件
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("文本文件 (*.txt)", "*.txt"));
            File file = fileChooser.showSaveDialog(new Stage());
            // 写入文件
            if (file != null) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    writer.write(content);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    // 另存为
    @FXML
    void SaveAs() {
        // 获取输入的内容
        String content = textArea.getText();
        // 弹出对话框
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("另存为");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("文本文件 (*.txt)", "*.txt"));
        File file = fileChooser.showSaveDialog(new Stage());

        if (file != null) {
            try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(content);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // 撤销
    @FXML
    void Undo() {
        if (!undoStack.isEmpty()) {
            textArea.getText();
            // 将之前的内容弹栈
            String previousText = undoStack.pop();
            ignoreChange = true; // 防止重复压栈
            textArea.setText(previousText);
            ignoreChange = false; // 撤销完成后可以压栈
        }
    }

    // 重做
    @FXML
    void Redo() {
        textArea.clear();
    }

    // 通过find打开查找与替换窗口
    @FXML
    void Find() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/xmu/find-view.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("查找与替换");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 通过font打开字体设置窗口
    @FXML
    void Font() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/xmu/font-view.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("字体设置");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 自动换行
    @FXML
    void autoWrapText() {
        boolean currentWrap = textArea.isWrapText();
        textArea.wrapTextProperty().set(!currentWrap);
        updateAutoWrapMenuItemText();
    }

    // 保存当前文件
    private void saveToFile(File file, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // 将当前文本保存到undoStack中
    private void saveToUndoStack(String oldValue) {
        undoStack.push(oldValue);
    }


    // 获取文本区域的内容
    public TextArea getTextContent() {
        return textArea;
    }

    private void updateAutoWrapMenuItemText() {
        if (textArea.isWrapText()) {
            autoWrapMenuItem.setText("取消自动换行");
        } else {
            autoWrapMenuItem.setText("自动换行");
        }
    }

    // 更新行列号
    private void updateCaretPosition() {
        // 获取光标当前的位置
        int caretPosition = textArea.getCaretPosition();
        // 获取文本框中从索引0到当前光标位置的文本内容
        String textUpToCaret = textArea.getText(0, caretPosition);

        // 计算行号（换行符的个数+1即为文本行数）
        int row = textUpToCaret.split("\n", -1).length;

        // 计算列号
        // 如果lastNewLineIndex等于-1，表示没有换行符，那么当前列号就是光标位置caretPosition加1，因为列号是从1开始计数的
        int lastNewLineIndex = textUpToCaret.lastIndexOf("\n");
        int col = (lastNewLineIndex == -1) ? caretPosition + 1 : caretPosition - lastNewLineIndex;

        positionLabel.setText("行" + row + "，列" + col);
    }

    // 复制文本
    private void copyText() {
        // 获取选中的文本内容
        String selectedText = textArea.getSelectedText();
        // 先判断是否为空
        if (!selectedText.isEmpty()) {
            // 剪切板
            ClipboardContent content = new ClipboardContent();
            content.putString(selectedText);
            // 复制到剪切板
            Clipboard.getSystemClipboard().setContent(content);
        }
    }

    // 粘贴文本
    private void pasteText() {
        // 从剪切板获取
        Clipboard clipboard = Clipboard.getSystemClipboard();
        // 如果剪切板不为空，将剪切板上的内容直接接到textarea内容的后面
        if (clipboard.hasString()) {
            int caretPosition = textArea.getCaretPosition();
            textArea.insertText(caretPosition, clipboard.getString());
        }
    }

    // 剪切文本
    private void cutText() {
        // 记录选中的文本
        String selectedText = textArea.getSelectedText();
        if (!selectedText.isEmpty()) {
            ClipboardContent content = new ClipboardContent();
            content.putString(selectedText);
            Clipboard.getSystemClipboard().setContent(content);
            int start = textArea.getSelection().getStart();
            int end = textArea.getSelection().getEnd();
            // 复制完成后删除原textarea原来的内容
            textArea.deleteText(start, end);
        }
    }

    // 全选文本
    private void selectAllText() {
        textArea.selectAll();
    }
}