package systems.mythical.mccloudadmin.websocket.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DatabaseMessage extends BaseMessage {
    private String operation;
    private String tableName;
    private String settingName;
    private String settingValue;
    private Object data;
    private boolean success;
    private String message;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public DatabaseMessage() {
        super("database", null);
    }

    public DatabaseMessage(String operation, String tableName, Object data) {
        super("database", null);
        this.operation = operation;
        this.tableName = tableName;
        this.data = data;
        this.success = true;
    }

    public String toJson() {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing DatabaseMessage to JSON", e);
        }
    }
} 