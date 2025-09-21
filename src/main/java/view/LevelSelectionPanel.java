package view;

import Game.GameInitializer;
import Game.LevelConfig;
import Game.GameLogger;
import Game.GameConfig;
import client.ClientMain;
import controller.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class LevelSelectionPanel extends JPanel implements ActionListener {
    
    private User user;
    private ClientMain client;
    private boolean onlineMode;
    private JButton[] levelButtons;
    private JButton backButton;
    private JLabel userInfoLabel;
    private JLabel titleLabel;
    
    public LevelSelectionPanel(ClientMain client , boolean onlineMode, User user) {
        this.client = client;
        this.onlineMode = onlineMode;
        this.user = user;
        initializeComponents();
        setupLayout();
        setupEventListeners();
        
        GameLogger.logUserAction("level_selection_open", "User opened level selection", true);
    }
    
    private void initializeComponents() {
        setSize(GameConfig.WIDTH, GameConfig.HEIGHT);
        setLayout(null);
        setBackground(new Color(240, 240, 240));
        titleLabel = new JLabel("Select Level", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBounds(300, 10, 200, 40);
        userInfoLabel = new JLabel();
        updateUserInfo();
        userInfoLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        userInfoLabel.setBounds(50, 50, 600, 30);
        levelButtons = new JButton[LevelConfig.getMaxLevel()];
        for (int i = 1; i <= LevelConfig.getMaxLevel(); i++) {
            levelButtons[i-1] = new JButton();
            setupLevelButton(i-1, i);
        }
        backButton = new JButton("Back to Main Menu");
        backButton.setFont(new Font("Arial", Font.PLAIN, 14));
        backButton.setBounds(350, 500, 150, 40);
    }
    
    private void setupLevelButton(int index, int levelNumber) {
        LevelConfig.LevelData levelData = LevelConfig.getLevel(levelNumber);
        JButton button = levelButtons[index];
        
        String buttonText = String.format("Level %d: %s", levelNumber, levelData.getLevelName());
        button.setText(buttonText);
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        int x = 100 + (index % 3) * 200;
        int y = 120 + (index / 3) * 80;
        button.setBounds(x, y, 180, 60);
        boolean isUnlocked = levelNumber <= user.getMaxLevelPass() + 1;
        button.setEnabled(isUnlocked);
        
        if (!isUnlocked) {
            button.setBackground(Color.LIGHT_GRAY);
            button.setToolTipText("Complete previous levels to unlock");
        } else {
            button.setBackground(Color.WHITE);
            button.setToolTipText(levelData.getDescription());
        }
        
        button.setFocusable(false);
    }
    
    private void setupLayout() {
        add(titleLabel);
        add(userInfoLabel);
        
        for (JButton button : levelButtons) {
            add(button);
        }
        
        add(backButton);
    }
    
    private void setupEventListeners() {
        for (JButton button : levelButtons) {
            button.addActionListener(this);
        }
        backButton.addActionListener(this);
    }
    
    private void updateUserInfo() {
        userInfoLabel.setText(String.format("User: %s | Coins: %d | Max Level: %d", 
                                           user.getUsername(), user.getCoin(), user.getMaxLevelPass()));
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == backButton) {
            GameLogger.logUserAction("level_selection_back", "User returned to main menu", true);
            Window.getMainFrame().setContentPane(new MainScreen(user, client, onlineMode));
        } else {
            for (int i = 0; i < levelButtons.length; i++) {
                if (e.getSource() == levelButtons[i]) {
                    int levelNumber = i + 1;
                    startLevel(levelNumber);
                    break;
                }
            }
        }
    }
    
    private void startLevel(int levelNumber) {
        LevelConfig.LevelData levelData = LevelConfig.getLevel(levelNumber);
        
        GameLogger.logUserAction("level_start_request", 
                               String.format("User requested to start level %d: %s", 
                                           levelNumber, levelData.getLevelName()), true);
        
        String message = String.format(
            "Level %d: %s\n\n%s\n\nTarget Packets: %d\nMax Packet Loss: %d\nTime Limit: %s",
            levelNumber,
            levelData.getLevelName(),
            levelData.getDescription(),
            levelData.getTargetPackets(),
            levelData.getMaxPacketLoss(),
            levelData.getTimeLimit() == -1 ? "No limit" : levelData.getTimeLimit() + " seconds"
        );
        
        int result = JOptionPane.showConfirmDialog(
            this,
            message,
            "Level Information",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.INFORMATION_MESSAGE
        );
        
        if (result == JOptionPane.OK_OPTION) {
            GameInitializer gameInitializer = new GameInitializer();
            gameInitializer.newGame(client , onlineMode , levelNumber, user);
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        g.setColor(new Color(220, 220, 220));
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 2; j++) {
                int x = 90 + i * 200;
                int y = 110 + j * 80;
                g.drawRect(x, y, 200, 80);
            }
        }
        
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 10));
        for (int i = 1; i <= LevelConfig.getMaxLevel(); i++) {
            LevelConfig.LevelData levelData = LevelConfig.getLevel(i);
            int x = 100 + ((i-1) % 3) * 200;
            int y = 180 + ((i-1) / 3) * 80;
            
            String packetTypes = String.join(", ", levelData.getAvailablePacketTypes());
            g.drawString("Packets: " + packetTypes, x, y);
            
            String systems = String.join(", ", levelData.getAvailableSystems());
            g.drawString("Systems: " + systems, x, y + 12);
        }
    }
}
