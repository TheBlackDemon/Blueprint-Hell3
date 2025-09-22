package server;

import network.GameStateData;
import network.LeaderboardEntry;
import network.NetworkMessage;
import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;


public class ClientHandler implements Runnable {
    private final String clientId;
    private final Socket socket;
    private final ServerMain server;
    private final BufferedReader reader;
    private final PrintWriter writer;
    private final AtomicBoolean connected = new AtomicBoolean(true);
    private final AtomicLong lastHeartbeat = new AtomicLong(System.currentTimeMillis());

    public ClientHandler(String clientId, Socket socket, ServerMain server) throws IOException {
        this.clientId = clientId;
        this.socket = socket;
        this.server = server;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new PrintWriter(socket.getOutputStream(), true);
    }
    
    @Override
    public void run() {
        try {
            sendMessage(new NetworkMessage(NetworkMessage.MessageType.CONNECT, "Connected to server", clientId));
            
            String inputLine;
            while (connected.get() && (inputLine = reader.readLine()) != null) {
                try {
                    NetworkMessage message = NetworkMessage.fromJson(inputLine);
                    handleMessage(message);
                } catch (Exception e) {
                    System.err.println("Error processing message from " + clientId + ": " + e.getMessage());
                    sendError("Invalid message format");
                }
            }
        } catch (IOException e) {
            if (connected.get()) {
                System.err.println("Error handling client " + clientId + ": " + e.getMessage());
            }
        } finally {
            disconnect();
        }
    }
    
    private void handleMessage(NetworkMessage message) {
        switch (message.getType()) {
            case CONNECT:
                handleConnect(message);
                break;
            case DISCONNECT:
                handleDisconnect(message);
                break;
            case GAME_STATE:
                handleGameState(message);
                break;
            case USER_INPUT:
                handleUserInput(message);
                break;
            case LEADERBOARD:
                handleLeaderboardRequest(message);
                break;
            case GAME_RESULT:
                handleGameResult(message);
                break;
            case HEARTBEAT:
                handleHeartbeat(message);
                break;
            case ERROR:
                handleError(message);
                break;
            case CREATE_GAME:
                handleCreateGame(message);
                break;
            case JOIN_GAME:
                handleJoinGame(message);
                break;
            case PLAYER_READY:
                handlePlayerReady(message);
                break;
            case WRATH_EFFECT:
                handleWrathEffect(message);
                break;
            case GAME_LIST:
                handleGameListRequest(message);
                break;
            case PLAYER_CONNECTION:
                handlePlayerConnection(message);
                break;
            case PACKET_UPDATE:
                handlePacketUpdate(message);
                break;
            default:
                System.out.println("Unknown message type from " + clientId + ": " + message.getType());
        }
    }
    
    private void handleConnect(NetworkMessage message) {
        System.out.println("Client " + clientId + " connected");
        lastHeartbeat.set(System.currentTimeMillis());
    }
    
    private void handleDisconnect(NetworkMessage message) {
        System.out.println("Client " + clientId + " disconnecting");
        disconnect();
    }
    
    private void handleGameState(NetworkMessage message) {
        message.setClientId(clientId);
        server.broadcastMessage(message, clientId);
        lastHeartbeat.set(System.currentTimeMillis());
    }
    
    private void handleUserInput(NetworkMessage message) {
        message.setClientId(clientId);
        server.broadcastMessage(message, clientId);
        lastHeartbeat.set(System.currentTimeMillis());
    }
    
    private void handleLeaderboardRequest(NetworkMessage message) {
        List<LeaderboardEntry> leaderboard = server.getLeaderboard();
        String leaderboardJson = new com.google.gson.Gson().toJson(leaderboard);
        
        NetworkMessage response = new NetworkMessage(
            NetworkMessage.MessageType.LEADERBOARD, 
            leaderboardJson, 
            clientId
        );
        sendMessage(response);
        lastHeartbeat.set(System.currentTimeMillis());
    }

    private void handleGameResult(NetworkMessage message) {
        System.out.println("Received GAME_RESULT from " + clientId + ": " + message.getData());

        try {
            GameStateData gameState = GameStateData.fromJson(message.getData());

            LeaderboardEntry entry = new LeaderboardEntry(
                    gameState.getUsername(),
                    gameState.getLevel(),
                    System.currentTimeMillis() - gameState.getLevelStartTime(),
                    calculateXP(gameState),
                    gameState.getCoins()
            );
            entry.setSessionId(clientId);

            server.addLeaderboardEntry(entry);

            List<LeaderboardEntry> leaderboard = server.getLeaderboard();
            String leaderboardJson = new com.google.gson.Gson().toJson(leaderboard);
            NetworkMessage broadcast = new NetworkMessage(
                    NetworkMessage.MessageType.LEADERBOARD,
                    leaderboardJson,
                    "server"
            );
            server.broadcastMessage(broadcast);

            System.out.println("Game result processed for " + clientId + ": " + entry);

        } catch (Exception e) {
            System.err.println("Error processing game result from " + clientId + ": " + e.getMessage());
            sendError("Invalid game result format");
        }
    }

    
    private void handleHeartbeat(NetworkMessage message) {
        lastHeartbeat.set(System.currentTimeMillis());
        sendMessage(new NetworkMessage(NetworkMessage.MessageType.HEARTBEAT, "pong", clientId));
    }
    
