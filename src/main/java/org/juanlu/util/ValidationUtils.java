package org.juanlu.util;

import org.juanlu.model.Message;

public final class ValidationUtils {

    public static final int MAX_MESSAGE_LENGTH = 1000;

    private ValidationUtils() { }

    /**
     * Validates the length of the incoming message.
     * The message must not be null and must not exceed the maximum allowed length.
     *
     * @param messageJson The JSON string representing the incoming message.
     * @return true if the message is valid, false otherwise.
     */
    public static boolean validLength(String messageJson) {
        return messageJson != null && messageJson.length() <= MAX_MESSAGE_LENGTH;
    }

    /**
     * Validates the fields of the Message object.
     * The sender and content fields must not be null or empty.
     *
     * @param message The Message object to validate.
     * @return true if the message fields are valid, false otherwise.
     */
    public static boolean validMessageFields(Message message) {
        return message != null &&
                message.getSender() != null && !message.getSender().isEmpty() &&
                message.getContent() != null && !message.getContent().isEmpty();
    }
}
