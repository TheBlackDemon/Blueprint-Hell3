package Game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import controller.User;
import java.util.Map;
import java.util.HashMap;

public class GameLogger {
    
    private static final Logger logger = LoggerFactory.getLogger(GameLogger.class);
    private static final Logger packetLogger = LoggerFactory.getLogger("packet");
    private static final Logger collisionLogger = LoggerFactory.getLogger("collision");
    private static final Logger shopLogger = LoggerFactory.getLogger("shop");
    private static final Logger systemLogger = LoggerFactory.getLogger("system");
    private static final Logger levelLogger = LoggerFactory.getLogger("level");
    private static final Logger userLogger = LoggerFactory.getLogger("user");
    private static String currentSessionId;
    private static long sessionStartTime;
    private static int currentLevel;
    private static User currentUser;
    private static Map<String, Integer> eventCounts = new HashMap<>();
    private static Map<String, Long> eventTimestamps = new HashMap<>();
    
    public static void initializeSession(String sessionId, User user, int level) {
        currentSessionId = sessionId;
        currentUser = user;
        currentLevel = level;
        sessionStartTime = System.currentTimeMillis();
        logger.info("Game session initialized - SessionID: {}, User: {}, Level: {}", 
                   sessionId, user.getUsername(), level);
        levelLogger.info("Level {} started - User: {}", level, user.getUsername());
        eventCounts.clear();
        eventTimestamps.clear();
    }
    
    public static void logPacketSpawn(String packetType, String connectionId, String fromNode, String toNode) {
        packetLogger.info("Packet spawned - Type: {}, Connection: {}->{}, ConnectionID: {}", 
                         packetType, fromNode, toNode, connectionId);
        incrementEventCount("packet_spawn_" + packetType);
    }
    
    public static void logPacketMovement(String packetId, String packetType, double x, double y, 
                                       double speed, String connectionId) {
        packetLogger.debug("Packet movement - ID: {}, Type: {}, Position: ({}, {}), Speed: {}, Connection: {}", 
                          packetId, packetType, String.format("%.2f", x), String.format("%.2f", y), 
                          String.format("%.4f", speed), connectionId);
    }
    
    public static void logPacketArrival(String packetId, String packetType, String nodeId, 
                                      boolean successful, long travelTime) {
        if (successful) {
            packetLogger.info("Packet arrived successfully - ID: {}, Type: {}, Node: {}, TravelTime: {}ms", 
                             packetId, packetType, nodeId, travelTime);
            incrementEventCount("packet_arrival_" + packetType);
        } else {
            packetLogger.warn("Packet failed to arrive - ID: {}, Type: {}, Node: {}, TravelTime: {}ms", 
                             packetId, packetType, nodeId, travelTime);
            incrementEventCount("packet_failure_" + packetType);
        }
    }
    
    public static void logPacketTimeout(String packetId, String packetType, String connectionId, 
                                      long timeoutDuration) {
        packetLogger.warn("Packet timeout - ID: {}, Type: {}, Connection: {}, Duration: {}ms", 
                         packetId, packetType, connectionId, timeoutDuration);
        incrementEventCount("packet_timeout");
    }
    
    public static void logPacketCollision(String packet1Id, String packet1Type, String packet2Id, 
                                        String packet2Type, double x, double y, double distance) {
        collisionLogger.error("Packet collision - Packets: {} ({}) and {} ({}), Position: ({}, {}), Distance: {}", 
                              packet1Id, packet1Type, packet2Id, packet2Type, 
                              String.format("%.2f", x), String.format("%.2f", y), String.format("%.2f", distance));
        incrementEventCount("packet_collision");
    }
    
    public static void logPacketLoss(int totalLoss, int currentLoss, int maxAllowed) {
        if (currentLoss > maxAllowed * 0.8) {
            collisionLogger.error("High packet loss - Total: {}, Current: {}, MaxAllowed: {}", 
                                  totalLoss, currentLoss, maxAllowed);
        } else if (currentLoss > maxAllowed * 0.5) {
            collisionLogger.warn("Moderate packet loss - Total: {}, Current: {}, MaxAllowed: {}", 
                                totalLoss, currentLoss, maxAllowed);
        } else {
            collisionLogger.info("Packet loss - Total: {}, Current: {}, MaxAllowed: {}", 
                                totalLoss, currentLoss, maxAllowed);
        }
        incrementEventCount("packet_loss");
    }
    
