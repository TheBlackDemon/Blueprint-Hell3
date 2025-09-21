package controller;

public class User {
    private String username;
    private int coin = 0;
    private int maxLevelPass = 0;

    public User(String username) {
        this.username = username;
    }

    public int getCoin() {
        return coin;
    }

    public void setCoin(int coin) {
        this.coin = coin;
    }

    public int getMaxLevelPass() {
        return maxLevelPass;
    }

    public void setMaxLevelPass(int maxLevelPass) {
        this.maxLevelPass = maxLevelPass;
    }
    
    public String getUsername() {
        return username;
    }
}
