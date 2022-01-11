package br.usp.each.typerace.server;

import org.java_websocket.server.WebSocketServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Scanner;


public class ServerMain {

    private WebSocketServer server;

    public ServerMain(WebSocketServer server) {
        this.server = server;
    }

    public void init() {
        System.out.println("Initializing server...");
        server.run();
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        Scanner input = new Scanner(System.in);
        
        System.out.println("Insert the port to initialize the server: (defaults to 8080)");
        String port = input.nextLine();

        String finalPort = "8080";

        if(!port.isEmpty()) {
            finalPort = port;
        }

        WebSocketServer server = new Server(Integer.parseInt(finalPort), new HashMap<>());

        ServerMain main = new ServerMain(server);

        main.init();

        while(true) {
            String command = input.nextLine();

            if(command.equals("exit")) {
                server.stop();
                break;
            }
        }

        input.close();
    }

}
