package Game;

import java.awt.Color;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PacketWaveManager {
    private final Map<String, WaveConfiguration> waveConfigurations;
    private final Map<String, Long> lastWaveTimes;
    private final Random random;
    private final ControllableSystemManager controllableSystemManager;
    private final PacketTypeRegistry packetTypeRegistry;
    
    public PacketWaveManager(ControllableSystemManager controllableSystemManager) {
        this.waveConfigurations = new ConcurrentHashMap<>();
        this.lastWaveTimes = new ConcurrentHashMap<>();
        this.random = new Random();
        this.controllableSystemManager = controllableSystemManager;
        this.packetTypeRegistry = new PacketTypeRegistry();
        
        initializeWaveConfigurations();
    }

    private void initializeWaveConfigurations() {
        waveConfigurations.put("square", new WaveConfiguration("square", 0, 2000, 5000, 1, 3));
        waveConfigurations.put("triangle", new WaveConfiguration("triangle", 1000, 3000, 4000, 1, 2));
        waveConfigurations.put("circle", new WaveConfiguration("circle", 500, 1500, 3000, 1, 4));
        waveConfigurations.put("protected", new WaveConfiguration("protected", 2000, 5000, 8000, 1, 1));
        
        waveConfigurations.put("confidential_4", new WaveConfiguration("confidential_4", 0, 2500, 6000, 1, 2));
        waveConfigurations.put("confidential_6", new WaveConfiguration("confidential_6", 1500, 4000, 7000, 1, 1));
        waveConfigurations.put("bulky_8", new WaveConfiguration("bulky_8", 3000, 6000, 10000, 1, 1));
        waveConfigurations.put("bulky_10", new WaveConfiguration("bulky_10", 5000, 8000, 12000, 1, 1));
    }

    public void updateWaves() {
        for (Map.Entry<String, List<INode>> entry : controllableSystemManager.getUncontrollableSystems().entrySet()) {
            String playerId = entry.getKey();
            List<INode> systems = entry.getValue();
            GameState gameState = controllableSystemManager.getPlayerGameState(playerId);
            
            if (gameState == null) continue;
            
            for (INode system : systems) {
                if (system.canOperate()) {
                    generateWaveForSystem(system, gameState, playerId);
                }
            }
        }
    }

    private void generateWaveForSystem(INode system, GameState gameState, String playerId) {
        String systemId = system.getId();
        long currentTime = System.currentTimeMillis();
        
        Long lastWaveTime = lastWaveTimes.get(systemId);
        if (lastWaveTime != null && (currentTime - lastWaveTime) < getMinWaveInterval()) {
            return;
        }
        
        String packetType = determinePacketType(playerId, system);
        if (packetType == null) return;
        
        WaveConfiguration config = waveConfigurations.get(packetType);
        if (config == null) return;
        
        if (lastWaveTime != null && (currentTime - lastWaveTime) < config.getMinSpawnTime()) {
            return;
        }
        
        int packetCount = random.nextInt(config.getMaxPackets() - config.getMinPackets() + 1) + config.getMinPackets();
        generatePacketsForWave(system, gameState, playerId, packetType, packetCount);
        
        lastWaveTimes.put(systemId, currentTime);
        
        System.out.println("Generated wave of " + packetCount + " " + packetType + " packets from system " + systemId);
    }

    private String determinePacketType(String playerId, INode system) {
        // Use reflection-based packet type discovery
        return packetTypeRegistry.getRandomPacketTypeForPlayer(playerId);
    }

    private void generatePacketsForWave(INode system, GameState gameState, String playerId, String packetType, int count) {
        Color playerColor = "player1".equals(playerId) ? Color.BLUE : Color.RED;
        
        for (int i = 0; i < count; i++) {
            IConnection connection = findAvailableConnection(system, gameState);
            if (connection != null) {
                Packet packet = new Packet(connection, packetType, playerId, playerColor);
                gameState.getPackets().add(packet);
            }
        }
    }

    private IConnection findAvailableConnection(INode system, GameState gameState) {
        for (IConnection conn : gameState.getConnections()) {
            if (conn.getFromNode().getId().equals(system.getId()) && 
                conn.getPacket() == null && !conn.isDestroyed()) {
                return conn;
            }
        }
        return null;
    }

    private long getMinWaveInterval() {
        return 1000; // 1 second minimum between waves
    }

    private Map<String, List<INode>> getUncontrollableSystems() {
        return controllableSystemManager.getUncontrollableSystems();
    }

    private GameState getPlayerGameState(String playerId) {
        return controllableSystemManager.getPlayerGameState(playerId);
    }

    private static class WaveConfiguration {
        private final String packetType;
        private final long minSpawnTime;
        private final long maxSpawnTime;
        private final long cooldown;
        private final int minPackets;
        private final int maxPackets;
        
        public WaveConfiguration(String packetType, long minSpawnTime, long maxSpawnTime, 
                               long cooldown, int minPackets, int maxPackets) {
            this.packetType = packetType;
            this.minSpawnTime = minSpawnTime;
            this.maxSpawnTime = maxSpawnTime;
            this.cooldown = cooldown;
            this.minPackets = minPackets;
            this.maxPackets = maxPackets;
        }
        
        public String getPacketType() { return packetType; }
        public long getMinSpawnTime() { return minSpawnTime; }
        public long getMaxSpawnTime() { return maxSpawnTime; }
        public long getCooldown() { return cooldown; }
        public int getMinPackets() { return minPackets; }
        public int getMaxPackets() { return maxPackets; }
    }
}
