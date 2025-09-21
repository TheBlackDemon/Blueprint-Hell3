package Game;

import controller.User;

import java.util.ArrayList;
import java.util.List;

public class GameState {
    private final List<INode> nodes;
    private final List<IConnection> connections;
    private final List<Packet> packets;
    private final List<Shockwave> shockwaves;
    private int numberPacketsSquare = 0;
    private int numberPacketTriangle = 0;
    private int numberPacketsCircle = 0;
    private int numberPacketsConfidential4 = 0;
    private int numberPacketsConfidential6 = 0;
    private int numberPacketsBulky8 = 0;
    private int numberPacketsBulky10 = 0;
    private int packetLoss = 0;
    private int level;
    private boolean gameOver = false;
    private boolean successfully = false;
    private User user;
    private long levelStartTime;
    private boolean clickAtar = false;
    private boolean atar = false;
    private long startTimeAtar = 0;
    private boolean clickAiryaman = false;
    private boolean airyaman = false;
    private long startTimeAiryaman = 0;
    private boolean clickAnahita = false;
    private boolean clickSpeedBooster = false;
    private boolean clickSpeedLimiter = false;
    private boolean clickWireOptimizer = false;
    private boolean speedBoosterActive = false;
    private boolean speedLimiterActive = false;
    private boolean wireOptimizerActive = false;
    private long startTimeSpeedBooster = 0;
    private long startTimeSpeedLimiter = 0;
    private long startTimeWireOptimizer = 0;
    private java.util.Map<String, Long> vpnProtectedPackets = new java.util.HashMap<>(); // Track VPN protected packets
    private java.util.Map<String, String> packetOriginalTypes = new java.util.HashMap<>(); // Track original packet types for VPN reversion
    private java.util.Map<String, java.util.Set<Packet>> vpnNodeToProtectedPackets = new java.util.HashMap<>();
    
    private boolean clickScrollAergia = false;
    private boolean clickScrollSisyphus = false;
    private boolean clickScrollEliphas = false;
    private boolean scrollAergiaActive = false;
    private boolean scrollSisyphusActive = false;
    private boolean scrollEliphasActive = false;
    private long startTimeScrollAergia = 0;
    private long startTimeScrollSisyphus = 0;
    private long startTimeScrollEliphas = 0;
    private long lastScrollAergiaUse = 0; // For cooldown tracking
    private java.util.Map<String, Long> scrollAergiaEffects = new java.util.HashMap<>(); // Track active Aergia effects on connections
    private java.util.Map<String, Long> scrollEliphasEffects = new java.util.HashMap<>(); // Track active Eliphas effects on connections
    
    public GameState(int level, User user) {
        this.level = level;
        this.user = user;
        this.levelStartTime = System.currentTimeMillis();
        nodes = new ArrayList<>();
        connections = new ArrayList<>();
        packets = new ArrayList<>();
        shockwaves = new ArrayList<>();
    }
    public int getNumberPacketTriangle() {
        return numberPacketTriangle;
    }

    public void setNumberPacketTriangle(int numberPacketTriangle) {
        this.numberPacketTriangle = numberPacketTriangle;
    }

    public int getNumberPacketsCircle() {
        return numberPacketsCircle;
    }

    public void setNumberPacketsCircle(int numberPacketsCircle) {
        this.numberPacketsCircle = numberPacketsCircle;
    }

    public int getNumberPacketsSquare() {
        return numberPacketsSquare;
    }

    public void setNumberPacketsSquare(int numberPacketsSquare) {
        this.numberPacketsSquare = numberPacketsSquare;
    }

    public void addNode(INode node) {
        nodes.add(node);
    }

    public void addConnection(IConnection connection) {
        connections.add(connection);
    }

    public void removeConnection(IConnection connection) {
        connections.remove(connection);
    }

    public void clearPackets() {
        packets.clear();
    }

    public void addShockwave(Shockwave shockwave) {
        shockwaves.add(shockwave);
    }

    public void clearShockwaves() {
        shockwaves.clear();
    }

    public void clearConnectedInputs() {
        for (INode node : nodes) {
            node.getConnectedInputs().clear();
        }
    }

    public List<INode> getNodes() {
        return nodes;
    }

    public List<IConnection> getConnections() {
        return connections;
    }

    public List<Packet> getPackets() {
        return packets;
    }

    public List<Shockwave> getShockwaves() {
        return shockwaves;
    }

    public int getPacketLoss() {
        return packetLoss;
    }

    public void setPacketLoss(int packetLoss) {
        this.packetLoss = packetLoss;
    }

