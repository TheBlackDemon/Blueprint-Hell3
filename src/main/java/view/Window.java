package view;

import controller.AudioManager;
import controller.User;
import Game.GameConfig;
import javax.swing.*;
import network.LeaderboardEntry;
import client.ClientMain;
import network.OfflineGameManager;

public class Window extends JFrame {
    private static Window MainFrame;
    private ClientMain client;
    private User user;
    private MainScreen mainScreen;
    private LeaderboardPanel leaderboardPanel;
    private OfflineGameManager offlineManager;
    private boolean onlineMode;

    public Window(ClientMain client, User user, boolean onlineMode) {
        this.client = client;
        this.user = user;
        this.onlineMode = onlineMode;

        this.offlineManager = new OfflineGameManager();

        try {
            AudioManager.loadSound("themeSong" , "C:\\Users\\asgari\\Desktop\\BlueprintHell\\src\\main\\java\\sounds\\themeSong.wav");
            AudioManager.loadSound("collision" , "C:\\Users\\asgari\\Desktop\\BlueprintHell\\src\\main\\java\\sounds\\collision.wav");
        } catch (Exception e) {
            System.err.println("Audio files not loaded: " + e.getMessage());
        }

        this.setSize(GameConfig.WIDTH, GameConfig.HEIGHT);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setLayout(null);

        mainScreen = new MainScreen(user, client, onlineMode);
        this.setContentPane(mainScreen);

        leaderboardPanel = new LeaderboardPanel(user, client, offlineManager, onlineMode);

        MainFrame = this;
        setVisible(true);
    }

    public static Window getMainFrame() {
        return MainFrame;
    }

    public static void setMainFrame(Window mainFrame) {
        MainFrame = mainFrame;
    }

    public void showLeaderboard(java.util.List<LeaderboardEntry> entries) {
        if (leaderboardPanel == null) {
            leaderboardPanel = new LeaderboardPanel(user , client, offlineManager, onlineMode);
        }
        leaderboardPanel.updateLeaderboard(entries);
        SwingUtilities.invokeLater(() -> {
            this.setContentPane(leaderboardPanel);
            this.revalidate();
            this.repaint();
        });
    }

    public LeaderboardPanel getLeaderboardPanel() {
        if (leaderboardPanel == null) {
            leaderboardPanel = new LeaderboardPanel(user , client, offlineManager, onlineMode);
        }
        return leaderboardPanel;
    }

    public void updateRemoteGameState(network.GameStateData state) {
        if (getContentPane() instanceof GamePanelSupported) {
            ((GamePanelSupported) getContentPane()).applyRemoteGameState(state);
        } else {
            System.out.println("No active game panel to apply remote game state");
        }
    }

    public void setNetworkConnected(boolean connected, String serverInfo, String clientId) {
        if (getContentPane() instanceof MainScreen) {
            ((MainScreen) getContentPane()).onNetworkStatusChanged(connected, serverInfo, clientId);
        }
    }

    public void onNetworkDisconnected() {
        if (getContentPane() instanceof MainScreen) {
            ((MainScreen) getContentPane()).onNetworkStatusChanged(false, "Disconnected", null);
        }
    }

    public void showServerError(String message) {
        JOptionPane.showMessageDialog(this, "Server error: " + message, "Server Error", JOptionPane.ERROR_MESSAGE);
    }

    public interface GamePanelSupported {
        void applyRemoteGameState(network.GameStateData state);
    }
}