    private void handleError(NetworkMessage message) {
        System.err.println("Error from client " + clientId + ": " + message.getData());
        lastHeartbeat.set(System.currentTimeMillis());
    }

    private void handleCreateGame(NetworkMessage message) {
        String gameId = server.createMultiplayerGame(clientId);
        if (gameId != null) {
            sendMessage(new NetworkMessage(NetworkMessage.MessageType.CREATE_GAME, gameId, clientId));
        } else {
            sendError("Failed to create game");
        }
        lastHeartbeat.set(System.currentTimeMillis());
    }
    
    private void handleJoinGame(NetworkMessage message) {
        String gameId = message.getData();
        boolean success = server.joinMultiplayerGame(clientId, gameId);
        if (success) {
            sendMessage(new NetworkMessage(NetworkMessage.MessageType.JOIN_GAME, gameId, clientId));
        } else {
            sendError("Failed to join game");
        }
        lastHeartbeat.set(System.currentTimeMillis());
    }
    
    private void handlePlayerReady(NetworkMessage message) {
        boolean ready = "ready".equals(message.getData());
        server.handlePlayerReady(clientId, ready);
        lastHeartbeat.set(System.currentTimeMillis());
    }
    
    private void handleWrathEffect(NetworkMessage message) {
        String effectType = message.getData();
        System.out.println("Wrath effect " + effectType + " applied by " + clientId);
        lastHeartbeat.set(System.currentTimeMillis());
    }
    
    private void handleGameListRequest(NetworkMessage message) {
        List<String> availableGames = server.getAvailableGames();
        String gamesJson = new com.google.gson.Gson().toJson(availableGames);
        sendMessage(new NetworkMessage(NetworkMessage.MessageType.GAME_LIST, gamesJson, clientId));
        lastHeartbeat.set(System.currentTimeMillis());
    }
    
    private void handlePlayerConnection(NetworkMessage message) {
        try {
            System.out.println("Player connection data from " + clientId + ": " + message.getData());
        } catch (Exception e) {
            System.err.println("Error processing player connection data: " + e.getMessage());
        }
        lastHeartbeat.set(System.currentTimeMillis());
    }
    
    private void handlePacketUpdate(NetworkMessage message) {
        try {
            System.out.println("Packet update from " + clientId + ": " + message.getData());
        } catch (Exception e) {
            System.err.println("Error processing packet update: " + e.getMessage());
        }
        lastHeartbeat.set(System.currentTimeMillis());
    }
    
    private int calculateXP(GameStateData gameState) {
        int baseXP = gameState.getLevel() * 100;
        int packetBonus = (gameState.getNumberPacketsSquare() + gameState.getNumberPacketTriangle() + 
                          gameState.getNumberPacketsCircle()) * 10;
        int timeBonus = Math.max(0, 1000 - (int)(System.currentTimeMillis() - gameState.getLevelStartTime()) / 1000);
        int lossPenalty = gameState.getPacketLoss() * 5;
        
        return Math.max(0, baseXP + packetBonus + timeBonus - lossPenalty);
    }
    
    public void sendMessage(NetworkMessage message) {
        if (connected.get()) {
            try {
                writer.println(message.toJson());
                writer.flush();
            } catch (Exception e) {
                System.err.println("Error sending message to " + clientId + ": " + e.getMessage());
                disconnect();
            }
        }
    }
    
    public void sendHeartbeat() {
        sendMessage(new NetworkMessage(NetworkMessage.MessageType.HEARTBEAT, "ping", "server"));
    }
    
    public void sendError(String errorMessage) {
        sendMessage(new NetworkMessage(NetworkMessage.MessageType.ERROR, errorMessage, "server"));
    }
    
    public void disconnect() {
        if (connected.compareAndSet(true, false)) {
            try {
                server.removeClient(clientId);
                socket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket for " + clientId + ": " + e.getMessage());
            }
        }
    }
    
    public long getLastHeartbeat() {
        return lastHeartbeat.get();
    }

    
    public Socket getSocket() {
        return socket;
    }


}
