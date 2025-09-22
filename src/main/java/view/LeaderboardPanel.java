package view;

import client.ClientMain;
import controller.User;
import network.LeaderboardEntry;
import network.OfflineGameManager;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class LeaderboardPanel extends JPanel {
    private JTable leaderboardTable;
    private DefaultTableModel tableModel;
    private JButton refreshButton;
    private JButton backButton;
    private JLabel statusLabel;
    private JComboBox<String> levelFilter;
    private JComboBox<String> sortByCombo;
    private List<LeaderboardEntry> currentEntries = new ArrayList<>();

    private ClientMain client;
    private OfflineGameManager offlineManager;
    private boolean isOnline;
    private User user;

    public LeaderboardPanel(User user, ClientMain client, OfflineGameManager offlineManager, boolean isOnline) {
        this.user = user;
        this.client = client;
        this.offlineManager = offlineManager;
        this.isOnline = isOnline;

        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }

    private void initializeComponents() {
        String[] columnNames = {"Rank", "Username", "Level", "Time", "XP", "Coins", "Date"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        leaderboardTable = new JTable(tableModel);
        leaderboardTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        leaderboardTable.setRowHeight(25);

        refreshButton = new JButton("Refresh");
        backButton = new JButton("Back");

        statusLabel = new JLabel("Loading leaderboard...");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        levelFilter = new JComboBox<>(new String[]{
                "All Levels", "Level 1", "Level 2", "Level 3", "Level 4", "Level 5"
        });

        sortByCombo = new JComboBox<>(new String[]{
                "Level (High to Low)", "Time (Fast to Slow)", "XP (High to Low)", "Coins (High to Low)"
        });
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(new JLabel("Level:"));
        topPanel.add(levelFilter);
        topPanel.add(new JLabel("Sort by:"));
        topPanel.add(sortByCombo);
        topPanel.add(refreshButton);
        topPanel.add(backButton);

        JScrollPane scrollPane = new JScrollPane(leaderboardTable);
        scrollPane.setPreferredSize(new Dimension(800, 400));

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(statusLabel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        refreshButton.addActionListener((ActionEvent e) -> refreshLeaderboard());

        backButton.addActionListener((ActionEvent e) -> {
            MainScreen main = new MainScreen(user, client, isOnline);
            Window.getMainFrame().setContentPane(main);
            Window.getMainFrame().revalidate();
            Window.getMainFrame().repaint();
        });

        levelFilter.addActionListener((ActionEvent e) -> filterAndSort());
        sortByCombo.addActionListener((ActionEvent e) -> filterAndSort());
    }

    public void updateLeaderboard(List<LeaderboardEntry> entries) {
        SwingUtilities.invokeLater(() -> {
            currentEntries = (entries != null) ? new ArrayList<>(entries) : new ArrayList<>();
            filterAndSort();
        });
    }

    public void refreshLeaderboard() {
        statusLabel.setText("Refreshing leaderboard...");
        if (isOnline && client != null && client.isConnected()) {
            client.requestLeaderboard();
        } else {
            updateLeaderboard(offlineManager.getLocalLeaderboard());
        }
    }

    private void filterAndSort() {
        if (currentEntries == null || currentEntries.isEmpty()) {
            statusLabel.setText("No leaderboard data available");
            tableModel.setRowCount(0);
            return;
        }

        List<LeaderboardEntry> filtered = new ArrayList<>(currentEntries);

        String selectedLevel = (String) levelFilter.getSelectedItem();
        if (selectedLevel != null && !selectedLevel.equals("All Levels")) {
            int level = Integer.parseInt(selectedLevel.replace("Level ", "").trim());
            filtered = filtered.stream()
                    .filter(e -> e.getLevel() == level)
                    .collect(Collectors.toList());
        }

        String sortBy = (String) sortByCombo.getSelectedItem();
        if (sortBy != null) {
            Comparator<LeaderboardEntry> comparator;
            switch (sortBy) {
                case "Time (Fast to Slow)":
                    comparator = Comparator.comparingLong(LeaderboardEntry::getCompletionTime);
                    break;
                case "XP (High to Low)":
                    comparator = Comparator.comparingInt(LeaderboardEntry::getXp).reversed();
                    break;
                case "Coins (High to Low)":
                    comparator = Comparator.comparingInt(LeaderboardEntry::getCoins).reversed();
                    break;
                default:
                    comparator = Comparator.comparingInt(LeaderboardEntry::getLevel).reversed();
                    break;
            }
            filtered.sort(comparator);
        }

        tableModel.setRowCount(0);
        int rank = 1;
        for (LeaderboardEntry entry : filtered) {
            Object[] row = {
                    rank++,
                    entry.getUsername(),
                    entry.getLevel(),
                    formatTime(entry.getCompletionTime()),
                    entry.getXp(),
                    entry.getCoins(),
                    formatDate(entry.getTimestamp())
            };
            tableModel.addRow(row);
        }

        statusLabel.setText("Leaderboard updated - " + filtered.size() + " entries");
    }

    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private String formatDate(long timestamp) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(new java.util.Date(timestamp));
    }
}