    public int getLevel() {
        return level;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public User getUser() {
        return user;
    }

    public boolean isSuccessfully() {
        return successfully;
    }

    public void setSuccessfully(boolean successfully) {
        this.successfully = successfully;
    }

    public boolean isClickAtar() {
        return clickAtar;
    }

    public void setClickAtar(boolean clickAtar) {
        this.clickAtar = clickAtar;
    }

    public boolean isAtar() {
        return atar;
    }

    public void setAtar(boolean atar) {
        this.atar = atar;
    }

    public boolean isClickAiryaman() {
        return clickAiryaman;
    }

    public void setClickAiryaman(boolean clickAiryaman) {
        this.clickAiryaman = clickAiryaman;
    }

    public boolean isAiryaman() {
        return airyaman;
    }

    public void setAiryaman(boolean airyaman) {
        this.airyaman = airyaman;
    }

    public boolean isClickAnahita() {
        return clickAnahita;
    }

    public void setClickAnahita(boolean clickAnahita) {
        this.clickAnahita = clickAnahita;
    }

    public long getStartTimeAtar() {
        return startTimeAtar;
    }

    public void setStartTimeAtar(long startTimeAtar) {
        this.startTimeAtar = startTimeAtar;
    }

    public long getStartTimeAiryaman() {
        return startTimeAiryaman;
    }

    public void setStartTimeAiryaman(long startTimeAiryaman) {
        this.startTimeAiryaman = startTimeAiryaman;
    }

    public java.util.Map<String, Long> getVpnProtectedPackets() {
        return vpnProtectedPackets;
    }

    public void setVpnProtectedPackets(java.util.Map<String, Long> vpnProtectedPackets) {
        this.vpnProtectedPackets = vpnProtectedPackets;
    }

    public java.util.Map<String, String> getPacketOriginalTypes() {
        return packetOriginalTypes;
    }

    public void setPacketOriginalTypes(java.util.Map<String, String> packetOriginalTypes) {
        this.packetOriginalTypes = packetOriginalTypes;
    }

    public void addVpnProtectedPacket(String packetId, long timestamp) {
        vpnProtectedPackets.put(packetId, timestamp);
    }

    public void removeVpnProtectedPacket(String packetId) {
        vpnProtectedPackets.remove(packetId);
    }

    public void addPacketOriginalType(String packetId, String originalType) {
        packetOriginalTypes.put(packetId, originalType);
    }

    public void removePacketOriginalType(String packetId) {
        packetOriginalTypes.remove(packetId);
    }

    public void addProtectedPacketForVpn(String vpnNodeId, Packet packet) {
        if (vpnNodeId == null || packet == null) return;
        vpnNodeToProtectedPackets.computeIfAbsent(vpnNodeId, k -> new java.util.HashSet<>()).add(packet);
    }

    public void removeProtectedPacketForVpn(String vpnNodeId, Packet packet) {
        if (vpnNodeId == null || packet == null) return;
        java.util.Set<Packet> set = vpnNodeToProtectedPackets.get(vpnNodeId);
        if (set != null) {
            set.remove(packet);
            if (set.isEmpty()) {
                vpnNodeToProtectedPackets.remove(vpnNodeId);
            }
        }
    }

    public java.util.Set<Packet> getProtectedPacketsByVpnNode(String vpnNodeId) {
        return vpnNodeToProtectedPackets.getOrDefault(vpnNodeId, java.util.Collections.emptySet());
    }

    public java.util.Map<String, java.util.Set<Packet>> getVpnNodeToProtectedPackets() {
        return vpnNodeToProtectedPackets;
    }

    public void setVpnNodeToProtectedPackets(java.util.Map<String, java.util.Set<Packet>> map) {
        this.vpnNodeToProtectedPackets = map != null ? map : new java.util.HashMap<>();
    }

    public boolean isClickSpeedBooster() {
        return clickSpeedBooster;
    }

    public void setClickSpeedBooster(boolean clickSpeedBooster) {
        this.clickSpeedBooster = clickSpeedBooster;
    }

    public boolean isSpeedBoosterActive() {
        return speedBoosterActive;
    }

    public void setSpeedBoosterActive(boolean speedBoosterActive) {
        this.speedBoosterActive = speedBoosterActive;
    }

    public long getStartTimeSpeedBooster() {
        return startTimeSpeedBooster;
    }

    public void setStartTimeSpeedBooster(long startTimeSpeedBooster) {
        this.startTimeSpeedBooster = startTimeSpeedBooster;
    }

    public boolean isClickSpeedLimiter() {
        return clickSpeedLimiter;
    }

    public void setClickSpeedLimiter(boolean clickSpeedLimiter) {
        this.clickSpeedLimiter = clickSpeedLimiter;
    }

    public boolean isSpeedLimiterActive() {
        return speedLimiterActive;
    }

    public void setSpeedLimiterActive(boolean speedLimiterActive) {
        this.speedLimiterActive = speedLimiterActive;
    }

    public long getStartTimeSpeedLimiter() {
        return startTimeSpeedLimiter;
    }

    public void setStartTimeSpeedLimiter(long startTimeSpeedLimiter) {
        this.startTimeSpeedLimiter = startTimeSpeedLimiter;
    }

    public boolean isClickWireOptimizer() {
        return clickWireOptimizer;
    }

    public void setClickWireOptimizer(boolean clickWireOptimizer) {
        this.clickWireOptimizer = clickWireOptimizer;
    }

    public boolean isWireOptimizerActive() {
        return wireOptimizerActive;
    }

    public void setWireOptimizerActive(boolean wireOptimizerActive) {
        this.wireOptimizerActive = wireOptimizerActive;
    }

    public long getStartTimeWireOptimizer() {
        return startTimeWireOptimizer;
    }

    public void setStartTimeWireOptimizer(long startTimeWireOptimizer) {
        this.startTimeWireOptimizer = startTimeWireOptimizer;
    }
    
    public int getNumberPacketsConfidential4() {
        return numberPacketsConfidential4;
    }
    
    public void setNumberPacketsConfidential4(int numberPacketsConfidential4) {
        this.numberPacketsConfidential4 = numberPacketsConfidential4;
    }
    
    public int getNumberPacketsConfidential6() {
        return numberPacketsConfidential6;
    }
    
    public void setNumberPacketsConfidential6(int numberPacketsConfidential6) {
        this.numberPacketsConfidential6 = numberPacketsConfidential6;
    }
    
    public int getNumberPacketsBulky8() {
        return numberPacketsBulky8;
    }
    
    public void setNumberPacketsBulky8(int numberPacketsBulky8) {
        this.numberPacketsBulky8 = numberPacketsBulky8;
    }
    
    public int getNumberPacketsBulky10() {
        return numberPacketsBulky10;
    }
    
    public void setNumberPacketsBulky10(int numberPacketsBulky10) {
        this.numberPacketsBulky10 = numberPacketsBulky10;
    }
    
    public boolean isClickScrollAergia() {
        return clickScrollAergia;
    }

    public void setClickScrollAergia(boolean clickScrollAergia) {
        this.clickScrollAergia = clickScrollAergia;
    }

    public boolean isScrollAergiaActive() {
        return scrollAergiaActive;
    }

    public void setScrollAergiaActive(boolean scrollAergiaActive) {
        this.scrollAergiaActive = scrollAergiaActive;
    }

    public long getStartTimeScrollAergia() {
        return startTimeScrollAergia;
    }

    public void setStartTimeScrollAergia(long startTimeScrollAergia) {
        this.startTimeScrollAergia = startTimeScrollAergia;
    }

    public long getLastScrollAergiaUse() {
        return lastScrollAergiaUse;
    }

    public void setLastScrollAergiaUse(long lastScrollAergiaUse) {
        this.lastScrollAergiaUse = lastScrollAergiaUse;
    }

    public java.util.Map<String, Long> getScrollAergiaEffects() {
        return scrollAergiaEffects;
    }

    public void setScrollAergiaEffects(java.util.Map<String, Long> scrollAergiaEffects) {
        this.scrollAergiaEffects = scrollAergiaEffects;
    }

    public void addScrollAergiaEffect(String connectionId, long timestamp) {
        scrollAergiaEffects.put(connectionId, timestamp);
    }

    public void removeScrollAergiaEffect(String connectionId) {
        scrollAergiaEffects.remove(connectionId);
    }

    public boolean isClickScrollSisyphus() {
        return clickScrollSisyphus;
    }

    public void setClickScrollSisyphus(boolean clickScrollSisyphus) {
        this.clickScrollSisyphus = clickScrollSisyphus;
    }

    public boolean isScrollSisyphusActive() {
        return scrollSisyphusActive;
    }

    public void setScrollSisyphusActive(boolean scrollSisyphusActive) {
        this.scrollSisyphusActive = scrollSisyphusActive;
    }

    public long getStartTimeScrollSisyphus() {
        return startTimeScrollSisyphus;
    }

    public void setStartTimeScrollSisyphus(long startTimeScrollSisyphus) {
        this.startTimeScrollSisyphus = startTimeScrollSisyphus;
    }

    public boolean isClickScrollEliphas() {
        return clickScrollEliphas;
    }

    public void setClickScrollEliphas(boolean clickScrollEliphas) {
        this.clickScrollEliphas = clickScrollEliphas;
    }

    public boolean isScrollEliphasActive() {
        return scrollEliphasActive;
    }

    public void setScrollEliphasActive(boolean scrollEliphasActive) {
        this.scrollEliphasActive = scrollEliphasActive;
    }

    public long getStartTimeScrollEliphas() {
        return startTimeScrollEliphas;
    }

    public void setStartTimeScrollEliphas(long startTimeScrollEliphas) {
        this.startTimeScrollEliphas = startTimeScrollEliphas;
    }

    public java.util.Map<String, Long> getScrollEliphasEffects() {
        return scrollEliphasEffects;
    }

    public void setScrollEliphasEffects(java.util.Map<String, Long> scrollEliphasEffects) {
        this.scrollEliphasEffects = scrollEliphasEffects;
    }

    public void addScrollEliphasEffect(String connectionId, long timestamp) {
        scrollEliphasEffects.put(connectionId, timestamp);
    }

    public void removeScrollEliphasEffect(String connectionId) {
        scrollEliphasEffects.remove(connectionId);
    }
    
    public long getLevelStartTime() {
        return levelStartTime;
    }
    
    public void setLevelStartTime(long levelStartTime) {
        this.levelStartTime = levelStartTime;
    }
}
