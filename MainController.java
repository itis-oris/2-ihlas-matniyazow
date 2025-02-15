package com.laberint_2;

import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainController {
    private int[][] maze = {
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1},
            {1, 0, 1, 1, 0, 1, 0, 1, 1, 1, 0, 1, 0, 1, 0, 1},
            {1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 2, 0, 1, 0, 1},
            {1, 1, 1, 0, 1, 1, 0, 1, 0, 1, 1, 1, 0, 1, 0, 1},
            {1, 0, 0, 0, 1, 0, 0, 1, 0, 2, 0, 0, 0, 1, 0, 1},
            {1, 0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 0, 1, 0, 1},
            {1, 0, 0, 0, 1, 0, 2, 0, 0, 0, 2, 1, 0, 1, 0, 1},
            {1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 1},
            {1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 0, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
    };

    private int playerX = 1, playerY = 1; // Начальная позиция игрока
    private int collectedCoins = 0;
    private int finishX = 14; // Координаты двери
    private int finishY = 10;


    // Графические элементы
    private static final int WIDTH = 800, HEIGHT = 600, CELL_SIZE = 40;
    @FXML
    private Canvas gameCanvas;
    private GraphicsContext gc;

    // Изображения
    private Image playerImage = new Image(getClass().getResource("/player.png").toExternalForm());
    private Image wallImage = new Image(getClass().getResource("/wall.png").toExternalForm());
    private Image floorImage = new Image(getClass().getResource("/floor.png").toExternalForm());
    private Image finishImage = new Image(getClass().getResource("/finish.png").toExternalForm());

    // Сетевые переменные
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    @FXML
    public void initialize() {
        gc = gameCanvas.getGraphicsContext2D();
        drawMaze();
        drawPlayer();
        setupEventHandlers();

        try {
            socket = GameClient.getSocket();
            writer = GameClient.getWriter();
            reader = GameClient.getReader();
            listenForServerUpdates();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawMaze() {
        gc.clearRect(0, 0, WIDTH, HEIGHT);
        for (int y = 0; y < maze.length; y++) {
            for (int x = 0; x < maze[0].length; x++) {
                if (Math.abs(playerX - x) <= 1 && Math.abs(playerY - y) <= 1) {
                    if (maze[y][x] == 1) {
                        gc.drawImage(wallImage, x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                    } else if (maze[y][x] == 3) {
                        gc.drawImage(finishImage, x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                    } else {
                        gc.drawImage(floorImage, x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                    }
                    if (maze[y][x] == 2) {
                        gc.setFill(Color.GOLD);
                        gc.fillOval(x * CELL_SIZE + 10, y * CELL_SIZE + 10, CELL_SIZE - 20, CELL_SIZE - 20);
                    }
                } else {
                    gc.setFill(Color.BLACK);
                    gc.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }
    }

    private void drawPlayer() {
        double offset = CELL_SIZE * 0.2; // Смещение внутрь клетки
        double size = CELL_SIZE * 0.6; // Размер персонажа меньше клетки
        gc.drawImage(playerImage, playerX * CELL_SIZE + offset, playerY * CELL_SIZE + offset, size, size);
    }

    private void setupEventHandlers() {
        gameCanvas.setOnKeyPressed(event -> {
            int newX = playerX;
            int newY = playerY;

            if (event.getCode() == KeyCode.UP) {
                newY--;
            } else if (event.getCode() == KeyCode.DOWN) {
                newY++;
            } else if (event.getCode() == KeyCode.LEFT) {
                newX--;
            } else if (event.getCode() == KeyCode.RIGHT) {
                newX++;
            }

            if (maze[newY][newX] != 1) {
                playerX = newX;
                playerY = newY;
                writer.println("MOVE " + newX + " " + newY);

                if (maze[newY][newX] == 2) {
                    maze[newY][newX] = 0;
                    collectedCoins++;
                    writer.println("COIN COLLECTED " + newX + " " + newY);

                    if (collectedCoins == 3) {
                        maze[finishY][finishX] = 3;
                        writer.println("FINISH APPEARED");
                    }
                }
                if (maze[newY][newX] == 3) {
                    writer.println("WIN " + playerX + " " + playerY);
                    System.out.println("Поздравляем! Вы выиграли!");
                    Platform.exit();
                }
            }

            drawMaze();
            drawPlayer();
        });

        gameCanvas.setFocusTraversable(true);
    }

    private void listenForServerUpdates() {
        new Thread(() -> {
            try {
                String message;
                while ((message = reader.readLine()) != null) {
                    System.out.println("Сервер: " + message);

                    if (message.startsWith("PLAYERS")) {
                        // Обработка позиций других игроков
                        String[] parts = message.split(" ");
                        List<int[]> otherPlayers = new ArrayList<>();
                        for (int i = 1; i < parts.length; i += 2) {
                            int x = Integer.parseInt(parts[i]);
                            int y = Integer.parseInt(parts[i + 1]);
                            otherPlayers.add(new int[]{x, y});
                        }

                        // Отладочный вывод
                        System.out.println("Другие игроки: " + otherPlayers);

                        Platform.runLater(() -> {
                            drawMaze();
                            drawPlayer();
                            drawOtherPlayers(otherPlayers, playerX, playerY);
                        });
                    } else if (message.startsWith("COIN COLLECTED")) {
                        // Обработка сбора монет
                        String[] parts = message.split(" ");
                        int coinX = Integer.parseInt(parts[2]);
                        int coinY = Integer.parseInt(parts[3]);

                        // Обновляем лабиринт на клиенте
                        maze[coinY][coinX] = 0;

                        // Перерисовываем лабиринт
                        Platform.runLater(() -> {
                            drawMaze();
                            drawPlayer();
                        });
                    } else if (message.startsWith("WIN")) {
                        // Обработка победы
                        System.out.println("Игра окончена! Кто-то победил!");
                        Platform.runLater(() -> {
                            System.out.println("Победил другой игрок!");
                            Platform.exit();
                        });
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void drawOtherPlayers(List<int[]> otherPlayers, int myX, int myY) {
        for (int[] pos : otherPlayers) {
            if (pos[0] != myX || pos[1] != myY) {
                System.out.println("Отрисовка другого игрока: " + pos[0] + ", " + pos[1]); // Отладочный вывод
                gc.setFill(Color.BLUE); // Цвет других игроков
                gc.fillOval(pos[0] * CELL_SIZE + 10, pos[1] * CELL_SIZE + 10, CELL_SIZE - 20, CELL_SIZE - 20);
            }
        }
    }
}