package view;

import client.ClientMain;
import network.NetworkMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MultiplayerLobbyPanel extends JPanel {
    private final ClientMain client;
    private final String playerId;
    private final Timer refreshTimer;
    
    private JLabel statusLabel;
    private JList<String> gameList;
    private DefaultListModel<String> gameListModel;
    private JButton createGameButton;
    private JButton joinGameButton;
    private JButton refreshButton;
    private JButton backButton;
    private JLabel playerInfoLabel;
    
    public MultiplayerLobbyPanel(ClientMain client, String playerId) {
        this.client = client;
        this.playerId = playerId;
        this.refreshTimer = new Timer();
        
        initializeUI();
        startRefreshTimer();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(240, 240, 240));
        
        JLabel titleLabel = new JLabel("Multiplayer Lobby");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        playerInfoLabel = new JLabel("Player: " + playerId);
        playerInfoLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        playerInfoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        statusLabel = new JLabel("Connected to server");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setForeground(Color.GREEN);
        
        gameListModel = new DefaultListModel<>();
        gameList = new JList<>(gameListModel);
        gameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gameList.setFont(new Font("Arial", Font.PLAIN, 12));
        gameList.setBorder(BorderFactory.createTitledBorder("Available Games"));
        
        JScrollPane gameListScrollPane = new JScrollPane(gameList);
        gameListScrollPane.setPreferredSize(new Dimension(300, 200));
        
        createGameButton = new JButton("Create New Game");
        createGameButton.setFont(new Font("Arial", Font.BOLD, 14));
        createGameButton.setBackground(Color.GREEN);
        createGameButton.setForeground(Color.WHITE);
        createGameButton.addActionListener(new CreateGameListener());
        
        joinGameButton = new JButton("Join Selected Game");
        joinGameButton.setFont(new Font("Arial", Font.BOLD, 14));
        joinGameButton.setBackground(Color.BLUE);
        joinGameButton.setForeground(Color.WHITE);
        joinGameButton.addActionListener(new JoinGameListener());
        joinGameButton.setEnabled(false);
        
        refreshButton = new JButton("Refresh Games");
        refreshButton.setFont(new Font("Arial", Font.PLAIN, 12));
        refreshButton.addActionListener(new RefreshListener());
        
        backButton = new JButton("Back to Main Menu");
        backButton.setFont(new Font("Arial", Font.PLAIN, 12));
        backButton.addActionListener(new BackListener());
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(titleLabel, BorderLayout.NORTH);
        topPanel.add(playerInfoLabel, BorderLayout.CENTER);
        topPanel.add(statusLabel, BorderLayout.SOUTH);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(gameListScrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(createGameButton);
        buttonPanel.add(joinGameButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(backButton);
        
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        gameList.addListSelectionListener(e -> {
            joinGameButton.setEnabled(!gameList.isSelectionEmpty());
        });
    }
    
    private void startRefreshTimer() {
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> refreshGameList());
            }
        }, 0, 5000); // Refresh every 5 seconds
    }
    
    private void refreshGameList() {
        if (client.isConnected()) {
            client.sendMessage(new NetworkMessage(NetworkMessage.MessageType.GAME_LIST, "", playerId));
        } else {
            statusLabel.setText("Disconnected from server");
            statusLabel.setForeground(Color.RED);
        }
    }
    
    public void updateGameList(List<String> games) {
        SwingUtilities.invokeLater(() -> {
            gameListModel.clear();
            for (String game : games) {
                gameListModel.addElement("Game: " + game);
            }
            statusLabel.setText("Found " + games.size() + " available games");
            statusLabel.setForeground(Color.BLUE);
        });
    }
    
    public void handleGameCreated(String gameId) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, 
                "Game created successfully!\nGame ID: " + gameId + "\nWaiting for opponent...",
                "Game Created", JOptionPane.INFORMATION_MESSAGE);
            
            gameListModel.addElement("Game: " + gameId + " (Your Game)");
        });
    }
    
    public void handleGameJoined(String gameId) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, 
                "Joined game: " + gameId + "\nStarting multiplayer game...",
                "Game Joined", JOptionPane.INFORMATION_MESSAGE);
            
            startMultiplayerGame(gameId);
        });
    }
    
    public void handleGameJoinFailed() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, 
                "Failed to join game. Please try again.",
                "Join Failed", JOptionPane.ERROR_MESSAGE);
        });
    }
    
    private void startMultiplayerGame(String gameId) {

        JOptionPane.showMessageDialog(this, 
            "Multiplayer game would start here with game ID: " + gameId,
            "Multiplayer Game", JOptionPane.INFORMATION_MESSAGE);
    }
    

    private class CreateGameListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (client.isConnected()) {
                client.sendMessage(new NetworkMessage(NetworkMessage.MessageType.CREATE_GAME, "", playerId));
                createGameButton.setEnabled(false);
                statusLabel.setText("Creating game...");
                statusLabel.setForeground(Color.ORANGE);
            } else {
                JOptionPane.showMessageDialog(MultiplayerLobbyPanel.this, 
                    "Not connected to server!", "Connection Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class JoinGameListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String selectedGame = gameList.getSelectedValue();
            if (selectedGame != null && client.isConnected()) {
                String gameId = selectedGame.replace("Game: ", "").replace(" (Your Game)", "");
                client.sendMessage(new NetworkMessage(NetworkMessage.MessageType.JOIN_GAME, gameId, playerId));
                joinGameButton.setEnabled(false);
                statusLabel.setText("Joining game...");
                statusLabel.setForeground(Color.ORANGE);
            }
        }
    }

    private class RefreshListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            refreshGameList();
        }
    }

    private class BackListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {

            JOptionPane.showMessageDialog(MultiplayerLobbyPanel.this, 
                "Returning to main menu...", "Back", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    public void cleanup() {
        if (refreshTimer != null) {
            refreshTimer.cancel();
        }
    }
}
