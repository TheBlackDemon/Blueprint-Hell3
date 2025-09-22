package view;

import client.ClientMain;
import controller.AudioManager;
import controller.User;
import Game.GameConfig;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameOverPanel extends JPanel implements ActionListener {
    private int packetLoss;
    private int healthyPacket;
    private User user;
    private JButton back;
    private ClientMain client;
    private boolean onlineMode;

    public GameOverPanel(ClientMain client, boolean onlineMode, User user, int packetLoss, int healthyPacket) {
        AudioManager.stopClip();
        this.client = client;
        this.onlineMode = onlineMode;
        this.user = user;
        this.packetLoss = packetLoss;
        this.healthyPacket = healthyPacket;
        this.setSize(GameConfig.WIDTH, GameConfig.HEIGHT);
        this.setLayout(null);
        this.setFocusable(true);
        this.requestFocus();
        this.requestFocusInWindow();

        JLabel l = new JLabel("GAME OVER");
        l.setBounds(300, 10, 200, 80);
        this.add(l);

        back = new JButton("Back to menu");
        back.setFocusable(false);
        back.setBounds(300, 100, 200, 80);
        back.addActionListener(this);
        this.add(back);

        String status = "Healthy packets: " + healthyPacket + "   \nPacket loss: " + packetLoss;
        JLabel label = new JLabel(status);
        label.setBounds(300, 250, 200, 80);
        this.add(label);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(back)){
            MainScreen mainScreen = new MainScreen(user, client, onlineMode);
            Window.getMainFrame().setContentPane(mainScreen);
        }

    }
}
