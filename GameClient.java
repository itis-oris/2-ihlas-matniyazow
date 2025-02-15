package com.laberint_2;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class GameClient extends Application {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 5000;
    private static Socket socket;
    private static BufferedReader reader;
    private static PrintWriter writer;
    private static String playerName;

    public static void setPlayerName(String name) {
        playerName = name;
    }

    public static void startGame(String name) {
        playerName = name;  // сохраняем имя игрока
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("Подключено к серверу, имя: " + playerName);
            writer.println("NAME " + playerName); // Отправляем имя на сервер

            // Запуск игрового окна
            Platform.runLater(() -> {
                try {
                    new LabyrinthGame().start(new Stage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            System.out.println("Ошибка подключения к серверу.");
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/laberint_2/login.fxml"));
        Scene scene = new Scene(loader.load(), 300, 200);

        LoginController controller = loader.getController();
        controller.setStage(primaryStage);

        primaryStage.setTitle("Вход в игру");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static PrintWriter getWriter() {
        return writer;
    }

    public static BufferedReader getReader() {
        return reader;
    }

    public static String getPlayerName() {
        return playerName;
    }

    public static Socket getSocket() {
        return socket;
    }

    public static void main(String[] args) {
        launch(args); // Запускаем окно ввода имени
    }
}