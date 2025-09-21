package network;

import Game.*;
import controller.User;
import java.util.*;

public class GameStateConverter {

    public static GameStateData toNetworkData(GameState gameState) {
        GameStateData data = new GameStateData();
        
        data.setLevel(gameState.getLevel());
        data.setGameOver(gameState.isGameOver());
        data.setSuccessfully(gameState.isSuccessfully());
        data.setPacketLoss(gameState.getPacketLoss());
        data.setLevelStartTime(gameState.getLevelStartTime());
        
        User user = gameState.getUser();
        if (user != null) {
            data.setUsername(user.getUsername());
            data.setCoins(user.getCoin());
            data.setMaxLevelPass(user.getMaxLevelPass());
        }
        
        data.setNumberPacketsSquare(gameState.getNumberPacketsSquare());
        data.setNumberPacketTriangle(gameState.getNumberPacketTriangle());
        data.setNumberPacketsCircle(gameState.getNumberPacketsCircle());
        data.setNumberPacketsConfidential4(gameState.getNumberPacketsConfidential4());
        data.setNumberPacketsConfidential6(gameState.getNumberPacketsConfidential6());
        data.setNumberPacketsBulky8(gameState.getNumberPacketsBulky8());
        data.setNumberPacketsBulky10(gameState.getNumberPacketsBulky10());
        
        List<GameStateData.NodeData> nodeDataList = new ArrayList<>();
        for (INode node : gameState.getNodes()) {
            GameStateData.NodeData nodeData = new GameStateData.NodeData();
            nodeData.setId(node.getId());
            nodeData.setX(node.getX());
            nodeData.setY(node.getY());
            nodeData.setType(node.getClass().getSimpleName());
            nodeData.setProperties(extractNodeProperties(node));
            nodeDataList.add(nodeData);
        }
        data.setNodes(nodeDataList);
        
        List<GameStateData.ConnectionData> connectionDataList = new ArrayList<>();
        for (IConnection connection : gameState.getConnections()) {
            GameStateData.ConnectionData connectionData = new GameStateData.ConnectionData();
            connectionData.setId(connection.getId());
            connectionData.setFromNodeId(connection.getFromNode().getId());
            connectionData.setToNodeId(connection.getToNode().getId());
            
            List<GameStateData.BendPointData> bendPoints = new ArrayList<>();
            for (BendPoint bendPoint : connection.getBendPoints()) {
                bendPoints.add(new GameStateData.BendPointData(bendPoint.getX(), bendPoint.getY()));
            }
            connectionData.setBendPoints(bendPoints);
            
            connectionData.setProperties(extractConnectionProperties(connection));
            connectionDataList.add(connectionData);
        }
        data.setConnections(connectionDataList);
        
        List<GameStateData.PacketData> packetDataList = new ArrayList<>();
        for (Packet packet : gameState.getPackets()) {
            GameStateData.PacketData packetData = new GameStateData.PacketData();
            packetData.setId(packet.getId());
            packetData.setX(packet.getX());
            packetData.setY(packet.getY());
            packetData.setType(packet.getClass().getSimpleName());
            packetData.setProgress(packet.getProgress());
            packetData.setConnectionId(packet.getConnection() != null ? packet.getConnection().getId() : null);
            packetData.setProperties(extractPacketProperties(packet));
            packetDataList.add(packetData);
        }
        data.setPackets(packetDataList);
        
        List<GameStateData.ShockwaveData> shockwaveDataList = new ArrayList<>();
        for (Shockwave shockwave : gameState.getShockwaves()) {
            GameStateData.ShockwaveData shockwaveData = new GameStateData.ShockwaveData();
            shockwaveData.setId(shockwave.getId());
            shockwaveData.setX(shockwave.getX());
            shockwaveData.setY(shockwave.getY());
            shockwaveData.setRadius(shockwave.getRadius());
            shockwaveData.setTimestamp(shockwave.getTimestamp());
            shockwaveData.setProperties(extractShockwaveProperties(shockwave));
            shockwaveDataList.add(shockwaveData);
        }
        data.setShockwaves(shockwaveDataList);
        
        Map<String, Boolean> powerUpStates = new HashMap<>();
        powerUpStates.put("atar", gameState.isAtar());
        powerUpStates.put("airyaman", gameState.isAiryaman());
        powerUpStates.put("anahita", gameState.isClickAnahita());
        powerUpStates.put("speedBooster", gameState.isSpeedBoosterActive());
        powerUpStates.put("speedLimiter", gameState.isSpeedLimiterActive());
        powerUpStates.put("wireOptimizer", gameState.isWireOptimizerActive());
        powerUpStates.put("scrollAergia", gameState.isScrollAergiaActive());
        powerUpStates.put("scrollSisyphus", gameState.isScrollSisyphusActive());
        powerUpStates.put("scrollEliphas", gameState.isScrollEliphasActive());
        data.setPowerUpStates(powerUpStates);
        
        Map<String, Long> powerUpTimers = new HashMap<>();
        powerUpTimers.put("atar", gameState.getStartTimeAtar());
        powerUpTimers.put("airyaman", gameState.getStartTimeAiryaman());
        powerUpTimers.put("speedBooster", gameState.getStartTimeSpeedBooster());
        powerUpTimers.put("speedLimiter", gameState.getStartTimeSpeedLimiter());
        powerUpTimers.put("wireOptimizer", gameState.getStartTimeWireOptimizer());
        powerUpTimers.put("scrollAergia", gameState.getStartTimeScrollAergia());
        powerUpTimers.put("scrollSisyphus", gameState.getStartTimeScrollSisyphus());
        powerUpTimers.put("scrollEliphas", gameState.getStartTimeScrollEliphas());
        data.setPowerUpTimers(powerUpTimers);
        
        return data;
    }

    public static GameState fromNetworkData(GameStateData data, User user) {
        GameState gameState = new GameState(data.getLevel(), user);
        
        gameState.setGameOver(data.isGameOver());
        gameState.setSuccessfully(data.isSuccessfully());
        gameState.setPacketLoss(data.getPacketLoss());
        gameState.setLevelStartTime(data.getLevelStartTime());
        
        gameState.setNumberPacketsSquare(data.getNumberPacketsSquare());
        gameState.setNumberPacketTriangle(data.getNumberPacketTriangle());
        gameState.setNumberPacketsCircle(data.getNumberPacketsCircle());
        gameState.setNumberPacketsConfidential4(data.getNumberPacketsConfidential4());
        gameState.setNumberPacketsConfidential6(data.getNumberPacketsConfidential6());
        gameState.setNumberPacketsBulky8(data.getNumberPacketsBulky8());
        gameState.setNumberPacketsBulky10(data.getNumberPacketsBulky10());

        return gameState;
    }
    
    private static Map<String, Object> extractNodeProperties(INode node) {
        Map<String, Object> properties = new HashMap<>();
        return properties;
    }
    
    private static Map<String, Object> extractConnectionProperties(IConnection connection) {
        Map<String, Object> properties = new HashMap<>();
        return properties;
    }
    
    private static Map<String, Object> extractPacketProperties(Packet packet) {
        Map<String, Object> properties = new HashMap<>();
        return properties;
    }
    
    private static Map<String, Object> extractShockwaveProperties(Shockwave shockwave) {
        Map<String, Object> properties = new HashMap<>();
        return properties;
    }
}
