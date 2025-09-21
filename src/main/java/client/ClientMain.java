package client;

import com.google.gson.Gson;
import network.GameStateData;
import network.LeaderboardEntry;
import network.NetworkMessage;
import view.Window;
import controller.User;
import network.OfflineGameManager;

import java.io.*;
import java.net.*;
import java.util.*;
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
    private String sessionId;
    private NetworkManager networkManager;
    private OfflineGameManager offlineGameManager;
    private Window window;

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
        if (connectToServer()) {
            System.out.println("Connected to server successfully!");
            startGame(true);
        } else {
            System.out.println("Failed to connect to server. Starting in offline mode...");
            startGame(false);
        }
    }

    private boolean connectToServer() {
        try {
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
            connected.set(false);
            return false;
        }
    }

    public boolean connectToServerForUI() {
        try {
            if (socket != null && !socket.isClosed()) socket.close();

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
            connected.set(false);
            return false;
        }
    }

    private void startGame(boolean onlineMode) {
        try {
            User user = new User("Player_" + System.currentTimeMillis());
            window = new Window(this, user, onlineMode);
        } catch (Exception e) {
            startOfflineGame();
        }
    }

    private void startOfflineGame() {
        try {
            User user = new User("Player_" + System.currentTimeMillis());
            window = new Window(this, user, false);
        } catch (Exception ignored) {}
    }

    private void startHeartbeat() {
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            if (connected.get()) sendMessage(new NetworkMessage(NetworkMessage.MessageType.HEARTBEAT, "ping", clientId));
        }, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS);
    }

    public synchronized void sendMessage(NetworkMessage message) {
        if (connected.get() && writer != null) {
            try {
                writer.println(message.toJson());
                writer.flush();
            } catch (Exception e) {
                disconnect();
            }
        } else {
            if (message.getType() == NetworkMessage.MessageType.GAME_RESULT) {
                try {
                    GameStateData data = GameStateData.fromJson(message.getData());
                    offlineGameManager.saveGameResult(data);
                } catch (Exception ignored) {}
            }
        }
    }

    public void sendGameResult(GameStateData gameState) {
        String gameStateJson = gameState.toJson();

        offlineGameManager.addToLeaderboardOnly(gameState);

        if (connected.get()) {
            sendMessage(new NetworkMessage(NetworkMessage.MessageType.GAME_RESULT, gameStateJson, clientId));
        } else {
            offlineGameManager.saveGameResult(gameState);
        }
    }

    public void sendGameState(GameStateData gameState) {
        String gameStateJson = gameState.toJson();

        offlineGameManager.addToLeaderboardOnly(gameState);

        if (connected.get()) {
            sendMessage(new NetworkMessage(NetworkMessage.MessageType.GAME_STATE, gameStateJson, clientId));
        } else {
            offlineGameManager.saveGameResult(gameState);
        }
    }

    public void sendUserInput(String input) {
        if (connected.get()) sendMessage(new NetworkMessage(NetworkMessage.MessageType.USER_INPUT, input, clientId));
    }

    public void requestLeaderboard() {
        if (connected.get()) sendMessage(new NetworkMessage(NetworkMessage.MessageType.LEADERBOARD, "", clientId));
        else if (window != null) window.showLeaderboard(offlineGameManager.getLocalLeaderboard());
    }

    public void disconnect() {
        if (connected.compareAndSet(true, false)) {
            try { sendMessage(new NetworkMessage(NetworkMessage.MessageType.DISCONNECT, "Goodbye", clientId)); } catch (Exception ignored) {}
            try {
                if (networkManager != null) networkManager.stop();
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException ignored) {}
            networkExecutor.shutdownNow();
            heartbeatExecutor.shutdownNow();
            if (window != null) window.onNetworkDisconnected();
        }
    }

    public void handleMessage(NetworkMessage message) {
        switch (message.getType()) {
            case CONNECT: handleConnect(message); break;
            case DISCONNECT: handleDisconnect(message); break;
            case GAME_STATE: handleGameState(message); break;
            case USER_INPUT: handleUserInput(message); break;
            case LEADERBOARD: handleLeaderboard(message); break;
            case GAME_RESULT: handleGameResult(message); break;
            case HEARTBEAT: handleHeartbeat(message); break;
            case ERROR: handleError(message); break;
        }
    }

    private void handleConnect(NetworkMessage message) { this.clientId = message.getClientId(); if (window != null) window.setNetworkConnected(true, serverHost + ":" + serverPort, clientId); }
    private void handleDisconnect(NetworkMessage message) { disconnect(); }
    private void handleGameState(NetworkMessage message) {
        try {
            GameStateData gs = GameStateData.fromJson(message.getData());
            if (window != null) window.updateRemoteGameState(gs);
        } catch (Exception ignored) {}
    }
    private void handleUserInput(NetworkMessage message) {}
    private void handleLeaderboard(NetworkMessage message) {
        try {
            LeaderboardEntry[] leaderboard = new Gson().fromJson(message.getData(), LeaderboardEntry[].class);
            List<LeaderboardEntry> list = Arrays.asList(leaderboard);
            offlineGameManager.updateLeaderboard(list);
            if (window != null) window.showLeaderboard(list);
        } catch (Exception e) {
            if (window != null) window.showLeaderboard(offlineGameManager.getLocalLeaderboard());
        }
    }
    private void handleGameResult(NetworkMessage message) {}
    private void handleHeartbeat(NetworkMessage message) {}
    private void handleError(NetworkMessage message) {}

    // Getters
    public boolean isConnected() { return connected.get(); }
    public String getClientId() { return clientId; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public OfflineGameManager getOfflineGameManager() { return offlineGameManager; }
    public NetworkManager getNetworkManager() { return networkManager; }
    public Window getWindow() { return window; }

    public void synchronizePendingGames() {
        if (!isConnected()) return;
        List<GameStateData> pending = offlineGameManager.getPendingGames();
        if (pending == null || pending.isEmpty()) return;

        List<GameStateData> synced = new ArrayList<>();
        for (GameStateData gs : pending) {
            try { sendGameResult(gs); synced.add(gs); } catch (Exception ignored) {}
        }
        offlineGameManager.markGamesAsSynchronized(synced);
    }
}
