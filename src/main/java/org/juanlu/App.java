package org.juanlu;

import org.glassfish.tyrus.server.Server;
import org.juanlu.endpoint.ChatEndpoint;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App {

    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        Properties properties = loadProperties();
        if (properties == null)
            return;

        String host = properties.getProperty("server.host", "localhost");
        int port;
        try {
            port = Integer.parseInt(properties.getProperty("server.port", "8025"));
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING,
                    "Invalid port number in config.properties. Using default port 8025.", e);
            port = 8025;
        }
        String contextPath = "/";

        Server server = new Server(host, port, contextPath, null, ChatEndpoint.class);

        try {
            server.start();
            LOGGER.info("WebSocket server started on ws://" + host + ":" + port + "/chat");
            LOGGER.info("Press ENTER to stop the server...");
            new Scanner(System.in).nextLine();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error while starting server", e);
        } finally {
            server.stop();
        }
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = App.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                LOGGER.severe("Error: Unable to find config.properties. The application will exit.");
                return null;
            }
            properties.load(input);
            return properties;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading config.properties. The application will exit.", e);
            return null;
        }
    }
}
