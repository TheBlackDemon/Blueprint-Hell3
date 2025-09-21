package Game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


public class LevelConfig {
    
    public static class LevelData {
        private final int levelNumber;
        private final String levelName;
        private final String description;
        private final int targetPackets;
        private final List<NodeConfig> nodes;
        private final List<String> availablePacketTypes;
        private final List<String> availableSystems;
        private final Map<String, Object> levelProperties;
        private final int timeLimit; // in seconds, -1 for no limit
        private final int maxPacketLoss;
        
        public LevelData(int levelNumber, String levelName, String description, 
                        int targetPackets, List<NodeConfig> nodes, 
                        List<String> availablePacketTypes, List<String> availableSystems,
                        Map<String, Object> levelProperties, int timeLimit, int maxPacketLoss) {
            this.levelNumber = levelNumber;
            this.levelName = levelName;
            this.description = description;
            this.targetPackets = targetPackets;
            this.nodes = nodes;
            this.availablePacketTypes = availablePacketTypes;
            this.availableSystems = availableSystems;
            this.levelProperties = levelProperties;
            this.timeLimit = timeLimit;
            this.maxPacketLoss = maxPacketLoss;
        }
        
        public int getLevelNumber() { return levelNumber; }
        public String getLevelName() { return levelName; }
        public String getDescription() { return description; }
        public int getTargetPackets() { return targetPackets; }
        public List<NodeConfig> getNodes() { return nodes; }
        public List<String> getAvailablePacketTypes() { return availablePacketTypes; }
        public List<String> getAvailableSystems() { return availableSystems; }
        public Map<String, Object> getLevelProperties() { return levelProperties; }
        public int getTimeLimit() { return timeLimit; }
        public int getMaxPacketLoss() { return maxPacketLoss; }
    }
    
    public static class NodeConfig {
        private final String id;
        private final int x, y;
        private final String[] outputShapes;
        private final String[] inputShapes;
        private final String systemType;
        private final Map<String, Object> nodeProperties;
        
        public NodeConfig(String id, int x, int y, String[] outputShapes, 
                         String[] inputShapes, String systemType, Map<String, Object> nodeProperties) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.outputShapes = outputShapes;
            this.inputShapes = inputShapes;
            this.systemType = systemType;
            this.nodeProperties = nodeProperties;
        }
        
