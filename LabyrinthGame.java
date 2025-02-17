package com.laberint_2;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LabyrinthGame extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Загрузка FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/laberint_2/main.fxml"));
        MainController controller = new MainController();
        loader.setController(controller);

        // Создание сцены
        Scene scene = new Scene(loader.load(), 800, 600);

        // Настройка окна
        primaryStage.setResizable(false);
        primaryStage.setTitle("Лабиринт - " + GameClient.getPlayerName());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

}