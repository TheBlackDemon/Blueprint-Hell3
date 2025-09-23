package server;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import network.*;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerMain {
    private static final int SERVER_PORT = 8888;
    private static final int MAX_CLIENTS = 10;
    private static final long HEARTBEAT_INTERVAL = 5000; // 5 seconds
    private static final long CLIENT_TIMEOUT = 30000; // 30 seconds
    private ServerSocket serverSocket;
    private final Map<String, ClientHandler> connectedClients = new ConcurrentHashMap<>();
    private final List<LeaderboardEntry> leaderboard = new CopyOnWriteArrayList<>();
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final ExecutorService clientExecutor = Executors.newCachedThreadPool();
    private final ScheduledExecutorService heartbeatExecutor = Executors.newScheduledThreadPool(1);
    private final AtomicInteger clientIdCounter = new AtomicInteger(0);
    private final Map<String, MultiplayerGameState> activeGames = new ConcurrentHashMap<>();
    private final Map<String, String> clientToGameMap = new ConcurrentHashMap<>();
    private final AtomicInteger gameIdCounter = new AtomicInteger(0);
    private final DataIntegrityValidator dataValidator = new DataIntegrityValidator();
    private static DefaultListModel<String> gameListModel = new DefaultListModel<>();


    public static void main(String[] args) {
        ServerMain server = new ServerMain();
        server.start();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            isRunning.set(true);

            System.out.println("Server started on port " + SERVER_PORT);
            System.out.println("Waiting for client connections...");

            startHeartbeatService();

            startLeaderboardCleanupService();
            
            while (isRunning.get()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    if (connectedClients.size() >= MAX_CLIENTS) {
                        System.out.println("Server full, rejecting connection from " + clientSocket.getInetAddress());
                        clientSocket.close();
                        continue;
                    }

                    String clientId = "client_" + clientIdCounter.incrementAndGet();
                    ClientHandler clientHandler = new ClientHandler(clientId, clientSocket, this);
                    connectedClients.put(clientId, clientHandler);
                    clientExecutor.submit(clientHandler);

                    System.out.println("Client connected: " + clientId + " from " + clientSocket.getInetAddress());
                } catch (IOException e) {
                    if (isRunning.get()) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        } finally {
            stop();
        }
    }
    
    public void stop() {
        isRunning.set(false);

        for (ClientHandler client : connectedClients.values()) {
            client.disconnect();
        }
        connectedClients.clear();

        clientExecutor.shutdown();
        heartbeatExecutor.shutdown();

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }
        
        System.out.println("Server stopped");
    }
    
    private void startHeartbeatService() {
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            if (!isRunning.get()) return;

            long currentTime = System.currentTimeMillis();
            List<String> clientsToRemove = new ArrayList<>();

            for (Map.Entry<String, ClientHandler> entry : connectedClients.entrySet()) {
                ClientHandler client = entry.getValue();
                if (currentTime - client.getLastHeartbeat() > CLIENT_TIMEOUT) {
                    System.out.println("Client " + entry.getKey() + " timed out, removing...");
                    clientsToRemove.add(entry.getKey());
                    client.disconnect();
                } else {
                    client.sendHeartbeat();
                }
            }

            for (String clientId : clientsToRemove) {
                connectedClients.remove(clientId);
            }
        }, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS);
    }
    
    private void startLeaderboardCleanupService() {
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            if (!isRunning.get()) return;
            
            long currentTime = System.currentTimeMillis();
            long maxAge = 24 * 60 * 60 * 1000; // 24 hours
            
            leaderboard.removeIf(entry -> currentTime - entry.getTimestamp() > maxAge);
        }, 60 * 60 * 1000, 60 * 60 * 1000, TimeUnit.MILLISECONDS); // Run every hour
    }
    
    public void removeClient(String clientId) {
        ClientHandler client = connectedClients.remove(clientId);
        if (client != null) {
            System.out.println("Client disconnected: " + clientId);
        }
    }
    
    public void broadcastMessage(NetworkMessage message) {
        for (ClientHandler client : connectedClients.values()) {
            client.sendMessage(message);
        }
    }
    
    public void broadcastMessage(NetworkMessage message, String excludeClientId) {
        for (Map.Entry<String, ClientHandler> entry : connectedClients.entrySet()) {
            if (!entry.getKey().equals(excludeClientId)) {
                entry.getValue().sendMessage(message);
            }
        }
    }
    
    public void addLeaderboardEntry(LeaderboardEntry entry) {
        System.out.println("Leaderboard before add: " + leaderboard.size());
        System.out.println("Added entry: " + entry + ", leaderboard now: " + leaderboard.size());

        leaderboard.add(entry);
        leaderboard.sort((a, b) -> {
            int levelCompare = Integer.compare(b.getLevel(), a.getLevel());
            if (levelCompare != 0) return levelCompare;
            return Long.compare(a.getCompletionTime(), b.getCompletionTime());
        });
        
        if (leaderboard.size() > 100) {
            leaderboard.subList(100, leaderboard.size()).clear();
        }
        
        System.out.println("Added leaderboard entry: " + entry);
    }
    
    public List<LeaderboardEntry> getLeaderboard() {
        return new ArrayList<>(leaderboard);
    }


    public String createMultiplayerGame(String clientId) {
        String gameId = "game_" + gameIdCounter.incrementAndGet();
        MultiplayerGameState gameState = new MultiplayerGameState(gameId);

        ClientHandler client = connectedClients.get(clientId);
        if (client != null) {
            String macAddress = dataValidator.getMacAddressFromConnection(client.getSocket());
            gameState.addPlayer(clientId, macAddress);
            activeGames.put(gameId, gameState);
            clientToGameMap.put(clientId, gameId);
            System.out.println("Created multiplayer game " + gameId + " for client " + clientId);
            return gameId;
        }

        return null;
    }

    

    public boolean joinMultiplayerGame(String clientId, String gameId) {
        MultiplayerGameState gameState = activeGames.get(gameId);
        if (gameState != null && gameState.getAllPlayers().size() < 2) {
            ClientHandler client = connectedClients.get(clientId);
            if (client != null) {
                String macAddress = dataValidator.getMacAddressFromConnection(client.getSocket());
                boolean success = gameState.addPlayer(clientId, macAddress);
                if (success) {
                    clientToGameMap.put(clientId, gameId);
                    System.out.println("Client " + clientId + " joined game " + gameId);
                    // If we now have 2 players, notify both clients to start the game
                    if (gameState.getAllPlayers().size() == 2) {
                        broadcastToGame(gameId, new NetworkMessage(
                            NetworkMessage.MessageType.JOIN_GAME,
                            gameId,
                            "server"
                        ));
                    }
                    return true;
                }
            }
        }
        return false;
    }
    

    public void handlePlayerReady(String clientId, boolean ready) {
        String gameId = clientToGameMap.get(clientId);
        if (gameId != null) {
            MultiplayerGameState gameState = activeGames.get(gameId);
            if (gameState != null) {
                gameState.setPlayerReady(clientId, ready);
                
                broadcastToGame(gameId, new NetworkMessage(
                    NetworkMessage.MessageType.PLAYER_READY,
                    clientId + ":" + ready,
                    clientId
                ));
            }
        }
    }

    public void handleGameStateUpdate(String clientId, GameStateData gameStateData) {
        String gameId = clientToGameMap.get(clientId);
        if (gameId != null) {
            MultiplayerGameState gameState = activeGames.get(gameId);
            if (gameState != null) {
                String macAddress = dataValidator.getMacAddressFromConnection(
                    connectedClients.get(clientId).getSocket()
                );

                DataIntegrityValidator.GameResultData resultData =
                    new DataIntegrityValidator.GameResultData(
                        gameStateData.getLevel(),
                        System.currentTimeMillis() - gameStateData.getLevelStartTime(),
                        gameStateData.getPacketLoss(),
                        gameStateData.getCoins()
                    );

                String dataHash = dataValidator.generateDataHash(resultData, macAddress);
                resultData.setDataHash(dataHash);

                if (dataValidator.validateGameResult(macAddress, resultData)) {
                    dataValidator.updateUserProgress(macAddress, resultData);

                    broadcastToGame(gameId, new NetworkMessage(
                        NetworkMessage.MessageType.GAME_STATE,
                        gameStateData.toJson(),
                        clientId
                    ), clientId);
                } else {
                    connectedClients.get(clientId).sendMessage(new NetworkMessage(
                        NetworkMessage.MessageType.ERROR,
                        "Data validation failed",
                        clientId
                    ));
                }
            }
        }
    }


    public void handleGameResult(String clientId, GameStateData gameStateData) {
        String gameId = clientToGameMap.get(clientId);
        if (gameId != null) {
            MultiplayerGameState gameState = activeGames.get(gameId);
            if (gameState != null) {
                // Validate and process game result
                String macAddress = dataValidator.getMacAddressFromConnection(
                    connectedClients.get(clientId).getSocket()
                );

                DataIntegrityValidator.GameResultData resultData =
                    new DataIntegrityValidator.GameResultData(
                        gameStateData.getLevel(),
                        System.currentTimeMillis() - gameStateData.getLevelStartTime(),
                        gameStateData.getPacketLoss(),
                        gameStateData.getCoins()
                    );

                String dataHash = dataValidator.generateDataHash(resultData, macAddress);
                resultData.setDataHash(dataHash);

                if (dataValidator.validateGameResult(macAddress, resultData)) {
                    dataValidator.updateUserProgress(macAddress, resultData);

                    LeaderboardEntry entry = new LeaderboardEntry(
                        dataValidator.getUserData(macAddress).getUsername(),
                        gameStateData.getLevel(),
                        resultData.getCompletionTime(),
                        gameStateData.getPacketLoss(), // XP
                        gameStateData.getCoins() // Coins
                    );
                    addLeaderboardEntry(entry);

                    if (gameState.isGameStarted()) {
                        MultiplayerGameState.GameResult gameResult = gameState.getGameResult();
                        if (gameResult != null) {
                            broadcastToGame(gameId, new NetworkMessage(
                                NetworkMessage.MessageType.GAME_RESULT,
                                new com.google.gson.Gson().toJson(gameResult),
                                clientId
                            ));
                        }
                    }
                }
            }
        }
    }


    private void broadcastToGame(String gameId, NetworkMessage message) {
        MultiplayerGameState gameState = activeGames.get(gameId);
        if (gameState != null) {
            for (String playerId : gameState.getAllPlayers().keySet()) {
                ClientHandler client = connectedClients.get(playerId);
                if (client != null) {
                    client.sendMessage(message);
                }
            }
        }
    }


    private void broadcastToGame(String gameId, NetworkMessage message, String excludeClientId) {
        MultiplayerGameState gameState = activeGames.get(gameId);
        if (gameState != null) {
            for (String playerId : gameState.getAllPlayers().keySet()) {
                if (!playerId.equals(excludeClientId)) {
                    ClientHandler client = connectedClients.get(playerId);
                    if (client != null) {
                        client.sendMessage(message);
                    }
                }
            }
        }
    }

    public List<String> getAvailableGames() {
        List<String> availableGames = new ArrayList<>();
        for (MultiplayerGameState game : activeGames.values()) {
            if (game.getAllPlayers().size() < 2) {
                availableGames.add(game.getGameId());
            }
        }
        return availableGames;
    }



    public static DefaultListModel<String> getGameListModel() {
        return gameListModel;
    }

}
