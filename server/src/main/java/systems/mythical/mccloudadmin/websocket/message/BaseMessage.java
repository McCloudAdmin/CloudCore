package systems.mythical.mccloudadmin.websocket.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseMessage {
    private String type;
    private String content;

    public BaseMessage() {
    }

    public BaseMessage(String type, String content) {
        this.type = type;
        this.content = content;
    }
} 