package com.laberint_2;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GameServer {
    private static final int PORT = 5000;
    private static final List<Socket> clients = new ArrayList<>();
    private static final List<Player> players = new ArrayList<>();

    private static final int[][] maze = {
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

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Сервер запущен... Ожидание подключений...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                clients.add(clientSocket);
                System.out.println("Игрок подключился: " + clientSocket.getInetAddress());

                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void handleClient(Socket clientSocket) {
        Player player = new Player(clientSocket);
        players.add(player);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

            writer.println("Добро пожаловать! Двигайтесь стрелками.");

            String message;
            while ((message = reader.readLine()) != null) {

                if (message.startsWith("NAME")) {
                    // Сохраняем имя игрока
                    String name = message.substring(5); // Извлекаем имя из сообщения
                    player.setName(name); // Устанавливаем имя игрока
                    System.out.println("Игрок " + name + " подключился.");
                    continue; // Переходим к следующему сообщению
                }
                System.out.println("Получено: " + message);

                if (message.startsWith("MOVE")) {
                    // Обновляем позицию игрока
                    String[] parts = message.split(" ");
                    int newX = Integer.parseInt(parts[1]);
                    int newY = Integer.parseInt(parts[2]);
                    player.setX(newX);
                    player.setY(newY);

                    // Отправляем обновленные позиции всем клиентам
                    broadcastPlayerPositions();
                } else if (message.startsWith("COIN COLLECTED")) {
                    String[] parts = message.split(" ");
                    int coinX = Integer.parseInt(parts[2]);
                    int coinY = Integer.parseInt(parts[3]);

                    // Обновляем лабиринт на сервере
                    maze[coinY][coinX] = 0;

                    // Отправляем обновление всем клиентам
                    broadcastMessage("COIN COLLECTED " + coinX + " " + coinY);
                } else if (message.startsWith("WIN")) {
                    broadcastMessage("Игра окончена! Победил игрок: " + player.getName());
                }
            }
        } catch (IOException e) {
            System.out.println("Игрок отключился: " + clientSocket.getInetAddress());
        } finally {
            players.remove(player);
            clients.remove(clientSocket);
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void broadcastPlayerPositions() {
        StringBuilder positions = new StringBuilder("PLAYERS ");
        for (Player player : players) {
            positions.append(player.getX()).append(" ").append(player.getY()).append(" ");
        }
        broadcastMessage(positions.toString());
    }

    private static void broadcastMessage(String message) {

        for (Socket client : clients) {
            try {
                PrintWriter writer = new PrintWriter(client.getOutputStream(), true);
                writer.println(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}