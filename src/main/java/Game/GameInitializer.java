package Game;
import client.ClientMain;
import controller.AudioManager;
import controller.User;
import view.Window;

public class GameInitializer {
    public void newGame(ClientMain client , boolean onlineMod , int level, User user) {
        AudioManager.playSound("themeSong");
        LevelManager levelManager = LevelManager.getInstance();
        levelManager.initializeGame(user);
        GameState state = levelManager.startLevel(level);
        if (state == null) {
            GameLogger.logError("GameInitializer", "Failed to start level " + level, 
                              new RuntimeException("Invalid level or initialization failed"));
            return;
        }
        IWireLengthManager wireLengthManager = new WireLengthManager(state.getConnections());
        IPacketManager packetManager = new PacketManager(state);
        IConnectionValidator validator = new ConnectionValidator();
        GamePanel gamePanel = new GamePanel(client , onlineMod , state, wireLengthManager, packetManager, validator);
        Window.getMainFrame().setContentPane(gamePanel);
        GameLogger.logDebug("GameInitializer", "Game initialized for level %d with user %s", 
                           level, user.getUsername());
    }
}