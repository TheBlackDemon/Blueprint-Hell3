package network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class OfflineGameManager {
    private static final String OFFLINE_DATA_FILE = "offline_games.dat";
    private static final String LEADERBOARD_FILE = "local_leaderboard.dat";
    
    private final List<LeaderboardEntry> localLeaderboard = new CopyOnWriteArrayList<>();
    private final List<GameStateData> pendingGames = new CopyOnWriteArrayList<>();
    private final Gson gson = new GsonBuilder().create();
    
    public OfflineGameManager() {
        loadOfflineData();
    }

    public void saveGameResult(GameStateData gameState) {
        try {
            pendingGames.add(gameState);
            
            LeaderboardEntry entry = new LeaderboardEntry(
                gameState.getUsername(),
                gameState.getLevel(),
                System.currentTimeMillis() - gameState.getLevelStartTime(),
                calculateXP(gameState),
                gameState.getCoins()
            );
            entry.setSessionId("offline_" + System.currentTimeMillis());
            
            localLeaderboard.add(entry);
            localLeaderboard.sort((a, b) -> {
                int levelCompare = Integer.compare(b.getLevel(), a.getLevel());
                if (levelCompare != 0) return levelCompare;
                return Long.compare(a.getCompletionTime(), b.getCompletionTime());
            });
            
            if (localLeaderboard.size() > 100) {
                localLeaderboard.subList(100, localLeaderboard.size()).clear();
            }
            
            saveOfflineData();
            
            System.out.println("Game result saved offline: " + entry);
        } catch (Exception e) {
            System.err.println("Error saving offline game result: " + e.getMessage());
        }
    }

    public List<LeaderboardEntry> getLocalLeaderboard() {
        return new ArrayList<>(localLeaderboard);
    }

    public List<GameStateData> getPendingGames() {
        return new ArrayList<>(pendingGames);
    }

    public void markGamesAsSynchronized(List<GameStateData> synchronizedGames) {
        pendingGames.removeAll(synchronizedGames);
        saveOfflineData();
    }

    public void updateLeaderboard(List<LeaderboardEntry> serverLeaderboard) {
        Map<String, LeaderboardEntry> entryMap = new HashMap<>();
        
        for (LeaderboardEntry entry : localLeaderboard) {
            entryMap.put(entry.getUsername() + "_" + entry.getLevel(), entry);
        }
        
        for (LeaderboardEntry entry : serverLeaderboard) {
            String key = entry.getUsername() + "_" + entry.getLevel();
            LeaderboardEntry existing = entryMap.get(key);
            if (existing == null || entry.getCompletionTime() < existing.getCompletionTime()) {
                entryMap.put(key, entry);
            }
        }
        
        localLeaderboard.clear();
        localLeaderboard.addAll(entryMap.values());
        localLeaderboard.sort((a, b) -> {
            int levelCompare = Integer.compare(b.getLevel(), a.getLevel());
            if (levelCompare != 0) return levelCompare;
            return Long.compare(a.getCompletionTime(), b.getCompletionTime());
        });
        
        if (localLeaderboard.size() > 100) {
            localLeaderboard.subList(100, localLeaderboard.size()).clear();
        }
        
        saveOfflineData();
    }
    
    private int calculateXP(GameStateData gameState) {
        int baseXP = gameState.getLevel() * 100;
        int packetBonus = (gameState.getNumberPacketsSquare() + gameState.getNumberPacketTriangle() + 
                          gameState.getNumberPacketsCircle()) * 10;
        int timeBonus = Math.max(0, 1000 - (int)(System.currentTimeMillis() - gameState.getLevelStartTime()) / 1000);
        int lossPenalty = gameState.getPacketLoss() * 5;
        
        return Math.max(0, baseXP + packetBonus + timeBonus - lossPenalty);
    }
    
    private void loadOfflineData() {
        try {
            Path leaderboardPath = Paths.get(LEADERBOARD_FILE);
            if (Files.exists(leaderboardPath)) {
                String leaderboardJson = Files.readString(leaderboardPath);
                LeaderboardEntry[] entries = gson.fromJson(leaderboardJson, LeaderboardEntry[].class);
                if (entries != null) {
                    localLeaderboard.addAll(Arrays.asList(entries));
                }
            }
            
            Path gamesPath = Paths.get(OFFLINE_DATA_FILE);
            if (Files.exists(gamesPath)) {
                String gamesJson = Files.readString(gamesPath);
                GameStateData[] games = gson.fromJson(gamesJson, GameStateData[].class);
                if (games != null) {
                    pendingGames.addAll(Arrays.asList(games));
                }
            }
            
            System.out.println("Loaded offline data: " + localLeaderboard.size() + " leaderboard entries, " + 
                             pendingGames.size() + " pending games");
        } catch (Exception e) {
            System.err.println("Error loading offline data: " + e.getMessage());
        }
    }
    public void addToLeaderboardOnly(GameStateData gameState) {
        try {
            LeaderboardEntry entry = new LeaderboardEntry(
                    gameState.getUsername(),
                    gameState.getLevel(),
                    System.currentTimeMillis() - gameState.getLevelStartTime(),
                    calculateXP(gameState),
                    gameState.getCoins()
            );
            entry.setSessionId("online_" + System.currentTimeMillis());

            localLeaderboard.add(entry);
            localLeaderboard.sort((a, b) -> {
                int levelCompare = Integer.compare(b.getLevel(), a.getLevel());
                if (levelCompare != 0) return levelCompare;
                return Long.compare(a.getCompletionTime(), b.getCompletionTime());
            });

            if (localLeaderboard.size() > 100) {
                localLeaderboard.subList(100, localLeaderboard.size()).clear();
            }

            Path leaderboardPath = Paths.get(LEADERBOARD_FILE);
            String leaderboardJson = gson.toJson(localLeaderboard.toArray(new LeaderboardEntry[0]));
            Files.writeString(leaderboardPath, leaderboardJson);

        } catch (Exception e) {
            System.err.println("Error adding to leaderboard: " + e.getMessage());
        }
    }


    private void saveOfflineData() {
        try {
            Path leaderboardPath = Paths.get(LEADERBOARD_FILE);
            String leaderboardJson = gson.toJson(localLeaderboard.toArray(new LeaderboardEntry[0]));
            Files.writeString(leaderboardPath, leaderboardJson);
            
            Path gamesPath = Paths.get(OFFLINE_DATA_FILE);
            String gamesJson = gson.toJson(pendingGames.toArray(new GameStateData[0]));
            Files.writeString(gamesPath, gamesJson);
            
        } catch (Exception e) {
            System.err.println("Error saving offline data: " + e.getMessage());
        }
    }

    public void clearOfflineData() {
        localLeaderboard.clear();
        pendingGames.clear();
        try {
            Files.deleteIfExists(Paths.get(LEADERBOARD_FILE));
            Files.deleteIfExists(Paths.get(OFFLINE_DATA_FILE));
        } catch (Exception e) {
            System.err.println("Error clearing offline data: " + e.getMessage());
        }
    }
}
