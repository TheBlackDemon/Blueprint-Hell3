package network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class NetworkMessage {
    public enum MessageType {
        CONNECT,           // Client connecting to server
        DISCONNECT,        // Client disconnecting
        GAME_STATE,        // Game state update
        USER_INPUT,        // User input from client
        LEADERBOARD,       // Leaderboard data
        GAME_RESULT,       // Game completion result
        HEARTBEAT,         // Keep-alive message
        ERROR,             // Error message
        CREATE_GAME,       // Create new multiplayer game
        JOIN_GAME,         // Join existing multiplayer game
        PLAYER_READY,      // Player ready status
        WRATH_EFFECT,      // Wrath effect application
        GAME_LIST,         // List of available games
        PLAYER_CONNECTION, // Player connection data
        PACKET_UPDATE      // Packet state update
    }
    
    private MessageType type;
    private String data;
    private String clientId;
    private long timestamp;
    private String sessionId;

    public NetworkMessage(MessageType type, String data, String clientId) {
        this.type = type;
        this.data = data;
        this.clientId = clientId;
        this.timestamp = System.currentTimeMillis();
    }
    

    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }
    
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String toJson() {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(this);
    }

    public static NetworkMessage fromJson(String json) {
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, NetworkMessage.class);
    }
    
    @Override
    public String toString() {
        return String.format("NetworkMessage{type=%s, clientId='%s', timestamp=%d, dataLength=%d}", 
                           type, clientId, timestamp, data != null ? data.length() : 0);
    }
}
