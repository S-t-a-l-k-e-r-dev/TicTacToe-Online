package com.game;

public interface TCPNet {
    default void onConnection(TCPConnection tcpConnection) {}
    default void onReceive(TCPConnection tcpConnection, String value) {}
    default void onDisconnect(TCPConnection tcpConnection) {}

}

