package tictactoe;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Connection {

    private final Socket socket;
    private Thread rxThread;
    private final BufferedReader in;
    private final BufferedWriter out;

    public Connection(GameServer server,Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        connectServer(server);
    }
    public Connection(GameClient user, String ip, int port) throws IOException {
        this.socket = new Socket(ip, port);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        connectUser(user);
    }

    private void connectUser(GameClient user) {
        rxThread = new Thread(() -> {
            try {
                while (!rxThread.isInterrupted()) {
                    user.onReceive(in.readLine());
                }
            } catch (IOException ignored) {
            }
        });
        rxThread.start();
    }

    private void connectServer(GameServer eventListener) {
        rxThread = new Thread(() -> {
            try {
                eventListener.onConnection(Connection.this);
                while (!rxThread.isInterrupted()) {
                    eventListener.onReceive(Connection.this, in.readLine());
                }
            } catch (IOException ignored) {
            } finally {
                eventListener.onDisconnect(Connection.this);
            }
        });
        rxThread.start();
    }

    public synchronized void sendString(String value) {
        try {
            out.write(value + "\r\n");
            out.flush();
        } catch (IOException e) {
            disconnect();
        }
    }

    public synchronized void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
