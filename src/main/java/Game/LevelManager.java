package Game;

import controller.User;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

public class LevelManager {
    
    private static LevelManager instance;
    private int currentLevel;
    private User currentUser;
    private String sessionId;
    private List<IConnection> persistentConnections;
    private List<INode> persistentNodes;
    private boolean levelCompleted;
    private long levelStartTime;
    private int levelPacketCount;
    private int levelPacketLoss;
    private int levelCoinsEarned;
    
    private LevelManager() {
        this.currentLevel = 0;
        this.persistentConnections = new ArrayList<>();
        this.persistentNodes = new ArrayList<>();
        this.levelCompleted = false;
        this.sessionId = UUID.randomUUID().toString();
    }
    
    public static LevelManager getInstance() {
        if (instance == null) {
            instance = new LevelManager();
        }
        return instance;
    }
    

    public void initializeGame(User user) {
        this.currentUser = user;
        this.currentLevel = user.getMaxLevelPass();
        this.persistentConnections.clear();
        this.persistentNodes.clear();
        this.levelCompleted = false;
        this.sessionId = UUID.randomUUID().toString();
        
        GameLogger.initializeSession(sessionId, user, currentLevel);
        GameLogger.logUserAction("game_start", "New game session started", true);
    }

    public GameState startLevel(int levelNumber) {
        if (!LevelConfig.isValidLevel(levelNumber)) {
            GameLogger.logError("LevelManager", "Invalid level number: " + levelNumber, 
                              new IllegalArgumentException("Level " + levelNumber + " does not exist"));
            return null;
        }
        
        this.currentLevel = levelNumber;
        this.levelCompleted = false;
        this.levelStartTime = System.currentTimeMillis();
        this.levelPacketCount = 0;
        this.levelPacketLoss = 0;
        this.levelCoinsEarned = 0;
        
        LevelConfig.LevelData levelData = LevelConfig.getLevel(levelNumber);
        GameLogger.logLevelStart(levelNumber, levelData.getLevelName(), levelData.getDescription());
        
        GameState gameState = new GameState(levelNumber, currentUser);
        
        for (INode persistentNode : persistentNodes) {
            gameState.addNode(persistentNode);
        }
        
        for (LevelConfig.NodeConfig nodeConfig : levelData.getNodes()) {
            boolean nodeExists = false;
            for (INode existingNode : gameState.getNodes()) {
                if (existingNode.getId().equals(nodeConfig.getId())) {
                    nodeExists = true;
                    break;
                }
            }
            
            if (!nodeExists) {
                INode newNode = NodeFactory.createNodeWithShapes(
                    nodeConfig.getId(),
                    nodeConfig.getX(),
                    nodeConfig.getY(),
                    nodeConfig.getOutputShapes(),
                    nodeConfig.getInputShapes(),
                    nodeConfig.getSystemType()
                );

                if (nodeConfig.getSystemType() != null && !nodeConfig.getSystemType().isEmpty()) {
                    if (newNode instanceof Node) {
                        ((Node) newNode).setSystemType(nodeConfig.getSystemType().equals("basic") ? "normal" : nodeConfig.getSystemType());
                    }
                }
                
                gameState.addNode(newNode);
                persistentNodes.add(newNode);
                
                GameLogger.logDebug("LevelManager", "Added new node: %s at (%d, %d) with system type: %s", 
                                  nodeConfig.getId(), nodeConfig.getX(), nodeConfig.getY(), nodeConfig.getSystemType());
            }
        }
        
        for (IConnection persistentConnection : persistentConnections) {
            gameState.addConnection(persistentConnection);
        }
        
        updateGameConfigForLevel(levelData);
        
        return gameState;
    }
    
    private void updateGameConfigForLevel(LevelConfig.LevelData levelData) {
        GameLogger.logDebug("LevelManager", "Updated game config for level %d", levelData.getLevelNumber());
    }

}
