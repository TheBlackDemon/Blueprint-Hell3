package view;

import client.ClientMain;
import controller.User;
import Game.*;
import network.OfflineGameManager;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainScreen extends JPanel implements ActionListener, NetworkStatusPanel.NetworkStatusListener {
    private User user;
    private JButton multiPlayer;
    private JButton start;
    private JButton setting;
    private JButton exit;
    private JButton levels;
    private JButton leaderboard;
    private JButton shop;
    private NetworkStatusPanel networkPanel;
    private OfflineGameManager offlineManager;
    private ClientMain client;
    private boolean isOnline = false;

    public MainScreen(User user, ClientMain client, boolean initialOnline) {
        this.user = user;
        this.client = client;
        this.offlineManager = client.getOfflineGameManager();
        this.isOnline = initialOnline;

        this.setSize(GameConfig.WIDTH, GameConfig.HEIGHT);
        this.setLayout(null);
        this.setFocusable(true);
        this.requestFocus();
        this.requestFocusInWindow();

        networkPanel = new NetworkStatusPanel();
        networkPanel.setBounds(10, 10, 780, 100);
        networkPanel.setNetworkStatusListener(this);
        this.add(networkPanel);

        start = new JButton("Start Last Game");
        start.setFocusable(false);
        start.setBounds(300, 130, 200, 40);
        start.addActionListener(this);
        this.add(start);

        levels = new JButton("Levels");
        levels.setFocusable(false);
        levels.setBounds(300, 185, 200, 40);
        levels.addActionListener(this);
        this.add(levels);

        leaderboard = new JButton("Leaderboard");
        leaderboard.setFocusable(false);
        leaderboard.setBounds(300, 240, 200, 40);
        leaderboard.addActionListener(this);
        this.add(leaderboard);

        shop = new JButton("Shop");
        shop.setFocusable(false);
        shop.setBounds(300, 295, 200, 40);
        shop.addActionListener(this);
        this.add(shop);

        setting = new JButton("Setting");
        setting.setFocusable(false);
        setting.setBounds(300, 350, 200, 40);
        setting.addActionListener(this);
        this.add(setting);

        multiPlayer = new JButton("Start Last Game");
        multiPlayer.setFocusable(false);
        multiPlayer.setBounds(300, 405, 200, 40);
        multiPlayer.addActionListener(this);
        this.add(multiPlayer);

        exit = new JButton("Exit");
        exit.setFocusable(false);
        exit.setBounds(300, 460, 200, 40);
        exit.addActionListener(this);
        this.add(exit);

        networkPanel.setConnectionStatus(isOnline, isOnline ? "Connected" : "Disconnected");

        if (isOnline && client != null && client.getClientId() != null) {
            networkPanel.setServerInfo(client.getClientId());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(start)){
            GameInitializer initializer = new GameInitializer();
            if (user.getMaxLevelPass() == 0){
                // Start from level 1
                initializer.newGame(client , isOnline, 1, user);
            } else {
                initializer.newGame(client , isOnline , user.getMaxLevelPass(), user);
            }
        }
        else if (e.getSource().equals(multiPlayer)) {
            MultiplayerLobbyPanel multiplayerLobbyPanel = new MultiplayerLobbyPanel(client , client.getClientId());
            Window.getMainFrame().setContentPane(multiplayerLobbyPanel);
            Window.getMainFrame().revalidate();
        } else if (e.getSource().equals(levels)) {
            LevelSelectionPanel levelPanel = new LevelSelectionPanel(client , isOnline , user);
            Window.getMainFrame().setContentPane(levelPanel);
            Window.getMainFrame().revalidate();
        } else if (e.getSource().equals(leaderboard)) {
            showLeaderboard();
        } else if (e.getSource().equals(shop)) {
            if (Window.getMainFrame().getContentPane() instanceof GamePanel) {
                GamePanel currentGame = (GamePanel) Window.getMainFrame().getContentPane();
                ShopPanel shopPanel = new ShopPanel(client , isOnline , user, currentGame);
                Window.getMainFrame().setContentPane(shopPanel);
                Window.getMainFrame().revalidate();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Start a game first before opening the shop.",
                        "No Active Game",
                        JOptionPane.WARNING_MESSAGE);
            }
        } else if (e.getSource().equals(setting)) {
            VolumeSliderPanel settingPanel = new VolumeSliderPanel(client , isOnline , user);
            Window.getMainFrame().setContentPane(settingPanel);
            Window.getMainFrame().revalidate();
        } else if (e.getSource().equals(exit)) {
            if (client != null && client.isConnected()) {
                client.disconnect();
            }
            System.exit(0);
        }
    }

    private void showLeaderboard() {
        LeaderboardPanel leaderboardPanel = Window.getMainFrame().getLeaderboardPanel();

        if (isOnline && client != null && client.isConnected()) {
            client.requestLeaderboard();
        } else {
            leaderboardPanel.updateLeaderboard(offlineManager.getLocalLeaderboard());
            Window.getMainFrame().setContentPane(leaderboardPanel);
            Window.getMainFrame().revalidate();
        }
    }

    @Override
    public void onConnect(String host, int port) {
        networkPanel.showConnectionProgress(true);
        networkPanel.setConnectionStatus(false, "Connecting...");

        new Thread(() -> {
            try {
                ClientMain newClient = new ClientMain(host, port);
                boolean connected = newClient.connectToServerForUI();
                if (connected) {
                    this.client = newClient;
                    isOnline = true;
                    SwingUtilities.invokeLater(() -> {
                        networkPanel.setConnectionStatus(true, "Connected to " + host + ":" + port);
                        networkPanel.setServerInfo(host + ":" + port);
                        networkPanel.showConnectionProgress(false);
                        Window.getMainFrame().revalidate();
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        networkPanel.setConnectionStatus(false, "Connection failed");
                        networkPanel.showConnectionProgress(false);
                    });
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    networkPanel.showError("Connection failed: " + e.getMessage());
                    networkPanel.showConnectionProgress(false);
                });
            }
        }).start();
    }

    @Override
    public void onDisconnect() {
        if (client != null) {
            client.disconnect();
        }
        isOnline = false;
        networkPanel.setConnectionStatus(false, "Disconnected");
    }

    @Override
    public void onRetry() {
        String host = networkPanel.getServerAddress();
        int port = networkPanel.getServerPort();
        onConnect(host, port);
    }

    public void onNetworkStatusChanged(boolean connected, String serverInfo, String clientId) {
        this.isOnline = connected;
        networkPanel.setConnectionStatus(connected, connected ? ("Connected to " + serverInfo) : "Disconnected");
        if (connected) {
            networkPanel.setServerInfo(serverInfo != null ? serverInfo : clientId);
            if (client != null) client.synchronizePendingGames();
        }
    }
}
