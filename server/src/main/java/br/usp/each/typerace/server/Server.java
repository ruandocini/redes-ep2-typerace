package br.usp.each.typerace.server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;


public class Server extends WebSocketServer {

    private final Map<String, WebSocket> connections;
    List<String> readyPlayers = new ArrayList<String>();
    Game currentGame;
    private Boolean isGameStarted = false;
    long startTime = 0;

    public Server(int port, Map<String, WebSocket> connections) {
        super(new InetSocketAddress(port));
        this.connections = connections;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {

        if(idValidator(conn)){
            String username = playerId(conn);
            connections.put(username, conn);
            conn.send("Welcome " + username);
            broadcast("New player " + username + " connected," +
                    " there are " + connections.size() + " players online");
        }

    }

    private String playerId(WebSocket conn) {
        String connInfos = conn.getResourceDescriptor();
        return connInfos.substring(connInfos.indexOf("username=") + 9);
    }

    private boolean idValidator(WebSocket conn) {
        if(connections.containsKey(playerId(conn))) {
            conn.send("Username already taken\n");
            conn.close(1000, "invalidName");
            return false;
        }

        return true;
    }    

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        String username = playerId(conn);
        connections.remove(username);
        broadcast("Player " + username + " left, " + connections.size() + " players left");
        System.out.println("Player " + username + " left, " + connections.size() + " players left");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Message received from [" + playerId(conn) + "] :" + message);

        if(isGameStarted){
            currentGame.verifyAnswer(playerId(conn), message);
            String winner = currentGame.verifyWinner();
            if(winner != null){
                broadcast(winner);
                broadcast(currentGame.leaderBoard());
                long elapsedTime = System.currentTimeMillis() - startTime;
                broadcast("Time elapsed: " + elapsedTime/1000 + " seconds");
                isGameStarted = false;
                readyPlayers.clear();
            }
            conn.send(currentGame.sendWord(playerId(conn)));
        }

        if (message.equals("start")) {
            if (readyPlayers.contains(playerId(conn))) {
                conn.send("You are already prepared\n");
            } else {
                readyPlayers.add(playerId(conn));
                for (WebSocket c : connections.values()) {
                    if (readyPlayers.contains(playerId(c))) {
                        broadcast("Player " + playerId(c) + ": READY");
                    }
                    else{
                        broadcast("Player " + playerId(c) + ": NOT READY");
                    }
                }
                if (readyPlayers.size() == connections.size()) {
                    broadcast("All players are ready, starting game");
                    broadcast("First word below:");
                    isGameStarted = true;
                    currentGame = new Game(readyPlayers);
                    broadcast(currentGame.sendWord(playerId(conn)));
                    startTime = System.currentTimeMillis();
                }
            }
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("Server started on port: " + getPort());
    }
}
