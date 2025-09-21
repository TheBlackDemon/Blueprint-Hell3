package view;

import client.ClientMain;
import controller.AudioManager;
import controller.User;
import Game.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ShopPanel extends JPanel implements ActionListener {
    private JButton atar;
    private JButton airyaman;
    private JButton anahita;
    private JButton speedBooster;
    private JButton speedLimiter;
    private JButton wireOptimizer;
    private JButton scrollAergia;
    private JButton scrollSisyphus;
    private JButton scrollEliphas;
    private JButton back;
    private User user;
    private ClientMain client;
    private boolean onlineMode;
    private GamePanel gamePanel;
    public ShopPanel(ClientMain client, boolean onlineMode, User user , GamePanel gamePanel ){
        AudioManager.pauseClip();
        this.setSize(GameConfig.WIDTH, GameConfig.HEIGHT);
        this.setLayout(null);
        this.setFocusable(true);
        this.requestFocus();
        this.requestFocusInWindow();
        this.client = client;
        this.onlineMode = onlineMode;
        this.user = user;
        this.gamePanel = gamePanel;
        atar = new JButton("Atar");
        atar.setFocusable(false);
        atar.setBounds(300, 100, 200, 80);
        atar.addActionListener(this);
        this.add(atar);

        airyaman = new JButton("Airyaman");
        airyaman.setFocusable(false);
        airyaman.setBounds(300, 200, 200, 80);
        airyaman.addActionListener(this);
        this.add(airyaman);

        anahita = new JButton("Anahita");
        anahita.setFocusable(false);
        anahita.setBounds(300, 300, 200, 80);
        anahita.addActionListener(this);
        this.add(anahita);

        speedBooster = new JButton("Speed Booster");
        speedBooster.setFocusable(false);
        speedBooster.setBounds(50, 100, 200, 60);
        speedBooster.addActionListener(this);
        this.add(speedBooster);

        speedLimiter = new JButton("Speed Limiter");
        speedLimiter.setFocusable(false);
        speedLimiter.setBounds(50, 180, 200, 60);
        speedLimiter.addActionListener(this);
        this.add(speedLimiter);

        wireOptimizer = new JButton("Wire Optimizer");
        wireOptimizer.setFocusable(false);
        wireOptimizer.setBounds(50, 260, 200, 60);
        wireOptimizer.addActionListener(this);
        this.add(wireOptimizer);

        scrollAergia = new JButton("Scroll of Aergia");
        scrollAergia.setFocusable(false);
        scrollAergia.setBounds(50, 340, 200, 60);
        scrollAergia.addActionListener(this);
        this.add(scrollAergia);

        scrollSisyphus = new JButton("Scroll of Sisyphus");
        scrollSisyphus.setFocusable(false);
        scrollSisyphus.setBounds(50, 420, 200, 60);
        scrollSisyphus.addActionListener(this);
        this.add(scrollSisyphus);

        scrollEliphas = new JButton("Scroll of Eliphas");
        scrollEliphas.setFocusable(false);
        scrollEliphas.setBounds(50, 500, 200, 60);
        scrollEliphas.addActionListener(this);
        this.add(scrollEliphas);

        back = new JButton("Back");
        back.setFocusable(false);
        back.setBounds(300, 400, 200, 80);
        back.addActionListener(this);
        this.add(back);

    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(atar)){
            if ( user.getCoin() >= 3) {
                if (!gamePanel.getState().isClickAtar()) {
                    user.setCoin(user.getCoin() - 3);
                    gamePanel.getState().setClickAtar(true);
                    GameLogger.logShopPurchase("Atar", 3, user.getCoin() + 3, true);
                    repaint();
                } else {
                    JOptionPane.showMessageDialog(
                            null,
                            "Atar is already purchased!",
                            "Already Owned",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
            }else {
                GameLogger.logShopPurchase("Atar", 3, user.getCoin(), false);
                JOptionPane.showMessageDialog(
                        null,
                        "You don't have enough coins!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        } else if (e.getSource().equals(airyaman)){
            if ( user.getCoin() >= 4) {
                if (!gamePanel.getState().isClickAiryaman()) {
                    user.setCoin(user.getCoin() - 4);
                    gamePanel.getState().setClickAiryaman(true);
                    GameLogger.logShopPurchase("Airyaman", 4, user.getCoin() + 4, true);
                    repaint();
                }
            }else {
                JOptionPane.showMessageDialog(
                        null,
                        "You don't have enough coins!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        } else if (e.getSource().equals(anahita)) {
            if (user.getCoin() >= 5) {
                if (!gamePanel.getState().isClickAnahita() ) {
                    user.setCoin(user.getCoin() - 5);
                    gamePanel.getState().setClickAnahita(true);
                    GameLogger.logShopPurchase("Anahita", 5, user.getCoin() + 5, true);
                    repaint();
                }
            }else {
                GameLogger.logShopPurchase("Anahita", 5, user.getCoin(), false);
                JOptionPane.showMessageDialog(
                        null,
                        "You don't have enough coins!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        } else if (e.getSource().equals(speedBooster)) {
            if (user.getCoin() >= 6) {
                if (!gamePanel.getState().isClickSpeedBooster()) {
                    user.setCoin(user.getCoin() - 6);
                    gamePanel.getState().setClickSpeedBooster(true);
                    GameLogger.logShopPurchase("Speed Booster", 6, user.getCoin() + 6, true);
                    repaint();
                }
            } else {
                GameLogger.logShopPurchase("Speed Booster", 6, user.getCoin(), false);
                JOptionPane.showMessageDialog(
                        null,
                        "You don't have enough coins!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        } else if (e.getSource().equals(speedLimiter)) {
            if (user.getCoin() >= 8) {
                if (!gamePanel.getState().isClickSpeedLimiter()) {
                    user.setCoin(user.getCoin() - 8);
                    gamePanel.getState().setClickSpeedLimiter(true);
                    GameLogger.logShopPurchase("Speed Limiter", 8, user.getCoin() + 8, true);
                    repaint();
                }
            } else {
                GameLogger.logShopPurchase("Speed Limiter", 8, user.getCoin(), false);
                JOptionPane.showMessageDialog(
                        null,
                        "You don't have enough coins!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        } else if (e.getSource().equals(wireOptimizer)) {
            if (user.getCoin() >= 10) {
                if (!gamePanel.getState().isClickWireOptimizer()) {
                    user.setCoin(user.getCoin() - 10);
                    gamePanel.getState().setClickWireOptimizer(true);
                    GameLogger.logShopPurchase("Wire Optimizer", 10, user.getCoin() + 10, true);
                    repaint();
                }
            } else {
                GameLogger.logShopPurchase("Wire Optimizer", 10, user.getCoin(), false);
                JOptionPane.showMessageDialog(
                        null,
                        "You don't have enough coins!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        } else if (e.getSource().equals(scrollAergia)) {
            if (user.getCoin() >= GameConfig.SCROLL_AERGIA_COST) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - gamePanel.getState().getLastScrollAergiaUse() >= GameConfig.SCROLL_AERGIA_COOLDOWN_MS) {
                    user.setCoin(user.getCoin() - GameConfig.SCROLL_AERGIA_COST);
                    gamePanel.getState().setClickScrollAergia(true);
                    gamePanel.getState().setLastScrollAergiaUse(currentTime);
                    gamePanel.setScrollAergiaMode(true);
                    GameLogger.logShopPurchase("Scroll of Aergia", GameConfig.SCROLL_AERGIA_COST, user.getCoin() + GameConfig.SCROLL_AERGIA_COST, true);
                    repaint();
                } else {
                    long remainingTime = (GameConfig.SCROLL_AERGIA_COOLDOWN_MS - (currentTime - gamePanel.getState().getLastScrollAergiaUse())) / 1000;
                    GameLogger.logShopPurchase("Scroll of Aergia", GameConfig.SCROLL_AERGIA_COST, user.getCoin(), false);
                    JOptionPane.showMessageDialog(
                            null,
                            "Scroll of Aergia is on cooldown! " + remainingTime + " seconds remaining.",
                            "Cooldown",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            } else {
                GameLogger.logShopPurchase("Scroll of Aergia", GameConfig.SCROLL_AERGIA_COST, user.getCoin(), false);
                JOptionPane.showMessageDialog(
                        null,
                        "You don't have enough coins! Need " + GameConfig.SCROLL_AERGIA_COST + " coins.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        } else if (e.getSource().equals(scrollSisyphus)) {
            if (user.getCoin() >= GameConfig.SCROLL_SISYPHUS_COST) {
                if (!gamePanel.getState().isClickScrollSisyphus()) {
                    user.setCoin(user.getCoin() - GameConfig.SCROLL_SISYPHUS_COST);
                    gamePanel.getState().setClickScrollSisyphus(true);
                    gamePanel.setScrollSisyphusMode(true);
                    GameLogger.logShopPurchase("Scroll of Sisyphus", GameConfig.SCROLL_SISYPHUS_COST, user.getCoin() + GameConfig.SCROLL_SISYPHUS_COST, true);
                    repaint();
                }
            } else {
                GameLogger.logShopPurchase("Scroll of Sisyphus", GameConfig.SCROLL_SISYPHUS_COST, user.getCoin(), false);
                JOptionPane.showMessageDialog(
                        null,
                        "You don't have enough coins! Need " + GameConfig.SCROLL_SISYPHUS_COST + " coins.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        } else if (e.getSource().equals(scrollEliphas)) {
            if (user.getCoin() >= GameConfig.SCROLL_ELIPHAS_COST) {
                if (!gamePanel.getState().isClickScrollEliphas()) {
                    user.setCoin(user.getCoin() - GameConfig.SCROLL_ELIPHAS_COST);
                    gamePanel.getState().setClickScrollEliphas(true);
                    gamePanel.setScrollEliphasMode(true);
                    GameLogger.logShopPurchase("Scroll of Eliphas", GameConfig.SCROLL_ELIPHAS_COST, user.getCoin() + GameConfig.SCROLL_ELIPHAS_COST, true);
                    repaint();
                }
            } else {
                GameLogger.logShopPurchase("Scroll of Eliphas", GameConfig.SCROLL_ELIPHAS_COST, user.getCoin(), false);
                JOptionPane.showMessageDialog(
                        null,
                        "You don't have enough coins! Need " + GameConfig.SCROLL_ELIPHAS_COST + " coins.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        } else if (e.getSource().equals(back)) {
            gamePanel.setNPressed(false);
            Window.getMainFrame().setContentPane(gamePanel);
            gamePanel.checkSolution(false);
            AudioManager.resumeClip();
        }

    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        String status = "Coin : " + user.getCoin();
        g.setColor(Color.BLUE);
        g.drawString(status , 400 , 20);

        g.setColor(Color.BLACK);


        String speedBoosterText = "Speed Booster (6 coins) - Increases packet speed";
        if (gamePanel.getState().isClickSpeedBooster()) {
            speedBoosterText += " [PURCHASED]";
            g.setColor(Color.GREEN);
        }
        g.drawString(speedBoosterText, 50, 80);

        g.setColor(Color.BLACK);
        String speedLimiterText = "Speed Limiter (8 coins) - Reduces packet speed";
        if (gamePanel.getState().isClickSpeedLimiter()) {
            speedLimiterText += " [PURCHASED]";
            g.setColor(Color.GREEN);
        }
        g.drawString(speedLimiterText, 50, 160);

        g.setColor(Color.BLACK);
        String wireOptimizerText = "Wire Optimizer (10 coins) - Reduces wire length effects";
        if (gamePanel.getState().isClickWireOptimizer()) {
            wireOptimizerText += " [PURCHASED]";
            g.setColor(Color.GREEN);
        }
        g.drawString(wireOptimizerText, 50, 240);

        g.setColor(Color.BLACK);
        String scrollAergiaText = "Scroll of Aergia (10 coins) - Stops packets on selected wire";
        if (gamePanel.getState().isClickScrollAergia()) {
            scrollAergiaText += " [PURCHASED]";
            g.setColor(Color.GREEN);
        }
        g.drawString(scrollAergiaText, 50, 320);

        g.setColor(Color.BLACK);
        String scrollSisyphusText = "Scroll of Sisyphus (15 coins) - Move non-reference systems";
        if (gamePanel.getState().isClickScrollSisyphus()) {
            scrollSisyphusText += " [PURCHASED]";
            g.setColor(Color.GREEN);
        }
        g.drawString(scrollSisyphusText, 50, 400);

        g.setColor(Color.BLACK);
        String scrollEliphasText = "Scroll of Eliphas (20 coins) - Restore packet center of mass";
        if (gamePanel.getState().isClickScrollEliphas()) {
            scrollEliphasText += " [PURCHASED]";
            g.setColor(Color.GREEN);
        }
        g.drawString(scrollEliphasText, 50, 480);

        g.setColor(Color.BLACK);
        String atarText = "Atar (3 coins) - Protects from packet loss";
        if (gamePanel.getState().isClickAtar()) {
            atarText += " [PURCHASED]";
            g.setColor(Color.GREEN);
        }
        g.drawString(atarText, 300, 80);

        g.setColor(Color.BLACK);
        String airyamanText = "Airyaman (4 coins) - Prevents packet collisions";
        if (gamePanel.getState().isClickAiryaman()) {
            airyamanText += " [PURCHASED]";
            g.setColor(Color.GREEN);
        }
        g.drawString(airyamanText, 300, 160);

        g.setColor(Color.BLACK);
        String anahitaText = "Anahita (5 coins) - Removes packet noise";
        if (gamePanel.getState().isClickAnahita()) {
            anahitaText += " [PURCHASED]";
            g.setColor(Color.GREEN);
        }
        g.drawString(anahitaText, 300, 240);
    }
}
