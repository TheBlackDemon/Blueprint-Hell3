package network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class LeaderboardEntry {
    private String username;
    private int level;
    private long completionTime; // in milliseconds
    private int xp;
    private int coins;
    private long timestamp;
    private String sessionId;
    
    public LeaderboardEntry() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public LeaderboardEntry(String username, int level, long completionTime, int xp, int coins) {
        this.username = username;
        this.level = level;
        this.completionTime = completionTime;
        this.xp = xp;
        this.coins = coins;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    
    public long getCompletionTime() { return completionTime; }
    public void setCompletionTime(long completionTime) { this.completionTime = completionTime; }
    
    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = xp; }
    
    public int getCoins() { return coins; }
    public void setCoins(int coins) { this.coins = coins; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String toJson() {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(this);
    }

    public static LeaderboardEntry fromJson(String json) {
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, LeaderboardEntry.class);
    }
    
    @Override
    public String toString() {
        return String.format("LeaderboardEntry{username='%s', level=%d, time=%dms, xp=%d, coins=%d}", 
                           username, level, completionTime, xp, coins);
    }
}
