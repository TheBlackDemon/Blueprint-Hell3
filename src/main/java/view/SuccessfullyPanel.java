package view;

import client.ClientMain;
import controller.AudioManager;
import controller.User;
import Game.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SuccessfullyPanel extends JPanel implements ActionListener {
    private int level;
    private int packetLoss;
    private int healthyPacket;
    private User user;
    private ClientMain client;
    private boolean onlineMode;
    private JButton back;
    private JButton nextLevel;

    public SuccessfullyPanel(ClientMain client, boolean onlineMode, User user, int packetLoss, int healthyPacket , int level) {
        AudioManager.stopClip();
        this.client = client;
        this.onlineMode = onlineMode;
        this.user = user;
        this.packetLoss = packetLoss;
        this.healthyPacket = healthyPacket;
        this.level = level;
        this.setSize(GameConfig.WIDTH, GameConfig.HEIGHT);
        this.setLayout(null);
        this.setFocusable(true);
        this.requestFocus();
        this.requestFocusInWindow();

        JLabel l = new JLabel("SUCCESSFULLY");
        l.setBounds(300, 10, 200, 80);
        this.add(l);

        back = new JButton("Back to menu");
        back.setFocusable(false);
        back.setBounds(300, 100, 200, 80);
        back.addActionListener(this);
        this.add(back);

        nextLevel = new JButton("next level");
        nextLevel.setFocusable(false);
        nextLevel.setBounds(300, 240, 200, 80);
        nextLevel.addActionListener(this);
        this.add(nextLevel);

        String status = "Healthy packets: " + healthyPacket + "     Packet loss: " + packetLoss + "     Level: " + (level);
        JLabel label = new JLabel(status);
        label.setBounds(100, 380, 540, 80);
        this.add(label);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(back)){
            user.setMaxLevelPass(level);
            MainScreen mainScreen = new MainScreen(user, client, onlineMode);
            Window.getMainFrame().setContentPane(mainScreen);
        } else if (e.getSource().equals(nextLevel)) {
            user.setMaxLevelPass(level);
            if (level == 4){
                JOptionPane.showMessageDialog(
                        null,
                        "Congratulations! You have successfully completed all stages of the game.\nThere are no more levels available.",
                        "Game Completed",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
            else {
                GameInitializer gameInitializer = new GameInitializer();
                gameInitializer.newGame(client , onlineMode, level + 1, user);
            }
        }
    }
}
