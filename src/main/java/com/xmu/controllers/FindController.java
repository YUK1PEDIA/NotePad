package com.xmu.controllers;

import com.xmu.NotePadApplication;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class FindController {
    // 替换文本框
    @FXML
    private TextField replaceTextFiled;

    // 查找文本框
    @FXML
    private TextField findTextField;

    // 初始化，在FindController中拿到MainController
    @FXML
    void initialize() {
        // 调用MainController
        MainController controller = (MainController) NotePadApplication.controllers.get(MainController.class.getSimpleName());
        // 获取当前文本框的内容
        noteTextArea = controller.getTextContent();
    }

    // 判断是否能查找到目标文本
    boolean flag = false;
    // 查找上一个
    @FXML
    void findPrev() {
        // 先获取用户输入的查找内容
        String searchText = findTextField.getText();
        // 判断是否为空
        if (searchText.isEmpty()) {
            showAlert("查找", "请输入查找内容！");
            return;
        }

        // 若搜索内容变更，重置fromIndex
        if (!searchText.equals(lastSearchText)) {
            // 和查找下一个不同的是，这里fromIndex重置为用户输入的文本长度
            fromIndex = noteTextArea.getText().length();
            lastSearchText = searchText;
        }

        String text = noteTextArea.getText();
        // 从后往前找
        startIndex = text.lastIndexOf(searchText, fromIndex - searchText.length() - 1);

        if (startIndex == -1) {
            if (flag) {
                showAlert("查找", "已经是第一条！");
            } else {
                showAlert("查找", "未查找到目标文本！");
            }
        } else {
            noteTextArea.selectRange(startIndex, startIndex + searchText.length());
            fromIndex = startIndex;
            flag = true;
        }
        flag = false;
    }

    // 查找下一个
    @FXML
    void findNext() {
        // 先获取用户输入的查找内容
        String searchText = findTextField.getText();
        // 判断用户输入是否为空
        if (searchText.isEmpty()) {
            showAlert("查找", "请输入查找内容！");
            return;
        }

        // 如果搜索内容变更，重置fromIndex
        if (!searchText.equals(lastSearchText)) {
            fromIndex = 0;
            lastSearchText = searchText;
        }

        // 获取主界面文本内容
        String text = noteTextArea.getText();
        // 从头开始找目标文本，返回找到的第一个索引，若没找到则indexOf返回-1
        startIndex = text.indexOf(searchText, fromIndex);

        if (startIndex == -1) {
            if (flag) {
                showAlert("查找", "已经是最后一条！");
            } else {
                showAlert("查找", "未查找到目标文本！");
            }
        } else {
            noteTextArea.selectRange(startIndex, startIndex + searchText.length());
            fromIndex = startIndex + searchText.length();
            flag = true;
        }
        flag = false;
    }

    // 替换当前选中的文本
    @FXML
    void replace() {
        // 获取用户输入在文本框的内容
        String searchText = findTextField.getText();
        // 获取用户希望替换的内容
        String replaceText = replaceTextFiled.getText();
        if (searchText.isEmpty()) {
            showAlert("替换", "请输入查找内容！");
            return;
        }

        if (startIndex != -1) {
            noteTextArea.replaceText(startIndex, startIndex + searchText.length(), replaceText);
            fromIndex = startIndex + replaceText.length();
        } else {
            showAlert("替换", "没有找到匹配内容！");
        }
    }

    // 替换所有匹配的文本
    @FXML
    void replaceAll() {
        // 获取查找到的文本
        String searchText = findTextField.getText();
        // 获取需要替换的文本
        String replaceText = replaceTextFiled.getText();
        // 先判断用户输入的文本是否为空
        if (searchText.isEmpty()) {
            showAlert("替换全部", "请输入查找内容！");
            return;
        }

        // 获取输入框所有文本
        String text = noteTextArea.getText();
        // 替换所有符合要求的内容
        text = text.replaceAll(searchText, replaceText);
        // 更新输入文本框的内容
        noteTextArea.setText(text);
        showAlert("替换全部", "已完成替换！");
    }

    // 记录主界面中的文本内容
    private TextArea noteTextArea;

    // 起始索引
    private int startIndex = 0;
    private int fromIndex = 0;
    // 记录上一次搜索内容
    private String lastSearchText = "";

    // 显示警告
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}