package com.game;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TCPConnection {

    private final Socket socket;
    private final Thread rxThread;
    private final BufferedReader in;
    private final BufferedWriter out;

    public TCPConnection(TCPNet eventListener, String ip, int port) throws IOException {
        this(eventListener, new Socket(ip, port));
    }

    public TCPConnection(TCPNet eventListener, Socket socket) throws IOException {
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(),
                StandardCharsets.UTF_8));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),
                StandardCharsets.UTF_8));
        rxThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    eventListener.onConnection(TCPConnection.this);
                    while (!rxThread.isInterrupted()) {
                        eventListener.onReceive(TCPConnection.this, in.readLine());
                    }
                } catch (IOException ignored) {
                } finally {
                    eventListener.onDisconnect(TCPConnection.this);
                }
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
        } catch (IOException ignored) {
        }
    }

}
