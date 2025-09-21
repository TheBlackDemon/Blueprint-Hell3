package view;

import client.ClientMain;
import controller.AudioManager;
import controller.User;
import Game.GameConfig;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class VolumeSliderPanel extends JPanel implements ActionListener {
    private JSlider volumeSlider;
    private JLabel volumeLabel;
    private JButton back;
    private User user;
    private ClientMain client;
    private boolean onlineMode;

    public VolumeSliderPanel(ClientMain client, boolean onlineMode,User user) {
        this.client = client;
        this.onlineMode = onlineMode;
        this.user = user;
        this.setSize(GameConfig.WIDTH, GameConfig.HEIGHT);
        this.setLayout(null);
        this.setFocusable(true);
        this.requestFocus();
        this.requestFocusInWindow();
        initComponents();
        setupLayout();

        back = new JButton("Back");
        back.setFocusable(false);
        back.setBounds(300, 400, 200, 80);
        back.addActionListener(this);
        this.add(back);
    }

    private void initComponents() {
        volumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);

        volumeSlider.setMajorTickSpacing(20);
        volumeSlider.setMinorTickSpacing(5);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);

        volumeLabel = new JLabel("Volume : 50%", JLabel.CENTER);

        volumeSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateVolume();
            }
        });
    }

    private void setupLayout() {
        volumeLabel.setBounds(250, 90, 300, 30);
        add(volumeLabel);
        volumeSlider.setBounds(120, 200, 550, 50);
        add(volumeSlider);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    private void updateVolume() {
        int value = volumeSlider.getValue();
        volumeLabel.setText("Volume : " + value + "%");
        setGameVolume(value / 100.0f);
    }

    private void setGameVolume(float volume) {
        AudioManager.setGlobalVolume(volume);
    }

    public int getVolume() {
        return volumeSlider.getValue();
    }

    public void setVolume(int volume) {
        volumeSlider.setValue(volume);
        updateVolume();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(back)){
            MainScreen mainScreen = new MainScreen(user, client, onlineMode);
            Window.getMainFrame().setContentPane(mainScreen);
        }

    }
}