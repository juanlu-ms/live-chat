package org.juanlu.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.juanlu.model.Message;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@ServerEndpoint("/chat")
public class ChatEndpoint {

    /**
     * A set to keep track of all active WebSocket sessions.
     * This allows us to broadcast messages to all connected clients.
     */
    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());

    /**
     * An ObjectMapper instance for JSON serialization and deserialization.
     * This is used to convert Message objects to JSON strings and vice versa.
     */
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * This method is called when a new WebSocket connection is opened.
     * It adds the new session to the set of active sessions.
     *
     * @param session The WebSocket session that was opened.
     */
    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        System.out.println("New session opened: " + session.getId());
    }

    /**
     * This method is called when a WebSocket connection is closed.
     * It removes the session from the set of active sessions.
     *
     * @param session The WebSocket session that was closed.
     */
    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        System.out.println("Session closed: " + session.getId());
    }

    @OnMessage
    public void onMessage(String messageJson, Session session) {
        try {
            // Deserialize the incoming message to a Message object
            Message message = mapper.readValue(messageJson, Message.class);

            // Set the timestamp to the current time
            message.setTimestamp(LocalDateTime.now().toString());

            // Serialize the Message object back to JSON
            String broadcastJson = mapper.writeValueAsString(message);

            // Broadcast the message to all connected sessions
            synchronized (sessions) {
                for (Session s : sessions) {
                    if (s.isOpen()) {
                        s.getBasicRemote().sendText(broadcastJson);
                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, "Error while processing message", e);
        }
    }
}