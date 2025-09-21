package Game;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class ControllableSystemManager {
    private final Map<String, ControllableReferenceSystem> controllableSystems;
    private final Map<String, List<INode>> uncontrollableSystems;
    private final Map<String, GameState> playerGameStates;
    private final Random random;
    private final PacketWaveManager waveManager;

    public ControllableSystemManager() {
        this.controllableSystems = new ConcurrentHashMap<>();
        this.uncontrollableSystems = new ConcurrentHashMap<>();
        this.playerGameStates = new ConcurrentHashMap<>();
        this.random = new Random();
        this.waveManager = new PacketWaveManager(this);
    }

    public void addControllableSystem(ControllableReferenceSystem system) {
        controllableSystems.put(system.getId(), system);
    }

    public void addUncontrollableSystem(String playerId, INode system) {
        uncontrollableSystems.computeIfAbsent(playerId, k -> new ArrayList<>()).add(system);
    }

    public void registerPlayerGameState(String playerId, GameState gameState) {
        playerGameStates.put(playerId, gameState);
    }

    public boolean firePacket(String systemId, AmmunitionType type, String firingPlayerId) {
        ControllableReferenceSystem system = controllableSystems.get(systemId);
        if (system == null || !system.getOwnerPlayerId().equals(firingPlayerId)) {
            return false;
        }

        String targetPlayerId = system.getTargetPlayerId();
        GameState targetGameState = playerGameStates.get(targetPlayerId);
        if (targetGameState == null) {
            return false;
        }

        return system.firePacket(type, targetGameState);
    }

    public List<ControllableReferenceSystem> getControllableSystemsForPlayer(String playerId) {
        return controllableSystems.values().stream()
                .filter(system -> system.getOwnerPlayerId().equals(playerId))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    public List<INode> getUncontrollableSystemsForPlayer(String playerId) {
        return uncontrollableSystems.getOrDefault(playerId, new ArrayList<>());
    }

    public void update() {
        for (ControllableReferenceSystem system : controllableSystems.values()) {
            system.update();
        }

        waveManager.updateWaves();
    }

    private void updateUncontrollableSystems() {
        for (Map.Entry<String, List<INode>> entry : uncontrollableSystems.entrySet()) {
            String playerId = entry.getKey();
            List<INode> systems = entry.getValue();
            GameState gameState = playerGameStates.get(playerId);
            
            if (gameState == null) continue;

            for (INode system : systems) {
                if (system.canOperate()) {
                    generateAutomaticPacket(system, gameState, playerId);
                }
            }
        }
    }

    private void generateAutomaticPacket(INode system, GameState gameState, String playerId) {
        IConnection targetConnection = findSuitableConnection(system, gameState);
        if (targetConnection == null) return;

        String packetType = getAlternatingPacketType(playerId);
        
        Color playerColor = "player1".equals(playerId) ? Color.BLUE : Color.RED;
        Packet packet = new Packet(targetConnection, packetType, playerId, playerColor);
        gameState.getPackets().add(packet);

        System.out.println("Uncontrollable system " + system.getId() + " generated " + packetType + " packet for player " + playerId);
    }

    private IConnection findSuitableConnection(INode system, GameState gameState) {
        for (IConnection conn : gameState.getConnections()) {
            if (conn.getFromNode().getId().equals(system.getId()) && 
                conn.getPacket() == null && !conn.isDestroyed()) {
                return conn;
            }
        }
        return null;
    }

    private String getAlternatingPacketType(String playerId) {
        String[] type1Packets = {"square", "triangle"};
        String[] type2Packets = {"confidential_4", "confidential_6"};
        
        if ("player1".equals(playerId)) {
            return type1Packets[random.nextInt(type1Packets.length)];
        } else {
            return type2Packets[random.nextInt(type2Packets.length)];
        }
    }

    public Collection<ControllableReferenceSystem> getAllControllableSystems() {
        return controllableSystems.values();
    }

    public ControllableReferenceSystem getControllableSystem(String systemId) {
        return controllableSystems.get(systemId);
    }

    public boolean isControllableSystem(String systemId) {
        return controllableSystems.containsKey(systemId);
    }


    public void removeControllableSystem(String systemId) {
        controllableSystems.remove(systemId);
    }

    public void clear() {
        controllableSystems.clear();
        uncontrollableSystems.clear();
        playerGameStates.clear();
    }

    public Map<String, List<INode>> getUncontrollableSystems() {
        return new HashMap<>(uncontrollableSystems);
    }

    public GameState getPlayerGameState(String playerId) {
        return playerGameStates.get(playerId);
    }
}
