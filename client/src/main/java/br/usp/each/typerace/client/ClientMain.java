package br.usp.each.typerace.client;

import org.java_websocket.client.WebSocketClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class ClientMain {

    private WebSocketClient client;
    

    public ClientMain(WebSocketClient client) {
        this.client = client;
    }

    public void init(String username) {
        System.out.println("Starting client " + username);
        client.connect();
    }

    public static void main(String[] args) throws URISyntaxException, InterruptedException {
        
        Scanner input = new Scanner(System.in);

        WebSocketClient client;

        while(true) {

            System.out.println("\nType the PORT of the server (defaults to 8080): ");
            String customServer = input.nextLine();

            String finalServer = "ws://localhost:";

            if (!customServer.isEmpty()) {
                finalServer += customServer;
            }       

            System.out.println("Insert an username");

            String username = input.nextLine();

            if (username.isEmpty()) {
                System.out.println("Empty name, plis insert a valid name: \nExample: JohnDoe\nBiggusDigus");
                continue;
            }

            username = username.replaceAll("\\s", ""); 

            finalServer += "/username=" + username;

            client = new Client(new URI(finalServer));
                        
            client.connect();

            TimeUnit.SECONDS.sleep(1);

            if(client.isOpen()) {
                break;
            }
            else{
                finalServer = "ws://localhost:";
            }
        }

        while(client.isOpen()) {
            String in = input.nextLine();

            if (in.equals("exit")) {
                client.close();
                break;
            } else{
                client.send(in);
            }

        }

        System.exit(0);

    }

    
}