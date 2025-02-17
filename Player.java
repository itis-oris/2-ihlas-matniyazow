package com.laberint_2;

import java.net.Socket;
public class Player {
    private int x, y;
    private Socket socket;
    private String name; // Добавляем поле для имени

    public Player(Socket socket) {
        this.socket = socket;
        this.x = 1; // Начальная позиция
        this.y = 1;
        this.name = "Игрок"; // По умолчанию
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public Socket getSocket() { return socket; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
