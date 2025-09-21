package Game;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.awt.Point;

public class SaveManager {
    private static final String SAVE_FILE_PATH = "game_save.dat";
    private static final long AUTO_SAVE_INTERVAL_MS = 30000; // Auto-save every 30 seconds
    private long lastAutoSave = 0;
    
    public void saveGame(GameState state) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE_PATH))) {
            SaveData saveData = new SaveData();
            saveData.level = state.getLevel();
            saveData.packetLoss = state.getPacketLoss();
            saveData.gameOver = state.isGameOver();
            saveData.successfully = state.isSuccessfully();
            saveData.userCoins = state.getUser().getCoin();
            saveData.nodes = new ArrayList<>();
            for (INode node : state.getNodes()) {
                NodeData nodeData = new NodeData();
                nodeData.id = node.getId();
                nodeData.x = node.getX();
                nodeData.y = node.getY();
                nodeData.inputShapes = node.getInputShapes();
                nodeData.outputShapes = node.getOutputShapes();
                nodeData.systemType = ((Node) node).getSystemType();
                nodeData.isDamaged = ((Node) node).isDamaged();
                nodeData.damageStartTime = ((Node) node).getDamageStartTime();
                nodeData.lastAntitrojanOperation = ((Node) node).getLastAntitrojanOperation();
                saveData.nodes.add(nodeData);
            }
            saveData.connections = new ArrayList<>();
            for (IConnection conn : state.getConnections()) {
                ConnectionData connData = new ConnectionData();
                connData.fromNodeId = conn.getFromNode().getId();
                connData.fromPort = conn.getFromPort();
                connData.toNodeId = conn.getToNode().getId();
                connData.toPort = conn.getToPort();
                connData.waypoints = new ArrayList<>(conn.getWaypoints());
                connData.bendPoints = new ArrayList<>();
                for (BendPoint bp : conn.getBendPoints()) {
                    BendPointData bpData = new BendPointData();
                    bpData.x = bp.getPosition().x;
                    bpData.y = bp.getPosition().y;
                    bpData.maxRadius = bp.getMaxRadius();
                    connData.bendPoints.add(bpData);
                }
                connData.bulkyPacketPasses = ((Connection) conn).getBulkyPacketPasses();
                connData.isDestroyed = ((Connection) conn).isDestroyed();
                saveData.connections.add(connData);
            }
            saveData.packets = new ArrayList<>();
            for (Packet packet : state.getPackets()) {
                PacketData packetData = new PacketData();
                packetData.packetType = packet.getPacketType();
                packetData.originalPacketType = packet.getOriginalPacketType();
                packetData.isProtected = packet.isProtected();
                try {
                    java.lang.reflect.Field f1 = Packet.class.getDeclaredField("vpnSourceNodeId");
                    java.lang.reflect.Field f2 = Packet.class.getDeclaredField("vpnSessionId");
                    f1.setAccessible(true);
                    f2.setAccessible(true);
                    Object vpnNodeId = f1.get(packet);
                    Object vpnSessionId = f2.get(packet);
                    packetData.vpnSourceNodeId = vpnNodeId != null ? vpnNodeId.toString() : null;
                    packetData.vpnSessionId = vpnSessionId != null ? (Long) vpnSessionId : 0L;
                } catch (Exception ignore) {}
                packetData.currentSpeed = packet.getCurrentSpeed();
                packetData.noise = packet.getNoise();
                packetData.progress = packet.getProgress();
                packetData.segmentIndex = packet.getSegmentIndex();
                packetData.displacement = packet.getDisplacement();
                packetData.creationTime = packet.getCreationTime();
                packetData.lastMovementTime = packet.getLastMovementTime();
                packetData.packetSize = packet.getPacketSize();
                packetData.isLargePacket = packet.isLargePacket();
                packetData.isBitPacket = packet.isBitPacket();
                packetData.parentPacketId = packet.getParentPacketId();
                packetData.isReturning = packet.isReturning();
                packetData.acceleration = packet.getAcceleration();
                packetData.movementBehavior = packet.getMovementBehavior();
                packetData.distanceTraveled = packet.getDistanceTraveled();
                packetData.parentBulkyPacketId = packet.getParentBulkyPacketId();
                if (packet.getCurrentConnection() != null) {
                    for (int i = 0; i < state.getConnections().size(); i++) {
                        if (state.getConnections().get(i) == packet.getCurrentConnection()) {
                            packetData.connectionIndex = i;
                            break;
                        }
                    }
                }
                saveData.packets.add(packetData);
            }
            saveData.clickAtar = state.isClickAtar();
            saveData.atar = state.isAtar();
            saveData.startTimeAtar = state.getStartTimeAtar();
            saveData.clickAiryaman = state.isClickAiryaman();
            saveData.airyaman = state.isAiryaman();
            saveData.startTimeAiryaman = state.getStartTimeAiryaman();
            saveData.clickAnahita = state.isClickAnahita();
            saveData.clickSpeedBooster = state.isClickSpeedBooster();
            saveData.clickSpeedLimiter = state.isClickSpeedLimiter();
            saveData.clickWireOptimizer = state.isClickWireOptimizer();
            saveData.speedBoosterActive = state.isSpeedBoosterActive();
            saveData.speedLimiterActive = state.isSpeedLimiterActive();
            saveData.wireOptimizerActive = state.isWireOptimizerActive();
            saveData.startTimeSpeedBooster = state.getStartTimeSpeedBooster();
            saveData.startTimeSpeedLimiter = state.getStartTimeSpeedLimiter();
            saveData.startTimeWireOptimizer = state.getStartTimeWireOptimizer();
            saveData.clickScrollAergia = state.isClickScrollAergia();
            saveData.clickScrollSisyphus = state.isClickScrollSisyphus();
            saveData.clickScrollEliphas = state.isClickScrollEliphas();
            saveData.scrollAergiaActive = state.isScrollAergiaActive();
            saveData.scrollSisyphusActive = state.isScrollSisyphusActive();
            saveData.scrollEliphasActive = state.isScrollEliphasActive();
            saveData.startTimeScrollAergia = state.getStartTimeScrollAergia();
            saveData.startTimeScrollSisyphus = state.getStartTimeScrollSisyphus();
            saveData.startTimeScrollEliphas = state.getStartTimeScrollEliphas();
            saveData.lastScrollAergiaUse = state.getLastScrollAergiaUse();
            saveData.scrollAergiaEffects = new HashMap<>(state.getScrollAergiaEffects());
            saveData.scrollEliphasEffects = new HashMap<>(state.getScrollEliphasEffects());
            saveData.vpnProtectedPackets = new HashMap<>(state.getVpnProtectedPackets());
            saveData.packetOriginalTypes = new HashMap<>(state.getPacketOriginalTypes());
            saveData.numberPacketsSquare = state.getNumberPacketsSquare();
            saveData.numberPacketTriangle = state.getNumberPacketTriangle();
            saveData.numberPacketsCircle = state.getNumberPacketsCircle();
            saveData.numberPacketsConfidential4 = state.getNumberPacketsConfidential4();
            saveData.numberPacketsConfidential6 = state.getNumberPacketsConfidential6();
            saveData.numberPacketsBulky8 = state.getNumberPacketsBulky8();
            saveData.numberPacketsBulky10 = state.getNumberPacketsBulky10();
            
            oos.writeObject(saveData);
            System.out.println("Game saved successfully");
            
        } catch (IOException e) {
            System.err.println("Failed to save game: " + e.getMessage());
        }
    }
    
    public boolean loadGame(GameState state) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SAVE_FILE_PATH))) {
            SaveData saveData = (SaveData) ois.readObject();
            state.setPacketLoss(saveData.packetLoss);
            state.setGameOver(saveData.gameOver);
            state.setSuccessfully(saveData.successfully);
            state.getUser().setCoin(saveData.userCoins);
            state.getNodes().clear();
            state.getConnections().clear();
            state.clearPackets();
            for (NodeData nodeData : saveData.nodes) {
                Node node = new Node(nodeData.x, nodeData.y, nodeData.id, 
                    nodeData.inputShapes, nodeData.outputShapes, nodeData.systemType);
                node.setDamaged(nodeData.isDamaged);
                node.setDamageStartTime(nodeData.damageStartTime);
                node.setLastAntitrojanOperation(nodeData.lastAntitrojanOperation);
                state.addNode(node);
            }
            for (ConnectionData connData : saveData.connections) {
                INode fromNode = findNodeById(state, connData.fromNodeId);
                INode toNode = findNodeById(state, connData.toNodeId);
                if (fromNode != null && toNode != null) {
                    Connection conn = new Connection(fromNode, connData.fromPort, toNode, connData.toPort);
                    conn.getWaypoints().addAll(connData.waypoints);
                    for (BendPointData bpData : connData.bendPoints) {
                        BendPoint bp = new BendPoint(bpData.x, bpData.y, bpData.maxRadius);
                        conn.getBendPoints().add(bp);
                    }
                    ((Connection) conn).setBulkyPacketPasses(connData.bulkyPacketPasses);
                    ((Connection) conn).setDestroyed(connData.isDestroyed);
                    state.addConnection(conn);
                }
            }
            for (PacketData packetData : saveData.packets) {
                if (packetData.connectionIndex >= 0 && packetData.connectionIndex < state.getConnections().size()) {
                    IConnection conn = state.getConnections().get(packetData.connectionIndex);
                    Packet packet = new Packet(conn, packetData.packetType);
                    packet.setOriginalPacketType(packetData.originalPacketType);
                    packet.setProtected(packetData.isProtected);
                    if (packetData.vpnSourceNodeId != null && packetData.isProtected) {
                        try {
                            java.lang.reflect.Field f1 = Packet.class.getDeclaredField("vpnSourceNodeId");
                            java.lang.reflect.Field f2 = Packet.class.getDeclaredField("vpnSessionId");
                            f1.setAccessible(true);
                            f2.setAccessible(true);
                            f1.set(packet, packetData.vpnSourceNodeId);
                            f2.set(packet, packetData.vpnSessionId);
                        } catch (Exception ignore) {}
                        state.addProtectedPacketForVpn(packetData.vpnSourceNodeId, packet);
                    }
                    packet.setCurrentSpeed(packetData.currentSpeed);
                    packet.setNoise(packetData.noise);
                    packet.setProgress(packetData.progress);
                    packet.setSegmentIndex(packetData.segmentIndex);
                    packet.setDisplacement(packetData.displacement);
                    packet.setCreationTime(packetData.creationTime);
                    packet.setLastMovementTime(packetData.lastMovementTime);
                    packet.setPacketSize(packetData.packetSize);
                    packet.setLargePacket(packetData.isLargePacket);
                    packet.setBitPacket(packetData.isBitPacket);
                    packet.setParentPacketId(packetData.parentPacketId);
                    packet.setReturning(packetData.isReturning);
                    packet.setAcceleration(packetData.acceleration);
                    packet.setMovementBehavior(packetData.movementBehavior);
                    packet.setDistanceTraveled(packetData.distanceTraveled);
                    packet.setParentBulkyPacketId(packetData.parentBulkyPacketId);
                    state.getPackets().add(packet);
                }
            }
            state.setClickAtar(saveData.clickAtar);
            state.setAtar(saveData.atar);
            state.setStartTimeAtar(saveData.startTimeAtar);
            state.setClickAiryaman(saveData.clickAiryaman);
            state.setAiryaman(saveData.airyaman);
            state.setStartTimeAiryaman(saveData.startTimeAiryaman);
            state.setClickAnahita(saveData.clickAnahita);
            state.setClickSpeedBooster(saveData.clickSpeedBooster);
            state.setClickSpeedLimiter(saveData.clickSpeedLimiter);
            state.setClickWireOptimizer(saveData.clickWireOptimizer);
            state.setSpeedBoosterActive(saveData.speedBoosterActive);
            state.setSpeedLimiterActive(saveData.speedLimiterActive);
            state.setWireOptimizerActive(saveData.wireOptimizerActive);
            state.setStartTimeSpeedBooster(saveData.startTimeSpeedBooster);
            state.setStartTimeSpeedLimiter(saveData.startTimeSpeedLimiter);
            state.setStartTimeWireOptimizer(saveData.startTimeWireOptimizer);
            state.setClickScrollAergia(saveData.clickScrollAergia);
            state.setClickScrollSisyphus(saveData.clickScrollSisyphus);
            state.setClickScrollEliphas(saveData.clickScrollEliphas);
            state.setScrollAergiaActive(saveData.scrollAergiaActive);
            state.setScrollSisyphusActive(saveData.scrollSisyphusActive);
            state.setScrollEliphasActive(saveData.scrollEliphasActive);
            state.setStartTimeScrollAergia(saveData.startTimeScrollAergia);
            state.setStartTimeScrollSisyphus(saveData.startTimeScrollSisyphus);
            state.setStartTimeScrollEliphas(saveData.startTimeScrollEliphas);
            state.setLastScrollAergiaUse(saveData.lastScrollAergiaUse);
            state.setScrollAergiaEffects(saveData.scrollAergiaEffects);
            state.setScrollEliphasEffects(saveData.scrollEliphasEffects);
            state.setVpnProtectedPackets(saveData.vpnProtectedPackets);
            state.setPacketOriginalTypes(saveData.packetOriginalTypes);
            state.setNumberPacketsSquare(saveData.numberPacketsSquare);
            state.setNumberPacketTriangle(saveData.numberPacketTriangle);
            state.setNumberPacketsCircle(saveData.numberPacketsCircle);
            state.setNumberPacketsConfidential4(saveData.numberPacketsConfidential4);
            state.setNumberPacketsConfidential6(saveData.numberPacketsConfidential6);
            state.setNumberPacketsBulky8(saveData.numberPacketsBulky8);
            state.setNumberPacketsBulky10(saveData.numberPacketsBulky10);
            
            System.out.println("Game loaded successfully");
            return true;
            
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load game: " + e.getMessage());
            return false;
        }
    }
    
    public void autoSave(GameState state) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAutoSave >= AUTO_SAVE_INTERVAL_MS) {
            saveGame(state);
            lastAutoSave = currentTime;
        }
    }
    
    public boolean hasSaveFile() {
        return new File(SAVE_FILE_PATH).exists();
    }
    
    private INode findNodeById(GameState state, String id) {
        for (INode node : state.getNodes()) {
            if (node.getId().equals(id)) {
                return node;
            }
        }
        return null;
    }
    private static class SaveData implements Serializable {
        private static final long serialVersionUID = 1L;
        int level;
        int packetLoss;
        boolean gameOver;
        boolean successfully;
        int userCoins;
        List<NodeData> nodes;
        List<ConnectionData> connections;
        List<PacketData> packets;
        boolean clickAtar, atar, clickAiryaman, airyaman, clickAnahita;
        boolean clickSpeedBooster, clickSpeedLimiter, clickWireOptimizer;
        boolean speedBoosterActive, speedLimiterActive, wireOptimizerActive;
        long startTimeAtar, startTimeAiryaman;
        long startTimeSpeedBooster, startTimeSpeedLimiter, startTimeWireOptimizer;
        boolean clickScrollAergia, clickScrollSisyphus, clickScrollEliphas;
        boolean scrollAergiaActive, scrollSisyphusActive, scrollEliphasActive;
        long startTimeScrollAergia, startTimeScrollSisyphus, startTimeScrollEliphas;
        long lastScrollAergiaUse;
        Map<String, Long> scrollAergiaEffects, scrollEliphasEffects;
        Map<String, Long> vpnProtectedPackets;
        Map<String, String> packetOriginalTypes;
        int numberPacketsSquare, numberPacketTriangle, numberPacketsCircle;
        int numberPacketsConfidential4, numberPacketsConfidential6;
        int numberPacketsBulky8, numberPacketsBulky10;
    }
    
    private static class NodeData implements Serializable {
        private static final long serialVersionUID = 1L;
        String id;
        int x, y;
        String[] inputShapes, outputShapes;
        String systemType;
        boolean isDamaged;
        long damageStartTime, lastAntitrojanOperation;
    }
    
    private static class ConnectionData implements Serializable {
        private static final long serialVersionUID = 1L;
        String fromNodeId, toNodeId;
        int fromPort, toPort;
        List<Point> waypoints;
        List<BendPointData> bendPoints;
        int bulkyPacketPasses;
        boolean isDestroyed;
    }
    
    private static class BendPointData implements Serializable {
        private static final long serialVersionUID = 1L;
        int x, y;
        int maxRadius;
    }
    
    private static class PacketData implements Serializable {
        private static final long serialVersionUID = 1L;
        String packetType, originalPacketType, movementBehavior, parentPacketId, parentBulkyPacketId;
        boolean isProtected, isLargePacket, isBitPacket, isReturning;
        double currentSpeed, noise, progress, acceleration, distanceTraveled;
        int segmentIndex, packetSize;
        Point displacement;
        long creationTime, lastMovementTime;
        int connectionIndex = -1;
        String vpnSourceNodeId;
        long vpnSessionId;
    }
}