        public String getId() { return id; }
        public int getX() { return x; }
        public int getY() { return y; }
        public String[] getOutputShapes() { return outputShapes; }
        public String[] getInputShapes() { return inputShapes; }
        public String getSystemType() { return systemType; }
        public Map<String, Object> getNodeProperties() { return nodeProperties; }
    }
    
    private static final Map<Integer, LevelData> LEVELS = new HashMap<>();
    
    static {
        initializeLevels();
    }
    
    private static void initializeLevels() {
        List<NodeConfig> level1Nodes = new ArrayList<>();
        level1Nodes.add(new NodeConfig("A", 100, 100, new String[]{"square", "triangle"},
                                     new String[]{"square", "triangle"}, "basic", new HashMap<>()));
        level1Nodes.add(new NodeConfig("B", 500, 100, new String[]{"square", "triangle"},
                                     new String[]{"square", "triangle"}, "basic", new HashMap<>()));
        level1Nodes.add(new NodeConfig("C", 100, 500, new String[]{"square", "triangle"},
                                     new String[]{"square", "triangle"}, "basic", new HashMap<>()));
        level1Nodes.add(new NodeConfig("D", 500, 500, new String[]{"square", "triangle"},
                                     new String[]{"square", "triangle"}, "basic", new HashMap<>()));

        List<String> level1PacketTypes = new ArrayList<>();
        level1PacketTypes.add("square");
        level1PacketTypes.add("triangle");

        List<String> level1Systems = new ArrayList<>();
        level1Systems.add("basic");

        Map<String, Object> level1Properties = new HashMap<>();
        level1Properties.put("wireLengthLimit", 300.0);
        level1Properties.put("packetSpeed", 0.02);
        level1Properties.put("collisionDistance", 2.0);

        LEVELS.put(1, new LevelData(1, "Network Foundation",
            "Learn the basics of network routing with simple square and triangle packets",
            20, level1Nodes, level1PacketTypes, level1Systems, level1Properties, -1, 10));
        
        List<NodeConfig> level2Nodes = new ArrayList<>();
        level2Nodes.add(new NodeConfig("A", 100, 100, new String[]{"square", "triangle", "circle"}, 
                                     new String[]{"square", "triangle", "circle"}, "basic", new HashMap<>()));
        level2Nodes.add(new NodeConfig("B", 300, 100, new String[]{"square", "triangle", "circle"}, 
                                     new String[]{"square", "triangle", "circle"}, "basic", new HashMap<>()));
        level2Nodes.add(new NodeConfig("C", 500, 100, new String[]{"square", "triangle", "circle"}, 
                                     new String[]{"square", "triangle", "circle"}, "basic", new HashMap<>()));
        level2Nodes.add(new NodeConfig("D", 100, 300, new String[]{"square", "triangle", "circle"}, 
                                     new String[]{"square", "triangle", "circle"}, "basic", new HashMap<>()));
        level2Nodes.add(new NodeConfig("E", 300, 300, new String[]{"square", "triangle", "circle"}, 
                                     new String[]{"square", "triangle", "circle"}, "basic", new HashMap<>()));
        level2Nodes.add(new NodeConfig("F", 500, 300, new String[]{"square", "triangle", "circle"}, 
                                     new String[]{"square", "triangle", "circle"}, "basic", new HashMap<>()));
        
        List<String> level2PacketTypes = new ArrayList<>();
        level2PacketTypes.add("square");
        level2PacketTypes.add("triangle");
        level2PacketTypes.add("circle");
        
        Map<String, Object> level2Properties = new HashMap<>();
        level2Properties.put("wireLengthLimit", 250.0);
        level2Properties.put("packetSpeed", 0.025);
        level2Properties.put("collisionDistance", 1.5);
        level2Properties.put("circleAcceleration", 1.2);
        
        LEVELS.put(2, new LevelData(2, "Packet Diversity", 
            "Master different packet types with varying speeds and behaviors",
            30, level2Nodes, level2PacketTypes, level1Systems, level2Properties, -1, 15));
        
        List<NodeConfig> level3Nodes = new ArrayList<>();
        level3Nodes.add(new NodeConfig("A", 100, 100, new String[]{"square", "triangle", "confidential_4"},
                                     new String[]{"square", "triangle", "confidential_4"}, "basic", new HashMap<>()));
        level3Nodes.add(new NodeConfig("B", 300, 100, new String[]{"square", "triangle", "confidential_4"},
                                     new String[]{"square", "triangle", "confidential_4"}, "vpn", new HashMap<>()));
        level3Nodes.add(new NodeConfig("C", 500, 100, new String[]{"square", "triangle", "confidential_6"},
                                     new String[]{"square", "triangle", "confidential_6"}, "basic", new HashMap<>()));
        level3Nodes.add(new NodeConfig("D", 100, 300, new String[]{"square", "triangle", "confidential_4"},
                                     new String[]{"square", "triangle", "confidential_4"}, "antitrojan", new HashMap<>()));
        level3Nodes.add(new NodeConfig("E", 300, 300, new String[]{"square", "triangle", "confidential_6"},
                                     new String[]{"square", "triangle", "confidential_6"}, "basic", new HashMap<>()));
        level3Nodes.add(new NodeConfig("F", 500, 300, new String[]{"square", "triangle", "confidential_4"},
                                     new String[]{"square", "triangle", "confidential_4"}, "basic", new HashMap<>()));

        List<String> level3PacketTypes = new ArrayList<>();
        level3PacketTypes.add("square");
        level3PacketTypes.add("triangle");
        level3PacketTypes.add("confidential_4");
        level3PacketTypes.add("confidential_6");

        List<String> level3Systems = new ArrayList<>();
        level3Systems.add("basic");
        level3Systems.add("vpn");
        level3Systems.add("antitrojan");

        Map<String, Object> level3Properties = new HashMap<>();
        level3Properties.put("wireLengthLimit", 200.0);
        level3Properties.put("packetSpeed", 0.03);
        level3Properties.put("collisionDistance", 1.0);
        level3Properties.put("confidentialSpeedReduction", 0.3);
        level3Properties.put("systemDamageThreshold", 0.1);

        LEVELS.put(3, new LevelData(3, "Security Systems",
            "Introduce VPN and Anti-trojan systems with confidential packets",
            40, level3Nodes, level3PacketTypes, level3Systems, level3Properties, -1, 20));
        
        List<NodeConfig> level4Nodes = new ArrayList<>();
        level4Nodes.add(new NodeConfig("A", 100, 100, new String[]{"square", "triangle", "bulky_8"}, 
                                     new String[]{"square", "triangle", "bulky_8"}, "basic", new HashMap<>()));
        level4Nodes.add(new NodeConfig("B", 250, 100, new String[]{"square", "triangle", "bulky_8"}, 
                                     new String[]{"square", "triangle", "bulky_8"}, "distribute", new HashMap<>()));
        level4Nodes.add(new NodeConfig("C", 400, 100, new String[]{"square", "triangle", "bulky_10"}, 
                                     new String[]{"square", "triangle", "bulky_10"}, "merge", new HashMap<>()));
        level4Nodes.add(new NodeConfig("D", 550, 100, new String[]{"square", "triangle", "bulky_8"}, 
                                     new String[]{"square", "triangle", "bulky_8"}, "basic", new HashMap<>()));
        level4Nodes.add(new NodeConfig("E", 100, 300, new String[]{"square", "triangle", "bulky_10"}, 
                                     new String[]{"square", "triangle", "bulky_10"}, "basic", new HashMap<>()));
        level4Nodes.add(new NodeConfig("F", 250, 300, new String[]{"square", "triangle", "bulky_8"}, 
                                     new String[]{"square", "triangle", "bulky_8"}, "basic", new HashMap<>()));
        level4Nodes.add(new NodeConfig("G", 400, 300, new String[]{"square", "triangle", "bulky_10"}, 
                                     new String[]{"square", "triangle", "bulky_10"}, "basic", new HashMap<>()));
        level4Nodes.add(new NodeConfig("H", 550, 300, new String[]{"square", "triangle", "bulky_8"}, 
                                     new String[]{"square", "triangle", "bulky_8"}, "basic", new HashMap<>()));
        
        List<String> level4PacketTypes = new ArrayList<>();
        level4PacketTypes.add("square");
        level4PacketTypes.add("triangle");
        level4PacketTypes.add("bulky_8");
        level4PacketTypes.add("bulky_10");
        
        List<String> level4Systems = new ArrayList<>();
        level4Systems.add("basic");
        level4Systems.add("distribute");
        level4Systems.add("merge");
        
        Map<String, Object> level4Properties = new HashMap<>();
        level4Properties.put("wireLengthLimit", 180.0);
        level4Properties.put("packetSpeed", 0.035);
        level4Properties.put("collisionDistance", 0.8);
        level4Properties.put("bulkyWireLimit", 3);
        level4Properties.put("maxBulkyPasses", 3);
        
        LEVELS.put(4, new LevelData(4, "Heavy Traffic", 
            "Handle bulky packets and complex distribution/merge systems",
            50, level4Nodes, level4PacketTypes, level4Systems, level4Properties, -1, 25));
        
        List<NodeConfig> level5Nodes = new ArrayList<>();
        level5Nodes.add(new NodeConfig("A", 100, 100, new String[]{"square", "triangle", "circle", "confidential_4", "bulky_8"}, 
                                     new String[]{"square", "triangle", "circle", "confidential_4", "bulky_8"}, "basic", new HashMap<>()));
        level5Nodes.add(new NodeConfig("B", 200, 100, new String[]{"square", "triangle", "circle", "confidential_6", "bulky_10"}, 
                                     new String[]{"square", "triangle", "circle", "confidential_6", "bulky_10"}, "vpn", new HashMap<>()));
        level5Nodes.add(new NodeConfig("C", 300, 100, new String[]{"square", "triangle", "circle", "confidential_4", "bulky_8"}, 
                                     new String[]{"square", "triangle", "circle", "confidential_4", "bulky_8"}, "distribute", new HashMap<>()));
        level5Nodes.add(new NodeConfig("D", 400, 100, new String[]{"square", "triangle", "circle", "confidential_6", "bulky_10"}, 
                                     new String[]{"square", "triangle", "circle", "confidential_6", "bulky_10"}, "merge", new HashMap<>()));
        level5Nodes.add(new NodeConfig("E", 500, 100, new String[]{"square", "triangle", "circle", "confidential_4", "bulky_8"}, 
                                     new String[]{"square", "triangle", "circle", "confidential_4", "bulky_8"}, "antitrojan", new HashMap<>()));
        level5Nodes.add(new NodeConfig("F", 100, 300, new String[]{"square", "triangle", "circle", "confidential_6", "bulky_10"}, 
                                     new String[]{"square", "triangle", "circle", "confidential_6", "bulky_10"}, "basic", new HashMap<>()));
        level5Nodes.add(new NodeConfig("G", 200, 300, new String[]{"square", "triangle", "circle", "confidential_4", "bulky_8"}, 
                                     new String[]{"square", "triangle", "circle", "confidential_4", "bulky_8"}, "spy", new HashMap<>()));
        level5Nodes.add(new NodeConfig("H", 300, 300, new String[]{"square", "triangle", "circle", "confidential_6", "bulky_10"}, 
                                     new String[]{"square", "triangle", "circle", "confidential_6", "bulky_10"}, "malicious", new HashMap<>()));
        level5Nodes.add(new NodeConfig("I", 400, 300, new String[]{"square", "triangle", "circle", "confidential_4", "bulky_8"}, 
                                     new String[]{"square", "triangle", "circle", "confidential_4", "bulky_8"}, "basic", new HashMap<>()));
        level5Nodes.add(new NodeConfig("J", 500, 300, new String[]{"square", "triangle", "circle", "confidential_6", "bulky_10"}, 
                                     new String[]{"square", "triangle", "circle", "confidential_6", "bulky_10"}, "basic", new HashMap<>()));
        
        List<String> level5PacketTypes = new ArrayList<>();
        level5PacketTypes.add("square");
        level5PacketTypes.add("triangle");
        level5PacketTypes.add("circle");
        level5PacketTypes.add("confidential_4");
        level5PacketTypes.add("confidential_6");
        level5PacketTypes.add("bulky_8");
        level5PacketTypes.add("bulky_10");
        
        List<String> level5Systems = new ArrayList<>();
        level5Systems.add("basic");
        level5Systems.add("vpn");
        level5Systems.add("antitrojan");
        level5Systems.add("distribute");
        level5Systems.add("merge");
        level5Systems.add("spy");
        level5Systems.add("malicious");
        
        Map<String, Object> level5Properties = new HashMap<>();
        level5Properties.put("wireLengthLimit", 150.0);
        level5Properties.put("packetSpeed", 0.04);
        level5Properties.put("collisionDistance", 0.5);
        level5Properties.put("systemDamageThreshold", 0.08);
        level5Properties.put("maxBulkyPasses", 2);
        level5Properties.put("confidentialSpeedReduction", 0.2);
        
        LEVELS.put(5, new LevelData(5, "Master Challenge", 
            "Master all game mechanics with maximum complexity and difficulty",
            60, level5Nodes, level5PacketTypes, level5Systems, level5Properties, 300, 30));
    }
    
    public static LevelData getLevel(int levelNumber) {
        return LEVELS.get(levelNumber);
    }
    
    public static boolean isValidLevel(int levelNumber) {
        return LEVELS.containsKey(levelNumber);
    }
    
    public static int getMaxLevel() {
        return LEVELS.size();
    }
    
}
