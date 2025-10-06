package org.juanlu.endpoint;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.juanlu.model.Message;
import org.juanlu.util.MessageUtils;
import org.juanlu.util.ValidationUtils;

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
     * Sends an error message back to the client.
     * This method is used to inform the client about validation errors or processing issues.
     *
     * @param session  The WebSocket session to send the error message to.
     * @param errorMsg The error message to send.
     */
    private void sendError(Session session, String errorMsg) {
        try {
            session.getBasicRemote().sendText(
                    "{\"error\":\"" + errorMsg.replace("\"", "\\\"") + "\"}"
            );
        } catch (IOException e) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE,
                    "Error while sending error message", e);
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
        Logger.getLogger(ChatEndpoint.class.getName()).log(Level.INFO,
                "New session opened: " + session.getId());
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
        Logger.getLogger(ChatEndpoint.class.getName()).log(Level.INFO,
                "Session closed: " + session.getId());
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
            if (!ValidationUtils.validLength(messageJson)) {
                sendError(session, "Mensaje demasiado largo o vacío");
                return;
            }

            // Deserialize the incoming message to a Message object
            Message message = MessageUtils.deserialize(messageJson);

            // Validate the sender and content fields
            if (!ValidationUtils.validMessageFields(message)) {
                sendError(session, "El remitente y el contenido no pueden estar vacíos");
                return;
            }

            // Set the timestamp to the current time
            message.setTimestamp(LocalDateTime.now().toString());

            // Serialize the Message object back to JSON
            String broadcastJson = MessageUtils.serialize(message);

            // Broadcast the message to all connected sessions
            broadcastMessage(broadcastJson);

        } catch (Exception e) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE,
                    "Error while processing message", e);
            sendError(session, "Error al procesar el mensaje");
        }
    }

    /**
     * Broadcasts a JSON message to all connected WebSocket sessions.
     *
     * @param broadcastJson The JSON string to broadcast.
     */
    private static void broadcastMessage(String broadcastJson) {
        synchronized (sessions) {
            for (Session s : sessions) {
                if (s.isOpen()) {
                    try {
                        s.getBasicRemote().sendText(broadcastJson);
                    } catch (IOException e) {
                        Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE,
                                "Error while sending message to session: " + s.getId(), e);
                    }
                }
            }
        }
    }
}