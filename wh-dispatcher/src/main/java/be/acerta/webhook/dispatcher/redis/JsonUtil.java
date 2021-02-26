package be.acerta.webhook.dispatcher.redis;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static java.lang.String.format;
import java.io.IOException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonUtil {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JsonUtil.class);
    public static final String JSON_SKEDIFY_OFFSET_DATE_TIME_FORMAT = "[yyyy-MM-dd\'T\'HH:mm:ssXXX]";

    private JsonUtil() {
    }


    public static class JsonUtilException extends RuntimeException {
        private static final long serialVersionUID = 1232204968531145885L;

        public JsonUtilException() {
        }

        public JsonUtilException(String message) {
            super(message);
        }

        public JsonUtilException(Throwable cause) {
            super(cause);
        }

        public JsonUtilException(String message, Throwable cause) {
            super(message, cause);
        }

        public JsonUtilException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }

    public static String objectToJson(Object object) {
        try {
            return getObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            String message = format("Object kon niet naar JSON omgezet worden: %s", object);
            log.error(message, e);
            throw new JsonUtilException(message, e);
        }
    }

    public static <T> T jsonToObject(String jsonString, Class<T> clazz) {
        try {
            return getObjectMapper().readValue(jsonString, clazz);
        } catch (IOException e) {
            String message = format("Json kon niet naar object van klasse %s omgezet worden: %s", clazz, jsonString);
            log.error(message, e);
            throw new JsonUtilException(message, e);
        }
    }

    public static ObjectMapper getObjectMapper() {
        // Hibernate5Module hibernate5Module = new Hibernate5Module();
        // hibernate5Module.enable(Hibernate5Module.Feature.USE_TRANSIENT_ANNOTATION);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        // objectMapper.registerModule(hibernate5Module);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS, SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
        objectMapper.setVisibility(objectMapper.getSerializationConfig().getDefaultVisibilityChecker().withFieldVisibility(ANY).withGetterVisibility(NONE).withIsGetterVisibility(NONE).withSetterVisibility(NONE).withCreatorVisibility(NONE));
        return objectMapper;
    }

    public static String getFieldAsString(String json, String field) {
        try {
            return getObjectMapper().readTree(json).path(field).asText();
        } catch (IOException e) {
            throw new JsonUtilException(e);
        }
    }
}
