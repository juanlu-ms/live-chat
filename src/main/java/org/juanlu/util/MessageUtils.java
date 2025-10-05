package org.juanlu.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.juanlu.model.Message;

public class MessageUtils {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static Message deserialize(String json) throws Exception {
        return mapper.readValue(json, Message.class);
    }

    public static String serialize(Message message) throws Exception {
        return mapper.writeValueAsString(message);
    }
}
