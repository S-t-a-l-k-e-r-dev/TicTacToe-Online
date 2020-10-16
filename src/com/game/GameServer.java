package com.game;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.LinkedList;

public class GameServer implements TCPNet {
    private final LinkedList<TCPConnection> room = new LinkedList<>();
    private final ArrayList<TCPConnection[]> connections = new ArrayList<>();

    public static void main(String[] args) {
        new GameServer();
    }

    private static int UserCount = 0;

    private GameServer() {
        System.out.println("Server running...");
        try (ServerSocket serverSocket = new ServerSocket(8189)) {
            while (true) {
                try {
                    new TCPConnection(this, serverSocket.accept());
                } catch (IOException e) {
                    System.out.println("TCPConnection exception: " + e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendToAllConnections(String value, TCPConnection tcp) {
        final int cnt = connections.size();
        if (cnt >= 1) {
            for (TCPConnection[] connection : connections) {
                String str = null;
                if (connection[0] == tcp) {
                    str = "O" + value;
                }
                if (connection[1] == tcp) {
                    str = "X" + value;
                }
                connection[0].sendString(str);
                connection[1].sendString(str);
            }
        }
    }

    public synchronized void putToServer() {
        if (room.size() >= 2) {
            TCPConnection[] con = new TCPConnection[2];
            con[0] = room.get(0);
            con[1] = room.get(1);
            room.pollFirst();
            room.pollFirst();
            connections.add(con);
        }
    }

    @Override
    public synchronized void onConnection(TCPConnection tcpConnection) {
        UserCount++;
        room.add(tcpConnection);
        putToServer();
        System.out.println(connections.size());
        sendSymbol();
    }

    public synchronized void sendSymbol() {
        if (connections.size() >= 1) {
            try {
                for (int i = 0; i < connections.size(); i++) {
                    connections.get(i)[0].sendString("C" + "O" + i);
                    connections.get(i)[1].sendString("C" + "X" + i);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public synchronized void onReceive(TCPConnection tcpConnection, String value) {
        sendToAllConnections(value, tcpConnection);
    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        int delete;
        for (int i = 0; i < connections.size(); i++) {
            if (connections.get(i)[0] == tcpConnection || connections.get(i)[1] == tcpConnection) {
                delete = i;
                connections.remove(delete);
                UserCount -= 2;
            }
        }
        room.remove(tcpConnection);
    }


}
