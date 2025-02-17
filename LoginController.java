package com.laberint_2;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    @FXML
    private TextField nameField;

    @FXML
    private Button connectButton;

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void onConnectButtonClicked() {
        String playerName = nameField.getText().trim();
        if (!playerName.isEmpty()) {
            GameClient.setPlayerName(playerName);

            stage.close(); // Закрываем окно регистрации
            GameClient.startGame(playerName); // Запускаем игру
        } else {
            nameField.setPromptText("Введите имя!");
        }
    }
}