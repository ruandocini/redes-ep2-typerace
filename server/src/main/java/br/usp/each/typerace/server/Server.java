package br.usp.each.typerace.server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Server extends WebSocketServer {

    private final Map<String, WebSocket> connections;

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
