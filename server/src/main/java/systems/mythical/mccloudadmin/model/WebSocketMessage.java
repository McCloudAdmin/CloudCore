package systems.mythical.mccloudadmin.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class WebSocketMessage {
    private final String type;
    private final String content;

    @JsonCreator
    public WebSocketMessage(@JsonProperty("type") String type, @JsonProperty("content") String content) {
        this.type = type;
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "WebSocketMessage{" +
                "type='" + type + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
} 