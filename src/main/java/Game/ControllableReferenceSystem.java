package Game;

import java.awt.*;
import java.util.*;
import java.util.List;

public class ControllableReferenceSystem implements INode {
    private final String id;
    private final int x, y;
    private final String[] inputShapes;
    private final String[] outputShapes;
    private final Set<Integer> connectedInputs;
    private final AmmunitionManager ammunitionManager;
    private final String ownerPlayerId;
    private final String targetPlayerId;
    private final List<AmmunitionType> availableAmmunitionTypes;
    private boolean isActive;
    private boolean isHovered;
    private Point hoverPosition;

    public ControllableReferenceSystem(String id, int x, int y, String ownerPlayerId, String targetPlayerId) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.ownerPlayerId = ownerPlayerId;
        this.targetPlayerId = targetPlayerId;
        this.ammunitionManager = new AmmunitionManager(ownerPlayerId);
        this.connectedInputs = new HashSet<>();
        this.isActive = true;
        this.isHovered = false;
        this.hoverPosition = new Point(0, 0);
        
        this.inputShapes = new String[]{"square", "triangle", "circle"};
        this.outputShapes = new String[]{"square", "triangle", "circle"};
        
        this.availableAmmunitionTypes = initializeAmmunitionTypes(ownerPlayerId);
    }

    private List<AmmunitionType> initializeAmmunitionTypes(String playerId) {
        List<AmmunitionType> types = new ArrayList<>();
        
        if ("player1".equals(playerId)) {
            types.addAll(Arrays.asList(
                AmmunitionType.SQUARE,
                AmmunitionType.TRIANGLE,
                AmmunitionType.CIRCLE,
                AmmunitionType.PROTECTED
            ));
        } else {
            types.addAll(Arrays.asList(
                AmmunitionType.CONFIDENTIAL_4,
                AmmunitionType.CONFIDENTIAL_6,
                AmmunitionType.BULKY_8,
                AmmunitionType.BULKY_10
            ));
        }
        
        return types;
    }

    public boolean firePacket(AmmunitionType type, GameState targetGameState) {
        if (!isActive || !ammunitionManager.canFirePacket(type) || !ammunitionManager.canUseSystem(id)) {
            return false;
        }

        IConnection targetConnection = findTargetConnection(targetGameState, type);
        if (targetConnection == null) {
            return false;
        }

        Packet packet = new Packet(targetConnection, type.getPacketType(), ownerPlayerId, getPlayerColor());
        targetGameState.getPackets().add(packet);
        
        ammunitionManager.firePacket(type, id);
        
        System.out.println("Player " + ownerPlayerId + " fired " + type.getDisplayName() + 
                          " into " + targetPlayerId + "'s network via system " + id);
        
        return true;
    }

    private IConnection findTargetConnection(GameState targetGameState, AmmunitionType type) {
        for (IConnection conn : targetGameState.getConnections()) {
            if (conn.getPacket() == null && !conn.isDestroyed()) {
                String[] outShapes = conn.getFromNode().getOutputShapes();
                int portIndex = conn.getFromPort();
                if (portIndex >= 0 && portIndex < outShapes.length) {
                    String portShape = outShapes[portIndex];
                    if (type.getPacketType().equals(portShape)) {
                        return conn;
                    }
                }
            }
        }
        return null;
    }

    private Color getPlayerColor() {
        return "player1".equals(ownerPlayerId) ? Color.BLUE : Color.RED;
    }

    public void update() {
        ammunitionManager.update();
    }

    public boolean canFireAnyAmmunition() {
        for (AmmunitionType type : availableAmmunitionTypes) {
            if (ammunitionManager.canFirePacket(type)) {
                return true;
            }
        }
        return false;
    }

    public List<AmmunitionType> getAvailableAmmunitionTypes() {
        return new ArrayList<>(availableAmmunitionTypes);
    }

    public List<AmmunitionManager.AmmunitionStatus> getAmmunitionStatuses() {
        List<AmmunitionManager.AmmunitionStatus> statuses = new ArrayList<>();
        for (AmmunitionType type : availableAmmunitionTypes) {
            statuses.add(ammunitionManager.getAmmunitionStatus(type));
        }
        return statuses;
    }

    public void setHovered(boolean hovered, Point position) {
        this.isHovered = hovered;
        this.hoverPosition = position != null ? new Point(position) : new Point(0, 0);
    }

    public boolean isOverSystem(int mx, int my) {
        return Math.hypot(mx - x, my - y) <= GameConfig.NODE_SIZE / 2;
    }

    @Override
    public int getX() { return x; }

    @Override
    public int getY() { return y; }

    @Override
    public String getId() { return id; }

    @Override
    public String[] getInputShapes() { return inputShapes.clone(); }

    @Override
    public String[] getOutputShapes() { return outputShapes.clone(); }

    @Override
    public Set<Integer> getConnectedInputs() { return new HashSet<>(connectedInputs); }

    @Override
    public Point getPortPosition(String portType, int portIndex) {
        int px, py;
        if (portType.equals("input")) {
            px = x - GameConfig.NODE_SIZE / 2 - 10;
            py = y - GameConfig.NODE_SIZE / 2 + (portIndex + 1) * GameConfig.NODE_SIZE / (inputShapes.length + 1);
        } else {
            px = x + GameConfig.NODE_SIZE / 2 + 10;
            py = y - GameConfig.NODE_SIZE / 2 + (portIndex + 1) * GameConfig.NODE_SIZE / (outputShapes.length + 1);
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
        return isOverSystem(mx, my);
    }

    @Override
    public void setPosition(int x, int y) {
    }

    @Override
    public boolean isDamaged() { return !isActive; }

    @Override
    public void setDamaged(boolean damaged) { this.isActive = !damaged; }

    @Override
    public boolean canOperate() { return isActive; }

    @Override
    public boolean isSpySystem() { return false; }

    @Override
    public boolean isSabotageSystem() { return false; }

    @Override
    public boolean isVpnSystem() { return false; }

    @Override
    public boolean isAntitrojanSystem() { return false; }

    @Override
    public boolean isDistributeSystem() { return false; }

    @Override
    public boolean isMergeSystem() { return false; }

    @Override
    public boolean isMaliciousSystem() { return false; }

    @Override
    public boolean canAntitrojanOperate() { return false; }

    @Override
    public void setLastAntitrojanOperation(long time) { }

    @Override
    public String getSystemType() { return GameConfig.CONTROLLABLE_SYSTEM_TYPE; }

    @Override
    public boolean isControllableSystem() { return true; }

    @Override
    public boolean isUncontrollableSystem() { return false; }

    public String getOwnerPlayerId() { return ownerPlayerId; }
    public String getTargetPlayerId() { return targetPlayerId; }
    public boolean isActive() { return isActive; }
    public boolean isHovered() { return isHovered; }
    public Point getHoverPosition() { return new Point(hoverPosition); }
    public AmmunitionManager getAmmunitionManager() { return ammunitionManager; }
}
