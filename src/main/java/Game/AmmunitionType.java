package Game;

import java.awt.Color;


public enum AmmunitionType {
    SQUARE("square", "Square Packet", Color.BLUE, 2),
    TRIANGLE("triangle", "Triangle Packet", Color.RED, 3),
    CIRCLE("circle", "Circle Packet", Color.GREEN, 1),
    PROTECTED("protected", "Protected Packet", Color.YELLOW, 5),
    CONFIDENTIAL_4("confidential_4", "Confidential 4", Color.MAGENTA, 3),
    CONFIDENTIAL_6("confidential_6", "Confidential 6", Color.CYAN, 4),
    BULKY_8("bulky_8", "Bulky 8", Color.ORANGE, 8),
    BULKY_10("bulky_10", "Bulky 10", Color.PINK, 10);

    private final String packetType;
    private final String displayName;
    private final Color color;
    private final int coinReward;

    AmmunitionType(String packetType, String displayName, Color color, int coinReward) {
        this.packetType = packetType;
        this.displayName = displayName;
        this.color = color;
        this.coinReward = coinReward;
    }

    public String getPacketType() {
        return packetType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Color getColor() {
        return color;
    }

    public int getCoinReward() {
        return coinReward;
    }

    public static AmmunitionType fromPacketType(String packetType) {
        for (AmmunitionType type : values()) {
            if (type.packetType.equals(packetType)) {
                return type;
            }
        }
        return SQUARE;
    }
}
