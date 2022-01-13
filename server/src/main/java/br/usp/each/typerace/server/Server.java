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

    //Method that talks to all clients when a connection happens 
    //Keep everyone in the game at the same page about whos connected
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {

        if(idValidator(conn)){
            String username = playerId(conn);
            connections.put(username, conn);
            conn.send("Welcome " + username);
            conn.send(menuGame());
            broadcast("New player " + username + " connected," +
                    " there are " + connections.size() + " players online");
            
        }


    }

    //method used to get the player id from the connection
    private String playerId(WebSocket conn) {
        String connInfos = conn.getResourceDescriptor();
        return connInfos.substring(connInfos.indexOf("username=") + 9);
    }

    //method used to validate the player id 
    //looking if theres another players with the same id
    private boolean idValidator(WebSocket conn) {
        if(connections.containsKey(playerId(conn))) {
            conn.send("Username already taken\n");
            conn.close(1000, "invalidName");
            return false;
        }

        return true;
    }    

    //when the conn is closed, remove its name from the poll and broadcast that the player left for everyone
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        String username = playerId(conn);
        connections.remove(username);
        broadcast("Player " + username + " left, " + connections.size() + " players left");
        System.out.println("Player " + username + " left, " + connections.size() + " players left");
    }

    //Just a simple menu used to show the possibliities to the player on connection
    public String menuGame(){
        return "MENU\n"+
                "\n- start: to start the game, when all players are ready\n"+
                "\n- exit: quit game in any point of the game\n" +
                "\n\n- Have fun fella\n";
    } 

    //most important method of the server, controls the flow of the game
    @Override
    public void onMessage(WebSocket conn, String message) {
        //every message is logged on the server side to monitor the interactions
        System.out.println("Message received from [" + playerId(conn) + "] :" + message);

        //verify if the game is active on each message sent to the server
        //important to define if the message is a command or a word in the game 
        //this part does the game exectution flow, calling the fuction to send the word to the player and verify answer
        //also used to track how much time the game took
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
            } else{
                conn.send(currentGame.sendWord(playerId(conn)));
            }
        }

        //counts the players that are ready for the game
        //I implemented this to avoid the game to start before all players are ready
        //if the player is ready, add it to the list of ready players
        //just start the game when all players are ready, I thought it was a better idea the just one player starting
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

                //note that the first word is only broadcasted because it happens at the same time for everyone
                //after that the interaction is individual between each player and the server
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
