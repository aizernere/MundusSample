package com.mygdx.game;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GameServer {
    Server server;
    Kryo kryo;

    private Map<Integer, PlayerPosition> playerPositions = new HashMap<>();


    public GameServer() throws IOException {
        server = new Server();
        server.start();
        server.bind(54555, 54777);

        kryo = server.getKryo();
        kryo.register(PlayerPosition.class);

        server.addListener(new Listener(){

            @Override
            public void received(Connection connection, Object object) {

                if (object instanceof PlayerPosition) {
                    PlayerPosition position = (PlayerPosition) object;
                    playerPositions.put(position.id, position);

                    // Broadcast updated positions to all clients
                    for (PlayerPosition pos : playerPositions.values()) {
                        server.sendToAllExceptTCP(connection.getID(),pos);
                    }
                }
            }
            @Override
            public void disconnected(Connection connection) {
                playerPositions.remove(connection.getID());
            }
        });
    }

    public static void main(String[] args) throws IOException {
        new GameServer();
    }
}