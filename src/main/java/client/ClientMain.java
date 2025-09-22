package client;

import com.google.gson.*;
import network.*;
import view.MultiplayerLobbyPanel;
import view.Window;
import controller.User;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Type;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientMain {
    private static final String DEFAULT_SERVER_HOST = "localhost";
    private static final int DEFAULT_SERVER_PORT = 8888;
    private static final int CONNECTION_TIMEOUT = 5000;
    private static final long HEARTBEAT_INTERVAL = 10000;
    private String serverHost;
    private int serverPort;
    public Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final ExecutorService networkExecutor = Executors.newCachedThreadPool();
    private final ScheduledExecutorService heartbeatExecutor = Executors.newScheduledThreadPool(1);
    private String clientId;
    private NetworkManager networkManager;
    private OfflineGameManager offlineGameManager;
    private Window window;
    private String gameId;
    private MultiplayerLobbyPanel multiplayerLobbyPanel;
    private User user;

    public ClientMain() {
        this(DEFAULT_SERVER_HOST, DEFAULT_SERVER_PORT);
    }

    public ClientMain(String host, int port) {
        this.serverHost = host;
        this.serverPort = port;
        this.offlineGameManager = new OfflineGameManager();
    }

    public static void main(String[] args) {
        ClientMain client = new ClientMain();
        client.start();
    }

    public void start() {
        user = new User("Player_" + System.currentTimeMillis());
        window = new Window(this, user, true);
        if (connectToServer()) {
            System.out.println("Connected to server successfully!");
        } else {
            System.out.println("Failed to connect to server. Starting in offline mode...");
            startGame(false);
        }
    }

    private boolean connectToServer() {
        try {
            System.out.println("Attempting to connect to server at " + serverHost + ":" + serverPort);
            socket = new Socket();
            socket.connect(new InetSocketAddress(serverHost, serverPort), CONNECTION_TIMEOUT);

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            networkManager = new NetworkManager(this);
            networkExecutor.submit(networkManager);

            startHeartbeat();

            connected.set(true);
            isRunning.set(true);
            return true;

        } catch (IOException e) {
            System.err.println("Connection failed: " + e.getMessage());
            connected.set(false);
            return false;
        }
    }

    private void startGame(boolean onlineMode) {
        try {
            user = new User("Player_" + System.currentTimeMillis());
            window = new Window(this, user, onlineMode);
            System.out.println("Game started in " + (onlineMode ? "online" : "offline") + " mode");
        } catch (Exception e) {
            System.err.println("Error starting game: " + e.getMessage());
            startOfflineGame();
        }
    }

    private void startOfflineGame() {
        try {
            user = new User("Player_" + System.currentTimeMillis());
            window = new Window(this, user, false);
            System.out.println("Game started in offline mode");
        } catch (Exception e) {
            System.err.println("Error starting offline game: " + e.getMessage());
        }
    }

    private void startHeartbeat() {
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            if (connected.get()) {
                sendMessage(new NetworkMessage(NetworkMessage.MessageType.HEARTBEAT, "ping", clientId));
            }
        }, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS);
    }

    public synchronized void sendMessage(NetworkMessage message) {
        if (connected.get() && writer != null) {
            try {
                writer.println(message.toJson());
                writer.flush();
            } catch (Exception e) {
                System.err.println("Error sending message: " + e.getMessage());
                disconnect();
            }
        } else {
            if (message.getType() == NetworkMessage.MessageType.GAME_RESULT) {
                try {
                    GameStateData data = GameStateData.fromJson(message.getData());
                    offlineGameManager.saveGameResult(data);
                } catch (Exception ex) {
                    System.err.println("Failed to save game result offline: " + ex.getMessage());
                }
            } else {
                System.out.println("Not connected: message not sent (type=" + message.getType() + ")");
            }
        }
    }

    public void sendGameState(GameStateData gameState) {
        if (connected.get()) {
            String gameStateJson = gameState.toJson();
            sendMessage(new NetworkMessage(NetworkMessage.MessageType.GAME_STATE, gameStateJson, clientId));
        } else {
            offlineGameManager.saveGameResult(gameState);
        }
    }
    public void sendGameResult(GameStateData gameState) {
        if (connected.get()) {
            String gameStateJson = gameState.toJson();
            sendMessage(new NetworkMessage(NetworkMessage.MessageType.GAME_RESULT, gameStateJson, clientId));
            requestLeaderboard();
        } else {
            System.out.println("Offline mode: game result saved locally only.");
            if (window != null) {
                window.showLeaderboard(offlineGameManager.getLocalLeaderboard());
            }
        }
    }


    public void requestLeaderboard() {
        sendMessage(new NetworkMessage(NetworkMessage.MessageType.LEADERBOARD, "", clientId));
    }

    public void disconnect() {
        if (connected.compareAndSet(true, false)) {
            try {
                sendMessage(new NetworkMessage(NetworkMessage.MessageType.DISCONNECT, "Goodbye", clientId));
            } catch (Exception ignored) {}

            try {
                if (networkManager != null) networkManager.stop();
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }

            networkExecutor.shutdownNow();
            heartbeatExecutor.shutdownNow();

            System.out.println("Disconnected from server");
            if (window != null) {
                window.onNetworkDisconnected();
            }
        }
    }

    public void handleMessage(NetworkMessage message) {
        switch (message.getType()) {
            case CONNECT -> handleConnect(message);
            case DISCONNECT -> handleDisconnect(message);
            case GAME_STATE -> handleGameState(message);
            case USER_INPUT -> handleUserInput(message);
            case LEADERBOARD -> handleLeaderboard(message);
            case GAME_RESULT -> handleGameResult(message);
            case HEARTBEAT -> handleHeartbeat(message);
            case ERROR -> handleError(message);
            case GAME_LIST -> handleGameList(message);
            case JOIN_GAME -> handleJoinGame(message);
            default -> System.out.println("Unknown message type: " + message.getType());
        }
    }
    private void handleJoinGame(NetworkMessage message){
        String gameId = message.getData();
        multiplayerLobbyPanel.handleGameJoined(gameId);
    }
    private void handleGameList(NetworkMessage message){
        java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<List<String>>(){}.getType();
        List<String> availableGames = new com.google.gson.Gson().fromJson(message.getData(), listType);
        multiplayerLobbyPanel.updateGameList(availableGames);
    }

    private void handleConnect(NetworkMessage message) {
        System.out.println("Connected to server: " + message.getData());
        this.clientId = message.getClientId();
        if (window != null) {
            window.setNetworkConnected(true, serverHost + ":" + serverPort, clientId);
        }
    }

    private void handleDisconnect(NetworkMessage message) {
        System.out.println("Server disconnected: " + message.getData());
        disconnect();
    }

    private void handleGameState(NetworkMessage message) {
        try {
            GameStateData gameState = GameStateData.fromJson(message.getData());
            System.out.println("Received game state update from " + message.getClientId());
            if (window != null) {
                window.updateRemoteGameState(gameState);
            }
        } catch (Exception e) {
            System.err.println("Error processing game state: " + e.getMessage());
        }
    }

    private void handleUserInput(NetworkMessage message) {
        System.out.println("Received user input from " + message.getClientId() + ": " + message.getData());
    }
    private void handleLeaderboard(NetworkMessage message) {
        try {
            LeaderboardEntry[] leaderboard = new Gson().fromJson(message.getData(), LeaderboardEntry[].class);
            System.out.println("Received leaderboard update with " + leaderboard.length + " entries");
            List<LeaderboardEntry> list = Arrays.asList(leaderboard);

            if (list.isEmpty()) {
                list = offlineGameManager.getLocalLeaderboard();
                System.out.println("Using local leaderboard (offline fallback)");
            } else {
                offlineGameManager.updateLeaderboard(list);
            }

            if (window != null) {
                window.showLeaderboard(list);
            }
        } catch (Exception e) {
            System.err.println("Error processing leaderboard: " + e.getMessage());
            if (window != null) {
                window.showLeaderboard(offlineGameManager.getLocalLeaderboard());
            }
        }
    }


    private void handleGameResult(NetworkMessage message) {
        System.out.println("Received game result from " + message.getClientId());
    }

    private void handleHeartbeat(NetworkMessage message) {
        System.out.println("Received heartbeat: " + message.getData());
    }

    private void handleError(NetworkMessage message) {
        System.err.println("Server error: " + message.getData());
        if (window != null) {
            window.showServerError(message.getData());
        }
    }

    public boolean isConnected() {
        return connected.get();
    }

    public String getClientId() {
        return clientId;
    }

    public OfflineGameManager getOfflineGameManager() {
        return offlineGameManager;
    }

    public Window getWindow() {
        return window;
    }

    public void synchronizePendingGames() {
        if (!isConnected()) return;
        List<GameStateData> pending = offlineGameManager.getPendingGames();
        if (pending == null || pending.isEmpty()) return;
        List<GameStateData> synced = new ArrayList<>();
        for (GameStateData gs : pending) {
            try {
                sendGameResult(gs);
                synced.add(gs);
            } catch (Exception e) {
                System.err.println("Failed to sync game: " + e.getMessage());
            }
        }
        offlineGameManager.markGamesAsSynchronized(synced);
    }
    public boolean connectToServerForUI() {
        try {
            if (socket != null && !socket.isClosed()) {
                try { socket.close(); } catch (Exception ignored) {}
            }
            socket = new Socket();
            socket.connect(new InetSocketAddress(this.serverHost, this.serverPort), CONNECTION_TIMEOUT);

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            if (networkManager != null) networkManager.stop();
            networkManager = new NetworkManager(this);
            networkExecutor.submit(networkManager);

            startHeartbeat();

            connected.set(true);
            isRunning.set(true);
            return true;
        } catch (Exception e) {
            System.err.println("connectToServerForUI failed: " + e.getMessage());
            connected.set(false);
            return false;
        }
    }



    public void setMultiplayerLobbyPanel(MultiplayerLobbyPanel multiplayerLobbyPanel) {
        this.multiplayerLobbyPanel = multiplayerLobbyPanel;
    }

    public User getUser() {
        return user;
    }
}
