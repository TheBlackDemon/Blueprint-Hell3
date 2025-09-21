package Game;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class Node implements INode {
    private int x, y;
    private final String id;
    private final String[] inputShapes;
    private final String[] outputShapes;
    private final int size = GameConfig.NODE_SIZE;
    private final Set<Integer> connectedInputs;
    private String systemType = "normal"; // normal, spy, sabotage, vpn, antitrojan
    private boolean isDamaged = false;
    private long damageStartTime = 0;
    private long lastAntitrojanOperation = 0;

    public Node(int x, int y, String id, String[] inputShapes, String[] outputShapes) {
        this.x = x;
        this.y = y;
        this.id = id;
        this.inputShapes = inputShapes;
        this.outputShapes = outputShapes;
        this.connectedInputs = new HashSet<>();
    }

    public Node(int x, int y, String id, String[] inputShapes, String[] outputShapes, String systemType) {
        this.x = x;
        this.y = y;
        this.id = id;
        this.inputShapes = inputShapes;
        this.outputShapes = outputShapes;
        this.systemType = systemType;
        this.connectedInputs = new HashSet<>();
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String[] getInputShapes() {
        return inputShapes;
    }

    @Override
    public String[] getOutputShapes() {
        return outputShapes;
    }

    @Override
    public Set<Integer> getConnectedInputs() {
        return connectedInputs;
    }

    @Override
    public Point getPortPosition(String portType, int portIndex) {
        int px, py;
        if (portType.equals("input")) {
            px = x - size / 2 - 10;
            py = y - size / 2 + (portIndex + 1) * size / (inputShapes.length + 1);
        } else {
            px = x + size / 2 + 10;
            py = y - size / 2 + (portIndex + 1) * size / (outputShapes.length + 1);
        }
        return new Point(px, py);
    }

    @Override
    public boolean isOverPort(int mx, int my, String portType, int portIndex) {
        Point pos = getPortPosition(portType, portIndex);
        return Math.hypot(mx - pos.x, my - pos.y) < 8;
    }

    @Override
    public boolean isOverNode(int mx, int my) {
        return mx >= x - size / 2 && mx <= x + size / 2 && my >= y - size / 2 && my <= y + size / 2;
    }

    @Override
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public String getSystemType() {
        return systemType;
    }

    public void setSystemType(String systemType) {
        this.systemType = systemType;
    }

    public boolean isDamaged() {
        return isDamaged;
    }

    public void setDamaged(boolean damaged) {
        this.isDamaged = damaged;
        if (damaged) {
            this.damageStartTime = System.currentTimeMillis();
        }
    }

    public long getDamageStartTime() {
        return damageStartTime;
    }

    public void setDamageStartTime(long damageStartTime) {
        this.damageStartTime = damageStartTime;
    }

    public long getLastAntitrojanOperation() {
        return lastAntitrojanOperation;
    }

    public void setLastAntitrojanOperation(long lastAntitrojanOperation) {
        this.lastAntitrojanOperation = lastAntitrojanOperation;
    }

    public boolean isSpySystem() {
        return "spy".equals(systemType);
    }

    public boolean isSabotageSystem() {
        return "sabotage".equals(systemType);
    }

    public boolean isVpnSystem() {
        return "vpn".equals(systemType);
    }

    public boolean isAntitrojanSystem() {
        return "antitrojan".equals(systemType);
    }

    public boolean isNormalSystem() {
        return "normal".equals(systemType);
    }
    
    public boolean isDistributeSystem() {
        return GameConfig.DISTRIBUTE_SYSTEM_TYPE.equals(systemType);
    }
    
    public boolean isMergeSystem() {
        return GameConfig.MERGE_SYSTEM_TYPE.equals(systemType);
    }
    
    public boolean isMaliciousSystem() {
        return GameConfig.MALICIOUS_SYSTEM_TYPE.equals(systemType);
    }

    public boolean canOperate() {
        if (isDamaged) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - damageStartTime >= GameConfig.SYSTEM_DAMAGE_DURATION_MS) {
                isDamaged = false;
                damageStartTime = 0;
                return true;
            }
            return false;
        }
        return true;
    }

    public boolean canAntitrojanOperate() {
        if (!isAntitrojanSystem() || isDamaged) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        return currentTime - lastAntitrojanOperation >= GameConfig.ANTITROJAN_COOLDOWN_MS;
    }

    @Override
    public boolean isControllableSystem() {
        return GameConfig.CONTROLLABLE_SYSTEM_TYPE.equals(systemType);
    }

    @Override
    public boolean isUncontrollableSystem() {
        return GameConfig.UNCONTROLLABLE_SYSTEM_TYPE.equals(systemType);
    }
}
