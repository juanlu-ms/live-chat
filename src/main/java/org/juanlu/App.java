package org.juanlu;

import org.glassfish.tyrus.server.Server;
import org.juanlu.endpoint.ChatEndpoint;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App {

    public static void main(String[] args) {

        String host = "localhost";
        int port = 8025;
        String contextPath = "/";

        Server server = new Server(host, port, contextPath, null, ChatEndpoint.class);

        try {
            server.start();
            System.out.println("WebSocket server started on ws://" + host + ":" + port + "/chat");
            System.out.println("Press ENTER to stop the server...");
            new Scanner(System.in).nextLine();
        } catch (Exception e) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, "Error while starting server", e);
        } finally {
            server.stop();
        }
    }
}
