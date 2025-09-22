package view;

import Game.*;
import client.ClientMain;
import network.GameStateData;
import network.MultiplayerGameState;
import network.NetworkMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MultiplayerGamePanel extends GamePanel {
    private final MultiplayerGameState gameState;
    private final ClientMain client;
    private final String playerId;
    private final String opponentId;
    private final Timer gameTimer;
    private final Timer networkSetupTimer;
    
    private JLabel setupTimeLabel;
    private JLabel playerScoreLabel;
    private JLabel opponentScoreLabel;
    private JButton readyButton;
    private JLabel statusLabel;
    private JPanel infoPanel;
    
    private boolean isReady;
    private long networkSetupStartTime;
    private boolean isExtendedTime;
    private boolean wrathEffectsActive;
    
    private AmmunitionPanel currentAmmunitionPanel;
    private ControllableReferenceSystem hoveredSystem;
    private boolean showOpponentNetwork;
    private boolean temporalProgressActive;
    private long temporalProgressStartTime;
    private ClientMain clientMain;
    private boolean onlineMod;
    
    public MultiplayerGamePanel(ClientMain clientMain , boolean onlineMod , GameState state, IWireLengthManager wireLengthManager,
                               IPacketManager packetManager, IConnectionValidator validator,
                               MultiplayerGameState gameState, ClientMain client, String playerId) {
        super(clientMain , onlineMod , state, wireLengthManager, packetManager, validator);
        this.clientMain = clientMain;
        this.onlineMod = onlineMod;
        this.gameState = gameState;
        this.client = client;
        this.playerId = playerId;
        this.opponentId = getOpponentId();
        this.gameTimer = new Timer();
        this.networkSetupTimer = new Timer();
        this.isReady = false;
        this.networkSetupStartTime = System.currentTimeMillis();
        this.isExtendedTime = false;
        this.wrathEffectsActive = false;
        this.currentAmmunitionPanel = null;
        this.hoveredSystem = null;
        this.showOpponentNetwork = false;
        this.temporalProgressActive = false;
        this.temporalProgressStartTime = 0;
        
        initializeMultiplayerUI();
        initializeControllableSystems();
        addMouseListener(new ControllableSystemMouseListener());
        startNetworkSetupTimer();
    }
    
    private void initializeMultiplayerUI() {
        infoPanel = new JPanel();
        infoPanel.setLayout(new BorderLayout());
        infoPanel.setBackground(new Color(240, 240, 240));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Multiplayer Game"));
        
        setupTimeLabel = new JLabel("Network Setup Time: 30s");
        setupTimeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        setupTimeLabel.setForeground(Color.BLUE);
        
        playerScoreLabel = new JLabel("Your Score: 0");
        playerScoreLabel.setFont(new Font("Arial", Font.BOLD, 14));
        playerScoreLabel.setForeground(gameState.getPlayerColor(playerId));
        
        opponentScoreLabel = new JLabel("Opponent Score: 0");
        opponentScoreLabel.setFont(new Font("Arial", Font.BOLD, 14));
        opponentScoreLabel.setForeground(gameState.getPlayerColor(opponentId));
        
        readyButton = new JButton("Ready to Start");
        readyButton.setFont(new Font("Arial", Font.BOLD, 14));
        readyButton.setBackground(Color.GREEN);
        readyButton.setForeground(Color.WHITE);
        readyButton.addActionListener(new ReadyButtonListener());
        
        statusLabel = new JLabel("Setting up your network...");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        statusLabel.setForeground(Color.DARK_GRAY);
        
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(setupTimeLabel);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(readyButton);
        
        JPanel scorePanel = new JPanel(new FlowLayout());
        scorePanel.add(playerScoreLabel);
        scorePanel.add(Box.createHorizontalStrut(20));
        scorePanel.add(opponentScoreLabel);
        
        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(statusLabel);
        
        infoPanel.add(topPanel, BorderLayout.NORTH);
        infoPanel.add(scorePanel, BorderLayout.CENTER);
        infoPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        this.add(infoPanel);
        infoPanel.setBounds(10, 10, 400, 120);
    }
    
    private void initializeControllableSystems() {
        createControllableSystems();
        
        gameState.registerPlayerGameState(playerId, getState());
        
        JButton temporalProgressButton = new JButton("Temporal Progress");
        temporalProgressButton.addActionListener(e -> activateTemporalProgress());
        infoPanel.add(temporalProgressButton);
    }
    
    private void createControllableSystems() {
        ControllableReferenceSystem player1System1 = new ControllableReferenceSystem(
            "player1_system1", 100, 200, "player1", "player2");
        ControllableReferenceSystem player1System2 = new ControllableReferenceSystem(
            "player1_system2", 200, 300, "player1", "player2");
        
        ControllableReferenceSystem player2System1 = new ControllableReferenceSystem(
            "player2_system1", 600, 200, "player2", "player1");
        ControllableReferenceSystem player2System2 = new ControllableReferenceSystem(
            "player2_system2", 700, 300, "player2", "player1");
        
        gameState.addControllableSystem(player1System1);
        gameState.addControllableSystem(player1System2);
        gameState.addControllableSystem(player2System1);
        gameState.addControllableSystem(player2System2);
        
        createUncontrollableSystems();
    }
    
    private void createUncontrollableSystems() {
        Node player1Uncontrollable1 = new Node(150, 150, "player1_uncontrollable1",
            new String[]{"square", "triangle"}, new String[]{"square", "triangle"}, 
            GameConfig.UNCONTROLLABLE_SYSTEM_TYPE);
        Node player1Uncontrollable2 = new Node(250, 250, "player1_uncontrollable2", 
            new String[]{"circle"}, new String[]{"circle"}, 
            GameConfig.UNCONTROLLABLE_SYSTEM_TYPE);
        
        Node player2Uncontrollable1 = new Node(650, 150, "player2_uncontrollable1",
            new String[]{"confidential_4", "confidential_6"}, new String[]{"confidential_4", "confidential_6"}, 
            GameConfig.UNCONTROLLABLE_SYSTEM_TYPE);
        Node player2Uncontrollable2 = new Node(750, 250, "player2_uncontrollable2", 
            new String[]{"bulky_8", "bulky_10"}, new String[]{"bulky_8", "bulky_10"}, 
            GameConfig.UNCONTROLLABLE_SYSTEM_TYPE);
        
        gameState.addUncontrollableSystem("player1", player1Uncontrollable1);
        gameState.addUncontrollableSystem("player1", player1Uncontrollable2);
        gameState.addUncontrollableSystem("player2", player2Uncontrollable1);
        gameState.addUncontrollableSystem("player2", player2Uncontrollable2);
    }
    
    private void activateTemporalProgress() {
        if (temporalProgressActive) return;
        
        temporalProgressActive = true;
        temporalProgressStartTime = System.currentTimeMillis();
        showOpponentNetwork = true;
        
        Timer temporalTimer = new Timer();
        temporalTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    temporalProgressActive = false;
                    showOpponentNetwork = false;
                    repaint();
                });
            }
        }, GameConfig.TEMPORAL_PROGRESS_DURATION_MS);
        
        repaint();
    }
    
    private String getOpponentId() {
        for (String id : gameState.getAllPlayers().keySet()) {
            if (!id.equals(playerId)) {
                return id;
            }
        }
        return "unknown";
    }
    
    private void startNetworkSetupTimer() {
        networkSetupTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> updateNetworkSetupTimer());
            }
        }, 0, 100); // Update every 100ms
    }
    
    private void updateNetworkSetupTimer() {
        long remainingTime = gameState.getRemainingSetupTime();
        
        if (remainingTime <= 0) {
            if (!isExtendedTime) {
                startExtendedTime();
            } else {
                if (!gameState.isGameStarted()) {
                    startGameWithPenalties();
                }
            }
        } else {
            long seconds = remainingTime / 1000;
            long milliseconds = (remainingTime % 1000) / 100;
            
            if (isExtendedTime) {
                setupTimeLabel.setText("Extended Time: " + seconds + "." + milliseconds + "s");
                setupTimeLabel.setForeground(Color.RED);
                statusLabel.setText("Extended time - Wrath effects active!");
                statusLabel.setForeground(Color.RED);
            } else {
                setupTimeLabel.setText("Network Setup: " + seconds + "." + milliseconds + "s");
                setupTimeLabel.setForeground(Color.BLUE);
                statusLabel.setText("Setting up your network...");
                statusLabel.setForeground(Color.DARK_GRAY);
            }
        }
    }
    
    private void startExtendedTime() {
        isExtendedTime = true;
        wrathEffectsActive = true;
        readyButton.setEnabled(false);
        readyButton.setText("Extended Time");
        readyButton.setBackground(Color.RED);
        
        applyWrathEffects();
        
        JOptionPane.showMessageDialog(this, 
            "Network setup time expired!\n" +
            "Extended time started with penalties:\n" +
            "• Wrath of Penia: Random packets added to opponent\n" +
            "• Wrath of Aergia: Increased cooldowns\n" +
            "• Wrath of Penia Speed: Increased packet speed",
            "Extended Time Started", JOptionPane.WARNING_MESSAGE);
    }
    
    private void startGameWithPenalties() {
        if (!gameState.isGameStarted()) {
            gameState.setPlayerReady(playerId, true);
            if (gameState.allPlayersReady()) {
                gameState.startGame();
                startGameTimer();
            }
        }
    }
    
    private void applyWrathEffects() {
        long extendedElapsed = System.currentTimeMillis() - networkSetupStartTime - 30000;
        
        if (extendedElapsed <= 10000) {
            if (extendedElapsed % 2000 < 100) {
                addRandomPacketToOpponent();
            }
        } else if (extendedElapsed <= 20000) {
            double cooldownMultiplier = 1.0 + ((extendedElapsed - 10000) / 1000.0) * 0.01;
            applyCooldownMultiplier(cooldownMultiplier);
        } else if (extendedElapsed <= 30000) {
            double speedMultiplier = 1.0 + ((extendedElapsed - 20000) / 1000.0) * 0.03;
            applySpeedMultiplier(speedMultiplier);
        }
    }
    
    private void addRandomPacketToOpponent() {
        NetworkMessage message = new NetworkMessage(
            NetworkMessage.MessageType.WRATH_EFFECT,
            "add_random_packet",
            playerId
        );
        client.sendMessage(message);
    }
    
    private void applyCooldownMultiplier(double multiplier) {

        System.out.println("Applying cooldown multiplier: " + multiplier);
    }
    
    private void applySpeedMultiplier(double multiplier) {
        for (Packet packet : getState().getPackets()) {
            packet.setCurrentSpeed(packet.getCurrentSpeed() * multiplier);
        }
        System.out.println("Applying speed multiplier: " + multiplier);
    }
    
    private void startGameTimer() {
        gameTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> updateGameState());
            }
        }, 0, 1000); // Update every second
    }
    
    private void updateGameState() {
        if (gameState.isGameStarted()) {
            gameState.updateControllableSystems();

            updateScores();
            
            sendGameStateUpdate();
            
            checkGameEndConditions();
        }
    }
    
    private void updateScores() {
        int playerScore = getState().getPackets().size() - getState().getPacketLoss();
        int opponentScore = getOpponentScore();
        
        playerScoreLabel.setText("Your Score: " + playerScore);
        opponentScoreLabel.setText("Opponent Score: " + opponentScore);
        
        gameState.updatePacketCount(playerId, getState().getPackets().size());
        gameState.updatePacketLoss(playerId, getState().getPacketLoss());
    }
    
    private int getOpponentScore() {
        return 0;
    }
    
    private void sendGameStateUpdate() {
        GameStateData gameStateData = convertToGameStateData();
        client.sendGameState(gameStateData);
    }
    
    private GameStateData convertToGameStateData() {
        GameStateData data = new GameStateData();
        data.setLevel(getState().getLevel());
        data.setGameOver(getState().isGameOver());
        data.setSuccessfully(getState().isSuccessfully());
        data.setPacketLoss(getState().getPacketLoss());
        data.setLevelStartTime(getState().getLevelStartTime());
        data.setUsername(getState().getUser().getUsername());
        data.setCoins(getState().getUser().getCoin());
        data.setMaxLevelPass(getState().getUser().getMaxLevelPass());
        

        return data;
    }
    
    private void checkGameEndConditions() {
        if (getState().isGameOver() || getState().isSuccessfully()) {
            endGame();
        }
    }
    
    private void endGame() {
        gameTimer.cancel();
        networkSetupTimer.cancel();
        
        sendGameResult();
        
        showGameOverScreen();
    }
    
    private void sendGameResult() {
        GameStateData finalState = convertToGameStateData();
        client.sendGameResult(finalState);
    }
    
    private void showGameOverScreen() {
        MultiplayerGameState.GameResult result = gameState.getGameResult();
        String message;
        
        if (result != null && playerId.equals(result.getWinner())) {
            message = "Congratulations! You won the multiplayer game!";
        } else if (result != null) {
            message = "Game Over! " + result.getWinner() + " won the game.";
        } else {
            message = "Game Over!";
        }
        
        JOptionPane.showMessageDialog(this, message, "Game Over", JOptionPane.INFORMATION_MESSAGE);
        

    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        

        if (showOpponentNetwork) {
            drawOpponentNetwork(g);
        }

        drawPlayerPackets(g);

        drawControllableSystems(g);

        if (currentAmmunitionPanel != null && hoveredSystem != null) {
            drawAmmunitionPanel(g);
        }

        if (gameState.isNetworkSetupPhase()) {
            drawNetworkSetupOverlay(g);
        }

        if (wrathEffectsActive) {
            drawWrathEffects(g);
        }

        if (temporalProgressActive) {
            drawTemporalProgressIndicator(g);
        }
    }
    
    private void drawPlayerPackets(Graphics g) {
        Color playerColor = gameState.getPlayerColor(playerId);
        Color opponentColor = gameState.getPlayerColor(opponentId);
        
        for (Packet packet : getState().getPackets()) {
            Color packetColor = determinePacketColor(packet, playerColor, opponentColor);
            
            g.setColor(packetColor);
            Point pos = packet.getPosition();
            g.fillOval(pos.x - 5, pos.y - 5, 10, 10);
        }
    }
    
    private Color determinePacketColor(Packet packet, Color playerColor, Color opponentColor) {

        return playerColor;
    }
    
    private void drawNetworkSetupOverlay(Graphics g) {
        g.setColor(new Color(0, 0, 0, 100));
        g.fillRect(0, 0, getWidth(), getHeight());
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        String text = "Network Setup Phase";
        FontMetrics fm = g.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        int y = getHeight() / 2;
        g.drawString(text, x, y);
    }
    
    private void drawWrathEffects(Graphics g) {
        g.setColor(new Color(255, 0, 0, 50));
        g.fillRect(0, 0, getWidth(), getHeight());
        
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("WRATH EFFECTS ACTIVE", 10, 30);
    }
    
    private void drawOpponentNetwork(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) GameConfig.OPPONENT_NETWORK_ALPHA));
        
        List<INode> opponentSystems = gameState.getUncontrollableSystemsForPlayer(opponentId);
        for (INode system : opponentSystems) {
            drawSystem(g2d, system, Color.GRAY);
        }
        
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }
    
    private void drawControllableSystems(Graphics g) {
        List<ControllableReferenceSystem> systems = gameState.getControllableSystemsForPlayer(playerId);
        for (ControllableReferenceSystem system : systems) {
            drawControllableSystem(g, system);
        }
    }
    
    private void drawControllableSystem(Graphics g, ControllableReferenceSystem system) {
        int x = system.getX();
        int y = system.getY();
        int size = GameConfig.NODE_SIZE;
        

        if (system.isActive()) {
            g.setColor(Color.GREEN);
        } else {
            g.setColor(Color.RED);
        }
        g.fillOval(x - size/2, y - size/2, size, size);
        
        g.setColor(Color.BLACK);
        g.drawOval(x - size/2, y - size/2, size, size);
        
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 10));
        FontMetrics fm = g.getFontMetrics();
        String id = system.getId();
        int textX = x - fm.stringWidth(id) / 2;
        int textY = y + fm.getHeight() / 4;
        g.drawString(id, textX, textY);
        
        if (!system.getAmmunitionManager().canUseSystem(system.getId())) {
            long remaining = system.getAmmunitionManager().getSystemCooldownRemaining(system.getId());
            int seconds = (int) (remaining / 1000);
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 8));
            g.drawString(seconds + "s", x - 10, y - size/2 - 5);
        }
    }
    
    private void drawSystem(Graphics g, INode system, Color color) {
        int x = system.getX();
        int y = system.getY();
        int size = GameConfig.NODE_SIZE;
        
        g.setColor(color);
        g.fillOval(x - size/2, y - size/2, size, size);
        
        g.setColor(Color.BLACK);
        g.drawOval(x - size/2, y - size/2, size, size);
    }
    
    private void drawAmmunitionPanel(Graphics g) {
        if (currentAmmunitionPanel == null || hoveredSystem == null) return;
        
        Point hoverPos = hoveredSystem.getHoverPosition();
        int panelX = hoverPos.x + 20;
        int panelY = hoverPos.y - GameConfig.AMMUNITION_PANEL_HEIGHT / 2;
        
        if (panelX + GameConfig.AMMUNITION_PANEL_WIDTH > getWidth()) {
            panelX = hoverPos.x - GameConfig.AMMUNITION_PANEL_WIDTH - 20;
        }
        if (panelY < 0) {
            panelY = 10;
        }
        
        currentAmmunitionPanel.setBounds(panelX, panelY, GameConfig.AMMUNITION_PANEL_WIDTH, GameConfig.AMMUNITION_PANEL_HEIGHT);
        currentAmmunitionPanel.paint(g.create(panelX, panelY, GameConfig.AMMUNITION_PANEL_WIDTH, GameConfig.AMMUNITION_PANEL_HEIGHT));
    }
    
    private void drawTemporalProgressIndicator(Graphics g) {
        g.setColor(new Color(0, 255, 255, 100));
        g.fillRect(0, 0, getWidth(), getHeight());
        
        g.setColor(Color.CYAN);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("TEMPORAL PROGRESS ACTIVE", 10, 30);
        
        long remaining = GameConfig.TEMPORAL_PROGRESS_DURATION_MS - (System.currentTimeMillis() - temporalProgressStartTime);
        int seconds = (int) (remaining / 1000);
        g.drawString("Time remaining: " + seconds + "s", 10, 50);
    }
    

    private class ReadyButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!isReady) {
                isReady = true;
                readyButton.setText("Ready!");
                readyButton.setBackground(Color.GREEN);
                readyButton.setEnabled(false);
                
                NetworkMessage message = new NetworkMessage(
                    NetworkMessage.MessageType.PLAYER_READY,
                    "ready",
                    playerId
                );
                client.sendMessage(message);
                
                statusLabel.setText("Waiting for opponent...");
                statusLabel.setForeground(Color.ORANGE);
            }
        }
    }

    private class ControllableSystemMouseListener extends MouseAdapter {
        @Override
        public void mouseMoved(MouseEvent e) {
            List<ControllableReferenceSystem> systems = gameState.getControllableSystemsForPlayer(playerId);
            ControllableReferenceSystem hovered = null;
            
            for (ControllableReferenceSystem system : systems) {
                if (system.isOverSystem(e.getX(), e.getY())) {
                    hovered = system;
                    break;
                }
            }
            
            if (hovered != hoveredSystem) {
                hoveredSystem = hovered;
                
                if (hoveredSystem != null) {
                    currentAmmunitionPanel = new AmmunitionPanel(hoveredSystem, new AmmunitionPanel.AmmunitionSelectionListener() {
                        @Override
                        public void onAmmunitionSelected(AmmunitionType type, ControllableReferenceSystem system) {
                            boolean success = gameState.firePacketFromSystem(system.getId(), type, playerId);
                            if (success) {
                                System.out.println("Fired " + type.getDisplayName() + " from " + system.getId());
                                currentAmmunitionPanel.updateAmmunitionStatuses();
                            }
                        }
                    });
                    hoveredSystem.setHovered(true, e.getPoint());
                } else {
                    currentAmmunitionPanel = null;
                }
                
                repaint();
            }
        }
        
        @Override
        public void mouseExited(MouseEvent e) {
            if (hoveredSystem != null) {
                hoveredSystem.setHovered(false, null);
                hoveredSystem = null;
                currentAmmunitionPanel = null;
                repaint();
            }
        }
    }
}
