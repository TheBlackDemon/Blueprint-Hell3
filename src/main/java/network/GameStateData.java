package network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.List;
import java.util.Map;


public class GameStateData {
    private int level;
    private boolean gameOver;
    private boolean successfully;
    private int packetLoss;
    private long levelStartTime;
    private String username;
    private int coins;
    private int maxLevelPass;
    
    private int numberPacketsSquare;
    private int numberPacketTriangle;
    private int numberPacketsCircle;
    private int numberPacketsConfidential4;
    private int numberPacketsConfidential6;
    private int numberPacketsBulky8;
    private int numberPacketsBulky10;
    
    private List<NodeData> nodes;
    
    private List<ConnectionData> connections;
    
    private List<PacketData> packets;
    
    private List<ShockwaveData> shockwaves;
    
    private Map<String, Boolean> powerUpStates;
    private Map<String, Long> powerUpTimers;
    
    public GameStateData() {}
    
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    
    public boolean isGameOver() { return gameOver; }
    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }
    
    public boolean isSuccessfully() { return successfully; }
    public void setSuccessfully(boolean successfully) { this.successfully = successfully; }
    
    public int getPacketLoss() { return packetLoss; }
    public void setPacketLoss(int packetLoss) { this.packetLoss = packetLoss; }
    
    public long getLevelStartTime() { return levelStartTime; }
    public void setLevelStartTime(long levelStartTime) { this.levelStartTime = levelStartTime; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public int getCoins() { return coins; }
    public void setCoins(int coins) { this.coins = coins; }
    
    public int getMaxLevelPass() { return maxLevelPass; }
    public void setMaxLevelPass(int maxLevelPass) { this.maxLevelPass = maxLevelPass; }
    
    public int getNumberPacketsSquare() { return numberPacketsSquare; }
    public void setNumberPacketsSquare(int numberPacketsSquare) { this.numberPacketsSquare = numberPacketsSquare; }
    
    public int getNumberPacketTriangle() { return numberPacketTriangle; }
    public void setNumberPacketTriangle(int numberPacketTriangle) { this.numberPacketTriangle = numberPacketTriangle; }
    
    public int getNumberPacketsCircle() { return numberPacketsCircle; }
    public void setNumberPacketsCircle(int numberPacketsCircle) { this.numberPacketsCircle = numberPacketsCircle; }
    
    public int getNumberPacketsConfidential4() { return numberPacketsConfidential4; }
    public void setNumberPacketsConfidential4(int numberPacketsConfidential4) { this.numberPacketsConfidential4 = numberPacketsConfidential4; }
    
    public int getNumberPacketsConfidential6() { return numberPacketsConfidential6; }
    public void setNumberPacketsConfidential6(int numberPacketsConfidential6) { this.numberPacketsConfidential6 = numberPacketsConfidential6; }
    
    public int getNumberPacketsBulky8() { return numberPacketsBulky8; }
    public void setNumberPacketsBulky8(int numberPacketsBulky8) { this.numberPacketsBulky8 = numberPacketsBulky8; }
    
    public int getNumberPacketsBulky10() { return numberPacketsBulky10; }
    public void setNumberPacketsBulky10(int numberPacketsBulky10) { this.numberPacketsBulky10 = numberPacketsBulky10; }
    
    public List<NodeData> getNodes() { return nodes; }
    public void setNodes(List<NodeData> nodes) { this.nodes = nodes; }
    
    public List<ConnectionData> getConnections() { return connections; }
    public void setConnections(List<ConnectionData> connections) { this.connections = connections; }
    
    public List<PacketData> getPackets() { return packets; }
    public void setPackets(List<PacketData> packets) { this.packets = packets; }
    
    public List<ShockwaveData> getShockwaves() { return shockwaves; }
    public void setShockwaves(List<ShockwaveData> shockwaves) { this.shockwaves = shockwaves; }
    
    public Map<String, Boolean> getPowerUpStates() { return powerUpStates; }
    public void setPowerUpStates(Map<String, Boolean> powerUpStates) { this.powerUpStates = powerUpStates; }
    
    public Map<String, Long> getPowerUpTimers() { return powerUpTimers; }
    public void setPowerUpTimers(Map<String, Long> powerUpTimers) { this.powerUpTimers = powerUpTimers; }

    public String toJson() {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(this);
    }

    public static GameStateData fromJson(String json) {
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, GameStateData.class);
    }
    
    public static class NodeData {
        private String id;
        private double x, y;
        private String type;
        private Map<String, Object> properties;
        
        public NodeData() {}
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public double getX() { return x; }
        public void setX(double x) { this.x = x; }
        
        public double getY() { return y; }
        public void setY(double y) { this.y = y; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public Map<String, Object> getProperties() { return properties; }
        public void setProperties(Map<String, Object> properties) { this.properties = properties; }
    }
    
    public static class ConnectionData {
        private String id;
        private String fromNodeId;
        private String toNodeId;
        private List<BendPointData> bendPoints;
        private Map<String, Object> properties;
        
        public ConnectionData() {}
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getFromNodeId() { return fromNodeId; }
        public void setFromNodeId(String fromNodeId) { this.fromNodeId = fromNodeId; }
        
        public String getToNodeId() { return toNodeId; }
        public void setToNodeId(String toNodeId) { this.toNodeId = toNodeId; }
        
        public List<BendPointData> getBendPoints() { return bendPoints; }
        public void setBendPoints(List<BendPointData> bendPoints) { this.bendPoints = bendPoints; }
        
        public Map<String, Object> getProperties() { return properties; }
        public void setProperties(Map<String, Object> properties) { this.properties = properties; }
    }
    
    public static class BendPointData {
        private double x, y;
        
        public BendPointData() {}
        
        public BendPointData(double x, double y) {
            this.x = x;
            this.y = y;
        }
        
        public double getX() { return x; }
        public void setX(double x) { this.x = x; }
        
        public double getY() { return y; }
        public void setY(double y) { this.y = y; }
    }
    
    public static class PacketData {
        private String id;
        private double x, y;
        private String type;
        private double progress;
        private String connectionId;
        private Map<String, Object> properties;
        
        public PacketData() {}
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public double getX() { return x; }
        public void setX(double x) { this.x = x; }
        
        public double getY() { return y; }
        public void setY(double y) { this.y = y; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public double getProgress() { return progress; }
        public void setProgress(double progress) { this.progress = progress; }
        
        public String getConnectionId() { return connectionId; }
        public void setConnectionId(String connectionId) { this.connectionId = connectionId; }
        
        public Map<String, Object> getProperties() { return properties; }
        public void setProperties(Map<String, Object> properties) { this.properties = properties; }
    }
    
    public static class ShockwaveData {
        private String id;
        private double x, y;
        private double radius;
        private long timestamp;
        private Map<String, Object> properties;
        
        public ShockwaveData() {}
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public double getX() { return x; }
        public void setX(double x) { this.x = x; }
        
        public double getY() { return y; }
        public void setY(double y) { this.y = y; }
        
        public double getRadius() { return radius; }
        public void setRadius(double radius) { this.radius = radius; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        
        public Map<String, Object> getProperties() { return properties; }
        public void setProperties(Map<String, Object> properties) { this.properties = properties; }
    }
}
