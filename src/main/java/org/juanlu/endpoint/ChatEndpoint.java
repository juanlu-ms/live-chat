package org.juanlu.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.juanlu.model.Message;

import java.io.IOException;
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
     * The maximum length of a message that can be sent or received.
     * This is used to validate incoming messages and prevent excessively long messages.
     */
    private static final int MAX_MESSAGE_LENGTH = 1000;

    /**
     * Sends an error message back to the client.
     * This method is used to inform the client about validation errors or processing issues.
     *
     * @param session   The WebSocket session to send the error message to.
     * @param errorMsg  The error message to send.
     */
    private void sendError(Session session, String errorMsg) {
        try {
            session.getBasicRemote().sendText(
                    "{\"error\":\"" + errorMsg.replace("\"", "\\\"") + "\"}"
            );
        } catch (IOException e) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, "Error while sending error message", e);
        }
    }

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

    /**
     * This method is called when a message is received from a WebSocket client.
     * It processes the incoming message, validates it, and broadcasts it to all connected clients.
     *
     * @param messageJson The JSON string representing the incoming message.
     * @param session     The WebSocket session from which the message was received.
     */
    @OnMessage
    public void onMessage(String messageJson, Session session) {
        try {
            // Validate the incoming message length
            if (!validLength(messageJson)) {
                sendError(session, "Mensaje demasiado largo o vacío");
            }

            // Deserialize the incoming message to a Message object
            Message message = deserialize(messageJson, session);
            if (message == null) return;

            // Validate the sender and content fields
            if (message.getSender() == null || message.getSender().isEmpty() ||
                    message.getContent() == null || message.getContent().isEmpty()) {
                sendError(session, "El remitente y el contenido no pueden estar vacíos");
                return;
            }

            // Set the timestamp to the current time
            message.setTimestamp(LocalDateTime.now().toString());

            // Serialize the Message object back to JSON
            String broadcastJson;
            try {
                broadcastJson = mapper.writeValueAsString(message);
            } catch (IOException e) {
                sendError(session, "Error al procesar el mensaje");
                return;
            }

            // Broadcast the message to all connected sessions
            synchronized (sessions) {
                for (Session s : sessions) {
                    if (s.isOpen()) {
                        try {
                            s.getBasicRemote().sendText(broadcastJson);
                        } catch (IOException e) {
                            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, "Error while sending message to session: " + s.getId(), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, "Error while processing message", e);
        }
    }

    private Message deserialize(String messageJson, Session session) {
        Message message;
        try {
            message = mapper.readValue(messageJson, Message.class);
        } catch (IOException e) {
            sendError(session, "Formato de mensaje inválido");
            return null;
        }
        return message;
    }

    /**
     * Validates the length of the incoming message.
     * The message must not be null and must not exceed the maximum allowed length.
     *
     * @param messageJson The JSON string representing the incoming message.
     * @return true if the message is valid, false otherwise.
     */
    private boolean validLength(String messageJson) {
        return !(messageJson == null || messageJson.length() > MAX_MESSAGE_LENGTH);
    }
}