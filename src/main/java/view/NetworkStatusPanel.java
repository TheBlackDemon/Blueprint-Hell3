package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class NetworkStatusPanel extends JPanel {
    private JLabel statusLabel;
    private JLabel serverLabel;
    private JButton connectButton;
    private JButton disconnectButton;
    private JButton retryButton;
    private JProgressBar connectionProgress;
    private JTextField serverAddressField;
    private JSpinner serverPortSpinner;
    
    private NetworkStatusListener listener;
    
    public interface NetworkStatusListener {
        void onConnect(String host, int port);
        void onDisconnect();
        void onRetry();
    }
    
    public NetworkStatusPanel() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setConnectionStatus(false, "Disconnected");
    }
    
    private void initializeComponents() {
        statusLabel = new JLabel("Disconnected");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        
        serverLabel = new JLabel("Server: Not connected");
        serverLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        
        connectButton = new JButton("Connect");
        disconnectButton = new JButton("Disconnect");
        retryButton = new JButton("Retry");
        
        serverAddressField = new JTextField("localhost", 10);
        serverPortSpinner = new JSpinner(new SpinnerNumberModel(8888, 1, 65535, 1));
        
        connectionProgress = new JProgressBar();
        connectionProgress.setIndeterminate(true);
        connectionProgress.setVisible(false);
        
        disconnectButton.setEnabled(false);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Network Status"));
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Server:"));
        topPanel.add(serverAddressField);
        topPanel.add(new JLabel("Port:"));
        topPanel.add(serverPortSpinner);
        topPanel.add(connectButton);
        topPanel.add(disconnectButton);
        topPanel.add(retryButton);
        
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        centerPanel.add(statusLabel);
        centerPanel.add(Box.createHorizontalStrut(20));
        centerPanel.add(serverLabel);
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(connectionProgress);
        
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String host = serverAddressField.getText().trim();
                int port = (Integer) serverPortSpinner.getValue();
                
                if (host.isEmpty()) {
                    JOptionPane.showMessageDialog(NetworkStatusPanel.this, 
                        "Please enter a server address", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                if (listener != null) {
                    listener.onConnect(host, port);
                }
            }
        });
        
        disconnectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (listener != null) {
                    listener.onDisconnect();
                }
            }
        });
        
        retryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (listener != null) {
                    listener.onRetry();
                }
            }
        });
    }
    
    public void setConnectionStatus(boolean connected, String message) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(message);
            if (connected) {
                statusLabel.setForeground(Color.GREEN);
                connectButton.setEnabled(false);
                disconnectButton.setEnabled(true);
                retryButton.setEnabled(false);
                serverAddressField.setEnabled(false);
                serverPortSpinner.setEnabled(false);
            } else {
                statusLabel.setForeground(Color.RED);
                connectButton.setEnabled(true);
                disconnectButton.setEnabled(false);
                retryButton.setEnabled(true);
                serverAddressField.setEnabled(true);
                serverPortSpinner.setEnabled(true);
            }
        });
    }
    
    public void setServerInfo(String serverInfo) {
        SwingUtilities.invokeLater(() -> {
            serverLabel.setText("Server: " + serverInfo);
        });
    }
    
    public void showConnectionProgress(boolean show) {
        SwingUtilities.invokeLater(() -> {
            connectionProgress.setVisible(show);
            if (show) {
                connectionProgress.setIndeterminate(true);
            }
        });
    }
    
    public void showError(String error) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, error, "Connection Error", JOptionPane.ERROR_MESSAGE);
            setConnectionStatus(false, "Connection failed: " + error);
        });
    }
    
    public void setNetworkStatusListener(NetworkStatusListener listener) {
        this.listener = listener;
    }
    
    public String getServerAddress() {
        return serverAddressField.getText().trim();
    }
    
    public int getServerPort() {
        return (Integer) serverPortSpinner.getValue();
    }
}