    public static void logConnectionCreated(String connectionId, String fromNode, String toNode, 
                                          double length, int fromPort, int toPort) {
        systemLogger.info("Connection created - ID: {}, From: {}:{} -> To: {}:{}, Length: {}", 
                         connectionId, fromNode, fromPort, toNode, toPort, String.format("%.2f", length));
        incrementEventCount("connection_created");
    }
    
    public static void logShopPurchase(String itemName, int cost, int userCoins, boolean successful) {
        if (successful) {
            shopLogger.info("Shop purchase successful - Item: {}, Cost: {}, UserCoins: {}", 
                           itemName, cost, userCoins);
            incrementEventCount("shop_purchase_" + itemName);
        } else {
            shopLogger.warn("Shop purchase failed - Item: {}, Cost: {}, UserCoins: {}, Reason: Insufficient funds", 
                           itemName, cost, userCoins);
            incrementEventCount("shop_purchase_failed_" + itemName);
        }
    }
    
    public static void logLevelStart(int level, String levelName, String description) {
        levelLogger.info("Level started - Number: {}, Name: {}, Description: {}", 
                        level, levelName, description);
        incrementEventCount("level_start");
    }
    
    public static void logLevelComplete(int level, String levelName, long completionTime, 
                                      int packetsDelivered, int packetLoss, int coinsEarned) {
        levelLogger.info("Level completed - Number: {}, Name: {}, CompletionTime: {}ms, PacketsDelivered: {}, PacketLoss: {}, CoinsEarned: {}", 
                        level, levelName, completionTime, packetsDelivered, packetLoss, coinsEarned);
        incrementEventCount("level_complete");
    }
    
    public static void logLevelFailed(int level, String levelName, String reason, 
                                    long failureTime, int packetsDelivered, int packetLoss) {
        levelLogger.warn("Level failed - Number: {}, Name: {}, Reason: {}, FailureTime: {}ms, PacketsDelivered: {}, PacketLoss: {}", 
                        level, levelName, reason, failureTime, packetsDelivered, packetLoss);
        incrementEventCount("level_failed");
    }
    
    public static void logGameOver(String reason, int finalLevel, int totalPackets, 
                                 int totalPacketLoss, int totalCoins) {
        logger.error("Game Over - Reason: {}, FinalLevel: {}, TotalPackets: {}, TotalPacketLoss: {}, TotalCoins: {}", 
                     reason, finalLevel, totalPackets, totalPacketLoss, totalCoins);
        incrementEventCount("game_over");
    }
    
    public static void logGameWin(int finalLevel, long totalTime, int totalPackets, 
                                int totalPacketLoss, int totalCoins) {
        logger.info("Game Won - FinalLevel: {}, TotalTime: {}ms, TotalPackets: {}, TotalPacketLoss: {}, TotalCoins: {}", 
                   finalLevel, totalTime, totalPackets, totalPacketLoss, totalCoins);
        incrementEventCount("game_win");
    }
    
    public static void logUserAction(String action, String details, boolean successful) {
        if (successful) {
            userLogger.info("User action successful - Action: {}, Details: {}", action, details);
        } else {
            userLogger.warn("User action failed - Action: {}, Details: {}", action, details);
        }
        incrementEventCount("user_action_" + action);
    }
    
    public static void logError(String component, String error, Exception exception) {
        logger.error("Error in {} - Error: {}", component, error, exception);
        incrementEventCount("error_" + component);
    }
    
    public static void logDebug(String component, String message, Object... args) {
        logger.debug("Debug [{}] - {}", component, String.format(message, args));
    }
    
    private static void incrementEventCount(String eventType) {
        eventCounts.put(eventType, eventCounts.getOrDefault(eventType, 0) + 1);
        eventTimestamps.put(eventType, System.currentTimeMillis());
    }

    public static void logSessionStatistics() {
        long sessionDuration = System.currentTimeMillis() - sessionStartTime;
        logger.info("Session statistics - Duration: {}ms, Events: {}", sessionDuration, eventCounts);
        eventCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(10)
            .forEach(entry -> logger.info("Top event - {}: {}", entry.getKey(), entry.getValue()));
    }
    
    public static void endSession() {
        logSessionStatistics();
        logger.info("Game session ended - SessionID: {}", currentSessionId);
    }

}
