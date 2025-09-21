package network;

import Game.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class MultiplayerGameState {
    private static final int NETWORK_SETUP_TIME_MS = 30000; // 30 seconds
    private static final int EXTENDED_TIME_MS = 30000; // 30 seconds additional
    private static final int WRATH_PENIA_DURATION_MS = 10000; // 10 seconds
    private static final int WRATH_AERGIA_DURATION_MS = 10000; // 10 seconds
    private static final int WRATH_PENIA_SPEED_DURATION_MS = 10000; // 10 seconds
    
    private final String gameId;
    private final Map<String, PlayerData> players;
    private final AtomicBoolean gameStarted;
    private final AtomicBoolean networkSetupPhase;
    private final AtomicLong gameStartTime;
    private final AtomicLong networkSetupStartTime;
    private final AtomicLong extendedTimeStartTime;
    private final Map<String, List<ConnectionData>> playerConnections;
    private final Map<String, Boolean> playerReadyStatus;
    private final Map<String, Integer> playerPacketCounts;
    private final Map<String, Integer> playerPacketLoss;
    private final Map<String, Color> playerColors;
    private final Map<String, Long> wrathEffects;
    private final Random random;
    private final Gson gson;
    private final ControllableSystemManager controllableSystemManager;
    
    public MultiplayerGameState(String gameId) {
        this.gameId = gameId;
        this.players = new ConcurrentHashMap<>();
        this.gameStarted = new AtomicBoolean(false);
        this.networkSetupPhase = new AtomicBoolean(true);
        this.gameStartTime = new AtomicLong(0);
        this.networkSetupStartTime = new AtomicLong(System.currentTimeMillis());
        this.extendedTimeStartTime = new AtomicLong(0);
        this.playerConnections = new ConcurrentHashMap<>();
        this.playerReadyStatus = new ConcurrentHashMap<>();
        this.playerPacketCounts = new ConcurrentHashMap<>();
        this.playerPacketLoss = new ConcurrentHashMap<>();
        this.playerColors = new ConcurrentHashMap<>();
        this.wrathEffects = new ConcurrentHashMap<>();
        this.random = new Random();
        this.gson = new GsonBuilder().create();
        this.controllableSystemManager = new ControllableSystemManager();
        
        initializePlayerColors();
    }
    
    private void initializePlayerColors() {
        playerColors.put("player1", Color.BLUE);
        playerColors.put("player2", Color.RED);
    }

    public boolean addPlayer(String playerId, String macAddress) {
        if (players.size() >= 2) {
            return false; // Game is full
        }
        
        PlayerData player = new PlayerData(playerId, macAddress);
        players.put(playerId, player);
        playerConnections.put(playerId, new ArrayList<>());
        playerReadyStatus.put(playerId, false);
        playerPacketCounts.put(playerId, 0);
        playerPacketLoss.put(playerId, 0);
        
        System.out.println("Player " + playerId + " joined game " + gameId);
        return true;
    }

    public void removePlayer(String playerId) {
        players.remove(playerId);
        playerConnections.remove(playerId);
        playerReadyStatus.remove(playerId);
        playerPacketCounts.remove(playerId);
        playerPacketLoss.remove(playerId);
        System.out.println("Player " + playerId + " left game " + gameId);
    }

    public boolean setPlayerReady(String playerId, boolean ready) {
        if (!players.containsKey(playerId)) {
            return false;
        }
        
        playerReadyStatus.put(playerId, ready);
        
        if (ready && allPlayersReady()) {
            startGame();
        }
        
        return true;
    }

    public boolean allPlayersReady() {
        return players.size() == 2 && 
               playerReadyStatus.values().stream().allMatch(Boolean::booleanValue);
    }

    public void startGame() {
        if (gameStarted.compareAndSet(false, true)) {
            gameStartTime.set(System.currentTimeMillis());
            networkSetupPhase.set(false);
            System.out.println("Game " + gameId + " started!");
        }
    }

    public boolean addPlayerConnection(String playerId, ConnectionData connection) {
        if (!players.containsKey(playerId) || !networkSetupPhase.get()) {
            return false;
        }
        
        playerConnections.get(playerId).add(connection);
        return true;
    }

    public boolean removePlayerConnection(String playerId, String connectionId) {
        if (!players.containsKey(playerId)) {
            return false;
        }
        
        List<ConnectionData> connections = playerConnections.get(playerId);
        return connections.removeIf(conn -> conn.getId().equals(connectionId));
    }

    public void updatePacketCount(String playerId, int count) {
        playerPacketCounts.put(playerId, count);
    }

    public void updatePacketLoss(String playerId, int loss) {
        playerPacketLoss.put(playerId, loss);
    }

    public long getRemainingSetupTime() {
        if (!networkSetupPhase.get()) {
            return 0;
        }
        
        long elapsed = System.currentTimeMillis() - networkSetupStartTime.get();
        long remaining = NETWORK_SETUP_TIME_MS - elapsed;
        
        if (remaining <= 0) {
            if (extendedTimeStartTime.get() == 0) {
                extendedTimeStartTime.set(System.currentTimeMillis());
                applyWrathEffects();
            }
            
            long extendedElapsed = System.currentTimeMillis() - extendedTimeStartTime.get();
            remaining = Math.max(0, EXTENDED_TIME_MS - extendedElapsed);
        }
        
        return remaining;
    }

    private void applyWrathEffects() {
        long extendedElapsed = System.currentTimeMillis() - extendedTimeStartTime.get();
        
        if (extendedElapsed <= WRATH_PENIA_DURATION_MS) {
            applyWrathOfPenia();
        } else if (extendedElapsed <= WRATH_PENIA_DURATION_MS + WRATH_AERGIA_DURATION_MS) {
            applyWrathOfAergia();
        } else if (extendedElapsed <= WRATH_PENIA_DURATION_MS + WRATH_AERGIA_DURATION_MS + WRATH_PENIA_SPEED_DURATION_MS) {
            applyWrathOfPeniaSpeed();
        }
    }
    
    private void applyWrathOfPenia() {
        for (String playerId : players.keySet()) {
            List<String> opponentIds = new ArrayList<>(players.keySet());
            opponentIds.remove(playerId);
            
            if (!opponentIds.isEmpty()) {
                String opponentId = opponentIds.get(0);
                if (System.currentTimeMillis() % 2000 < 100) {
                    addRandomPacketToOpponent(playerId, opponentId);
                }
            }
        }
    }
    
    private void applyWrathOfAergia() {
        long secondsElapsed = (System.currentTimeMillis() - extendedTimeStartTime.get() - WRATH_PENIA_DURATION_MS) / 1000;
        double cooldownMultiplier = 1.0 + (secondsElapsed * 0.01);
        wrathEffects.put("aergia_cooldown", (long) (cooldownMultiplier * 1000));
    }
    
    private void applyWrathOfPeniaSpeed() {
        long secondsElapsed = (System.currentTimeMillis() - extendedTimeStartTime.get() -
                              WRATH_PENIA_DURATION_MS - WRATH_AERGIA_DURATION_MS) / 1000;
        double speedMultiplier = 1.0 + (secondsElapsed * 0.03);
        wrathEffects.put("penia_speed", (long) (speedMultiplier * 1000));
    }
    
    private void addRandomPacketToOpponent(String playerId, String opponentId) {

        System.out.println("Wrath of Penia: Adding random packet to " + opponentId + " from " + playerId);
    }

    public GameStatus getGameStatus() {
        if (gameStarted.get()) {
            return GameStatus.IN_PROGRESS;
        } else if (networkSetupPhase.get()) {
            return GameStatus.NETWORK_SETUP;
        } else {
            return GameStatus.WAITING_FOR_PLAYERS;
        }
    }

    public PlayerData getPlayer(String playerId) {
        return players.get(playerId);
    }

    public Map<String, PlayerData> getAllPlayers() {
        return new HashMap<>(players);
    }

    public List<ConnectionData> getPlayerConnections(String playerId) {
        return new ArrayList<>(playerConnections.getOrDefault(playerId, new ArrayList<>()));
    }

    public Color getPlayerColor(String playerId) {
        return playerColors.getOrDefault(playerId, Color.BLACK);
    }

    public GameResult getGameResult() {
        if (!gameStarted.get()) {
            return null;
        }
        
        Map<String, Integer> scores = new HashMap<>();
        for (String playerId : players.keySet()) {
            int score = playerPacketCounts.getOrDefault(playerId, 0) - 
                       playerPacketLoss.getOrDefault(playerId, 0);
            scores.put(playerId, score);
        }
        
        return new GameResult(gameId, scores, System.currentTimeMillis());
    }

    public void addControllableSystem(ControllableReferenceSystem system) {
        controllableSystemManager.addControllableSystem(system);
    }

    public void addUncontrollableSystem(String playerId, INode system) {
        controllableSystemManager.addUncontrollableSystem(playerId, system);
    }

    public void registerPlayerGameState(String playerId, GameState gameState) {
        controllableSystemManager.registerPlayerGameState(playerId, gameState);
    }

    public boolean firePacketFromSystem(String systemId, AmmunitionType type, String firingPlayerId) {
        return controllableSystemManager.firePacket(systemId, type, firingPlayerId);
    }

    public List<ControllableReferenceSystem> getControllableSystemsForPlayer(String playerId) {
        return controllableSystemManager.getControllableSystemsForPlayer(playerId);
    }

    public List<INode> getUncontrollableSystemsForPlayer(String playerId) {
        return controllableSystemManager.getUncontrollableSystemsForPlayer(playerId);
    }

    public void updateControllableSystems() {
        controllableSystemManager.update();
    }

    public ControllableReferenceSystem getControllableSystem(String systemId) {
        return controllableSystemManager.getControllableSystem(systemId);
    }

    public String getGameId() { return gameId; }
    public boolean isGameStarted() { return gameStarted.get(); }
    public boolean isNetworkSetupPhase() { return networkSetupPhase.get(); }
    public long getGameStartTime() { return gameStartTime.get(); }
    public Map<String, Long> getWrathEffects() { return new HashMap<>(wrathEffects); }
    public ControllableSystemManager getControllableSystemManager() { return controllableSystemManager; }

    public static class PlayerData {
        private String playerId;
        private String macAddress;
        private String username;
        private long joinTime;
        private boolean isReady;
        
        public PlayerData(String playerId, String macAddress) {
            this.playerId = playerId;
            this.macAddress = macAddress;
            this.username = "Player_" + playerId;
            this.joinTime = System.currentTimeMillis();
            this.isReady = false;
        }
        
        public String getPlayerId() { return playerId; }
        public void setPlayerId(String playerId) { this.playerId = playerId; }
        
        public String getMacAddress() { return macAddress; }
        public void setMacAddress(String macAddress) { this.macAddress = macAddress; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public long getJoinTime() { return joinTime; }
        public void setJoinTime(long joinTime) { this.joinTime = joinTime; }
        
        public boolean isReady() { return isReady; }
        public void setReady(boolean ready) { this.isReady = ready; }
    }

    public static class ConnectionData {
        private String id;
        private String fromNodeId;
        private String toNodeId;
        private int fromPort;
        private int toPort;
        private String playerId;
        
        public ConnectionData(String id, String fromNodeId, String toNodeId, 
                            int fromPort, int toPort, String playerId) {
            this.id = id;
            this.fromNodeId = fromNodeId;
            this.toNodeId = toNodeId;
            this.fromPort = fromPort;
            this.toPort = toPort;
            this.playerId = playerId;
        }
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getFromNodeId() { return fromNodeId; }
        public void setFromNodeId(String fromNodeId) { this.fromNodeId = fromNodeId; }
        
        public String getToNodeId() { return toNodeId; }
        public void setToNodeId(String toNodeId) { this.toNodeId = toNodeId; }
        
        public int getFromPort() { return fromPort; }
        public void setFromPort(int fromPort) { this.fromPort = fromPort; }
        
        public int getToPort() { return toPort; }
        public void setToPort(int toPort) { this.toPort = toPort; }
        
        public String getPlayerId() { return playerId; }
        public void setPlayerId(String playerId) { this.playerId = playerId; }
    }
    

    public static class GameResult {
        private String gameId;
        private Map<String, Integer> playerScores;
        private long endTime;
        private String winner;
        
        public GameResult(String gameId, Map<String, Integer> playerScores, long endTime) {
            this.gameId = gameId;
            this.playerScores = new HashMap<>(playerScores);
            this.endTime = endTime;
            this.winner = determineWinner();
        }
        
        private String determineWinner() {
            return playerScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
        }
        
        public String getGameId() { return gameId; }
        public Map<String, Integer> getPlayerScores() { return new HashMap<>(playerScores); }
        public long getEndTime() { return endTime; }
        public String getWinner() { return winner; }
    }

    public enum GameStatus {
        WAITING_FOR_PLAYERS,
        NETWORK_SETUP,
        IN_PROGRESS,
        FINISHED
    }
}
