package network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.net.NetworkInterface;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataIntegrityValidator {
    private static final String USER_DATA_DIR = "user_data";
    private static final String GAME_DATA_DIR = "game_data";
    private static final String DIV_SALT = "BlueprintHell_DIV_2024";
    
    private final Map<String, UserData> userDataCache = new ConcurrentHashMap<>();
    private final Map<String, GameData> gameDataCache = new ConcurrentHashMap<>();
    private final Gson gson;
    
    public DataIntegrityValidator() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        initializeDirectories();
    }
    
    private void initializeDirectories() {
        try {
            Files.createDirectories(Paths.get(USER_DATA_DIR));
            Files.createDirectories(Paths.get(GAME_DATA_DIR));
        } catch (IOException e) {
            System.err.println("Failed to create data directories: " + e.getMessage());
        }
    }

    public String getMacAddress() {
        try {
            NetworkInterface network = NetworkInterface.getByInetAddress(
                java.net.InetAddress.getLocalHost()
            );
            if (network != null) {
                byte[] mac = network.getHardwareAddress();
                if (mac != null) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                    }
                    return sb.toString();
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to get MAC address: " + e.getMessage());
        }
        return "UNKNOWN_MAC_" + System.currentTimeMillis();
    }

    public String getMacAddressFromConnection(java.net.Socket socket) {
        try {
            // Try to get MAC address from the connected client
            String clientInfo = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
            return "CLIENT_" + clientInfo.hashCode();
        } catch (Exception e) {
            System.err.println("Failed to get MAC address from connection: " + e.getMessage());
            return "UNKNOWN_CLIENT_" + System.currentTimeMillis();
        }
    }

    public UserData getUserData(String macAddress) {
        if (userDataCache.containsKey(macAddress)) {
            return userDataCache.get(macAddress);
        }
        
        UserData userData = loadUserDataFromFile(macAddress);
        if (userData == null) {
            userData = new UserData(macAddress);
            saveUserData(userData);
        }
        
        userDataCache.put(macAddress, userData);
        return userData;
    }
    

    public void saveUserData(UserData userData) {
        try {
            String filename = USER_DATA_DIR + "/" + sanitizeMacAddress(userData.getMacAddress()) + ".json";
            String json = gson.toJson(userData);
            Files.write(Paths.get(filename), json.getBytes());
            userDataCache.put(userData.getMacAddress(), userData);
        } catch (IOException e) {
            System.err.println("Failed to save user data: " + e.getMessage());
        }
    }

    private UserData loadUserDataFromFile(String macAddress) {
        try {
            String filename = USER_DATA_DIR + "/" + sanitizeMacAddress(macAddress) + ".json";
            Path path = Paths.get(filename);
            if (Files.exists(path)) {
                String json = new String(Files.readAllBytes(path));
                return gson.fromJson(json, UserData.class);
            }
        } catch (IOException e) {
            System.err.println("Failed to load user data: " + e.getMessage());
        }
        return null;
    }

    public boolean validateGameResult(String macAddress, GameResultData resultData) {
        UserData userData = getUserData(macAddress);
        
        if (!isValidGameResult(resultData)) {
            return false;
        }
        
        if (resultData.getLevel() > userData.getMaxLevelPass() + 1) {
            System.err.println("User " + macAddress + " attempted to submit result for level " + 
                             resultData.getLevel() + " but max level is " + userData.getMaxLevelPass());
            return false;
        }
        
        if (resultData.getCompletionTime() < 1000) { // Less than 1 second is suspicious
            System.err.println("Suspicious completion time: " + resultData.getCompletionTime() + "ms");
            return false;
        }
        
        String expectedHash = generateDataHash(resultData, macAddress);
        if (!expectedHash.equals(resultData.getDataHash())) {
            System.err.println("Data hash mismatch for user " + macAddress);
            return false;
        }
        
        return true;
    }

    public String generateDataHash(GameResultData data, String macAddress) {
        try {
            String dataString = data.getLevel() + ":" + data.getCompletionTime() + ":" + 
                              data.getPacketLoss() + ":" + data.getCoinsEarned() + ":" + 
                              macAddress + ":" + DIV_SALT;
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(dataString.getBytes());
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("SHA-256 algorithm not available: " + e.getMessage());
            return "INVALID_HASH";
        }
    }

    private boolean isValidGameResult(GameResultData data) {
        return data.getLevel() > 0 && 
               data.getLevel() <= 100 && // Maximum level limit
               data.getCompletionTime() > 0 && 
               data.getCompletionTime() < 3600000 && // Max 1 hour
               data.getPacketLoss() >= 0 && 
               data.getPacketLoss() <= 1000 && // Max 1000 packet loss
               data.getCoinsEarned() >= 0 && 
               data.getCoinsEarned() <= 10000; // Max 10000 coins per game
    }

    private String sanitizeMacAddress(String macAddress) {
        return macAddress.replaceAll("[^a-zA-Z0-9]", "_");
    }

    public void updateUserProgress(String macAddress, GameResultData resultData) {
        UserData userData = getUserData(macAddress);
        
        if (resultData.getLevel() > userData.getMaxLevelPass()) {
            userData.setMaxLevelPass(resultData.getLevel());
        }
        
        userData.setCoins(userData.getCoins() + resultData.getCoinsEarned());
        
        userData.addGameResult(resultData);
        
        userData.addXP(calculateXP(resultData));
        
        saveUserData(userData);
    }

    private int calculateXP(GameResultData data) {
        int baseXP = data.getLevel() * 100;
        int timeBonus = Math.max(0, 1000 - (int)(data.getCompletionTime() / 1000)); // Time bonus
        int lossPenalty = data.getPacketLoss() * 10; // Penalty for packet loss
        
        return Math.max(0, baseXP + timeBonus - lossPenalty);
    }

    public GameData getGameData() {
        if (gameDataCache.containsKey("main")) {
            return gameDataCache.get("main");
        }
        
        GameData gameData = loadGameDataFromFile();
        if (gameData == null) {
            gameData = new GameData();
            saveGameData(gameData);
        }
        
        gameDataCache.put("main", gameData);
        return gameData;
    }

    public void saveGameData(GameData gameData) {
        try {
            String filename = GAME_DATA_DIR + "/game_data.json";
            String json = gson.toJson(gameData);
            Files.write(Paths.get(filename), json.getBytes());
            gameDataCache.put("main", gameData);
        } catch (IOException e) {
            System.err.println("Failed to save game data: " + e.getMessage());
        }
    }

    private GameData loadGameDataFromFile() {
        try {
            String filename = GAME_DATA_DIR + "/game_data.json";
            Path path = Paths.get(filename);
            if (Files.exists(path)) {
                String json = new String(Files.readAllBytes(path));
                return gson.fromJson(json, GameData.class);
            }
        } catch (IOException e) {
            System.err.println("Failed to load game data: " + e.getMessage());
        }
        return null;
    }

    public static class UserData {
        private String macAddress;
        private String username;
        private int coins;
        private int maxLevelPass;
        private int totalXP;
        private List<String> unlockedAbilities;
        private List<GameResultData> gameHistory;
        private long lastLogin;
        private Map<String, Long> abilityCooldowns;
        
        public UserData() {
            this.unlockedAbilities = new ArrayList<>();
            this.gameHistory = new ArrayList<>();
            this.abilityCooldowns = new HashMap<>();
        }
        
        public UserData(String macAddress) {
            this();
            this.macAddress = macAddress;
            this.username = "Player_" + System.currentTimeMillis();
            this.coins = 0;
            this.maxLevelPass = 0;
            this.totalXP = 0;
            this.lastLogin = System.currentTimeMillis();
        }
        
        public String getMacAddress() { return macAddress; }
        public void setMacAddress(String macAddress) { this.macAddress = macAddress; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public int getCoins() { return coins; }
        public void setCoins(int coins) { this.coins = coins; }
        
        public int getMaxLevelPass() { return maxLevelPass; }
        public void setMaxLevelPass(int maxLevelPass) { this.maxLevelPass = maxLevelPass; }
        
        public int getTotalXP() { return totalXP; }
        public void setTotalXP(int totalXP) { this.totalXP = totalXP; }
        
        public List<String> getUnlockedAbilities() { return unlockedAbilities; }
        public void setUnlockedAbilities(List<String> unlockedAbilities) { this.unlockedAbilities = unlockedAbilities; }
        
        public List<GameResultData> getGameHistory() { return gameHistory; }
        public void setGameHistory(List<GameResultData> gameHistory) { this.gameHistory = gameHistory; }
        
        public long getLastLogin() { return lastLogin; }
        public void setLastLogin(long lastLogin) { this.lastLogin = lastLogin; }
        
        public Map<String, Long> getAbilityCooldowns() { return abilityCooldowns; }
        public void setAbilityCooldowns(Map<String, Long> abilityCooldowns) { this.abilityCooldowns = abilityCooldowns; }
        
        public void addXP(int xp) {
            this.totalXP += xp;
        }
        
        public void addGameResult(GameResultData result) {
            this.gameHistory.add(result);
            if (this.gameHistory.size() > 100) {
                this.gameHistory.remove(0);
            }
        }
        
        public void unlockAbility(String ability) {
            if (!this.unlockedAbilities.contains(ability)) {
                this.unlockedAbilities.add(ability);
            }
        }
    }

    public static class GameData {
        private Map<String, Object> levelConfigurations;
        private Map<String, Object> systemConfigurations;
        private Map<String, Object> packetConfigurations;
        private List<String> availableAbilities;
        private Map<String, Integer> abilityCosts;
        
        public GameData() {
            this.levelConfigurations = new HashMap<>();
            this.systemConfigurations = new HashMap<>();
            this.packetConfigurations = new HashMap<>();
            this.availableAbilities = new ArrayList<>();
            this.abilityCosts = new HashMap<>();
            
            initializeDefaultData();
        }
        
        private void initializeDefaultData() {
            levelConfigurations.put("maxLevel", 50);
            levelConfigurations.put("basePacketSpeed", 0.01);
            levelConfigurations.put("baseWireLength", 100);
            
            systemConfigurations.put("maxNodes", 20);
            systemConfigurations.put("maxConnections", 50);
            
            packetConfigurations.put("packetTypes", Arrays.asList("square", "triangle", "circle", "confidential", "bulky"));
            
            availableAbilities.add("speed_booster");
            availableAbilities.add("speed_limiter");
            availableAbilities.add("wire_optimizer");
            availableAbilities.add("scroll_aergia");
            availableAbilities.add("scroll_sisyphus");
            availableAbilities.add("scroll_eliphas");
            
            abilityCosts.put("speed_booster", 100);
            abilityCosts.put("speed_limiter", 150);
            abilityCosts.put("wire_optimizer", 200);
            abilityCosts.put("scroll_aergia", 300);
            abilityCosts.put("scroll_sisyphus", 400);
            abilityCosts.put("scroll_eliphas", 500);
        }
        
        public Map<String, Object> getLevelConfigurations() { return levelConfigurations; }
        public void setLevelConfigurations(Map<String, Object> levelConfigurations) { this.levelConfigurations = levelConfigurations; }
        
        public Map<String, Object> getSystemConfigurations() { return systemConfigurations; }
        public void setSystemConfigurations(Map<String, Object> systemConfigurations) { this.systemConfigurations = systemConfigurations; }
        
        public Map<String, Object> getPacketConfigurations() { return packetConfigurations; }
        public void setPacketConfigurations(Map<String, Object> packetConfigurations) { this.packetConfigurations = packetConfigurations; }
        
        public List<String> getAvailableAbilities() { return availableAbilities; }
        public void setAvailableAbilities(List<String> availableAbilities) { this.availableAbilities = availableAbilities; }
        
        public Map<String, Integer> getAbilityCosts() { return abilityCosts; }
        public void setAbilityCosts(Map<String, Integer> abilityCosts) { this.abilityCosts = abilityCosts; }
    }

    public static class GameResultData {
        private int level;
        private long completionTime;
        private int packetLoss;
        private int coinsEarned;
        private String dataHash;
        private long timestamp;
        
        public GameResultData() {
            this.timestamp = System.currentTimeMillis();
        }
        
        public GameResultData(int level, long completionTime, int packetLoss, int coinsEarned) {
            this();
            this.level = level;
            this.completionTime = completionTime;
            this.packetLoss = packetLoss;
            this.coinsEarned = coinsEarned;
        }
        
        public int getLevel() { return level; }
        public void setLevel(int level) { this.level = level; }
        
        public long getCompletionTime() { return completionTime; }
        public void setCompletionTime(long completionTime) { this.completionTime = completionTime; }
        
        public int getPacketLoss() { return packetLoss; }
        public void setPacketLoss(int packetLoss) { this.packetLoss = packetLoss; }
        
        public int getCoinsEarned() { return coinsEarned; }
        public void setCoinsEarned(int coinsEarned) { this.coinsEarned = coinsEarned; }
        
        public String getDataHash() { return dataHash; }
        public void setDataHash(String dataHash) { this.dataHash = dataHash; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}
