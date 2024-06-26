package com.xmu.controllers;

import com.xmu.NotePadApplication;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

public class FontController {
    // 字体大小
    @FXML
    private ComboBox<Integer> fontSizeComboBox;

    // 字体样式
    @FXML
    private ComboBox<String> fontStyleComboBox;

    // 字体颜色
    @FXML
    private ComboBox<String> fontColorComboBox;

    // 确认按钮
    @FXML
    private Button confirmButton;

    // 文本框中的文本
    private TextArea noteTextArea;

    // 初始化
    @FXML
    void initialize() {
        MainController controller = (MainController) NotePadApplication.controllers.get(MainController.class.getSimpleName());
        noteTextArea = controller.getTextContent(); // 获取文本

        // 初始化字体样式选项
        fontStyleComboBox.setItems(FXCollections.observableArrayList("SimHei", "FangSong", "Times New Roman", "KaiTi"));
        fontStyleComboBox.setValue("SimHei");

        // 初始化字体大小
        fontSizeComboBox.setItems(FXCollections.observableArrayList(10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32));
        fontSizeComboBox.setValue(12);

        // 初始化字体颜色
        fontColorComboBox.setItems(FXCollections.observableArrayList("Black", "Red", "Green", "Blue", "Yellow"));
        fontColorComboBox.setValue("Black");

        // 确认按钮事件
        confirmButton.setOnAction(event -> applyFontChanges());
    }

    // 应用字体更改
    private void applyFontChanges() {
        String selectedFont = fontStyleComboBox.getValue();
        int selectedSize = fontSizeComboBox.getValue();
        String selectedColor = fontColorComboBox.getValue();

        Font font = Font.font(selectedFont, FontWeight.NORMAL, FontPosture.REGULAR, selectedSize);
        noteTextArea.setFont(font);

        switch (selectedColor.toLowerCase()) {
            case "red" -> noteTextArea.setStyle("-fx-text-fill: red;");
            case "green" -> noteTextArea.setStyle("-fx-text-fill: green;");
            case "blue" -> noteTextArea.setStyle("-fx-text-fill: blue;");
            case "yellow" -> noteTextArea.setStyle("-fx-text-fill: yellow;");
            default -> noteTextArea.setStyle("-fx-text-fill: black;");
        }

        showAlert("字体设置", "字体样式已完成设置！");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}