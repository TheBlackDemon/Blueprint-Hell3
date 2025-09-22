package Game;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class PacketTypeRegistry {
    private static final String PACKET_PACKAGE = "main.java.Game";
    private static final String AMMUNITION_TYPE_CLASS = "AmmunitionType";
    
    private final Map<String, PacketTypeInfo> packetTypes;
    private final Map<String, Class<?>> packetClasses;
    private final Map<String, Method> packetMethods;
    private final Random random;
    
    public PacketTypeRegistry() {
        this.packetTypes = new ConcurrentHashMap<>();
        this.packetClasses = new ConcurrentHashMap<>();
        this.packetMethods = new ConcurrentHashMap<>();
        this.random = new Random();
        
        initializePacketTypes();
    }
    
    /**
     * Initialize packet types using reflection
     */
    private void initializePacketTypes() {
        try {
            discoverPacketTypesFromEnum();
            
            discoverPacketTypesFromPacketClass();
            
            System.out.println("Discovered " + packetTypes.size() + " packet types using reflection");
        } catch (Exception e) {
            System.err.println("Error initializing packet types: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private Class<?> loadAmmunitionTypeClass() throws ClassNotFoundException {
        String[] possiblePackages = {
                "Game",
                "main.java.Game",
                "main.Game"
        };

        for (String pkg : possiblePackages) {
            try {
                String fullClassName = pkg + "." + AMMUNITION_TYPE_CLASS;
                System.out.println("Trying: " + fullClassName);
                return Class.forName(fullClassName);
            } catch (ClassNotFoundException e) {
                System.out.println("Not found: " + pkg + "." + AMMUNITION_TYPE_CLASS);
            }
        }

        throw new ClassNotFoundException("Cannot find AmmunitionType in any package");
    }

    private void discoverPacketTypesFromEnum() throws Exception {


        Class<?> ammunitionTypeClass = AmmunitionType.class;

        Object[] enumConstants = ammunitionTypeClass.getEnumConstants();
        
        for (Object enumConstant : enumConstants) {
            if (enumConstant != null) {
                Method getPacketTypeMethod = ammunitionTypeClass.getMethod("getPacketType");
                String packetType = (String) getPacketTypeMethod.invoke(enumConstant);
                
                Method getDisplayNameMethod = ammunitionTypeClass.getMethod("getDisplayName");
                String displayName = (String) getDisplayNameMethod.invoke(enumConstant);
                
                Method getColorMethod = ammunitionTypeClass.getMethod("getColor");
                Object color = getColorMethod.invoke(enumConstant);
                
                Method getCoinRewardMethod = ammunitionTypeClass.getMethod("getCoinReward");
                Integer coinReward = (Integer) getCoinRewardMethod.invoke(enumConstant);
                
                PacketTypeInfo info = new PacketTypeInfo(packetType, displayName, color, coinReward);
                packetTypes.put(packetType, info);
                
                System.out.println("Discovered packet type: " + packetType + " (" + displayName + ")");
            }
        }
    }


    private void discoverPacketTypesFromPacketClass() throws Exception {
        Class<?> packetClass = Packet.class;

        Field[] fields = packetClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().toLowerCase().contains("packet") &&
                    field.getType() == String.class) {

                field.setAccessible(true);

                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                    String packetType = (String) field.get(null);

                    if (packetType != null && !packetType.trim().isEmpty()) {
                        if (!packetTypes.containsKey(packetType)) {
                            PacketTypeInfo info = new PacketTypeInfo(packetType, packetType, null, 1);
                            packetTypes.put(packetType, info);
                            System.out.println("Discovered packet type: " + packetType);
                        }
                    } else {
                        System.out.println("Skipping null/empty field: " + field.getName());
                    }
                } else {
                    System.out.println("Skipping non-static field: " + field.getName());
                }
            }
        }
    }
    

    public Collection<PacketTypeInfo> getAllPacketTypes() {
        return packetTypes.values();
    }
    

    public PacketTypeInfo getPacketTypeInfo(String packetType) {
        return packetTypes.get(packetType);
    }

    public List<PacketTypeInfo> getPacketTypesForPlayer(String playerId) {
        List<PacketTypeInfo> playerPackets = new ArrayList<>();
        
        for (PacketTypeInfo info : packetTypes.values()) {
            if (isPacketTypeForPlayer(info.getPacketType(), playerId)) {
                playerPackets.add(info);
            }
        }
        
        return playerPackets;
    }
    

    private boolean isPacketTypeForPlayer(String packetType, String playerId) {
        if ("player1".equals(playerId)) {
            return isType1Packet(packetType);
        } else if ("player2".equals(playerId)) {
            return isType2Packet(packetType);
        }
        return false;
    }

    private boolean isType1Packet(String packetType) {
        return "square".equals(packetType) || "triangle".equals(packetType) || 
               "circle".equals(packetType) || "protected".equals(packetType);
    }

    private boolean isType2Packet(String packetType) {
        return "confidential_4".equals(packetType) || "confidential_6".equals(packetType) ||
               "bulky_8".equals(packetType) || "bulky_10".equals(packetType);
    }

    public String getRandomPacketTypeForPlayer(String playerId) {
        List<PacketTypeInfo> playerPackets = getPacketTypesForPlayer(playerId);
        if (playerPackets.isEmpty()) {
            return "square";
        }
        
        PacketTypeInfo randomPacket = playerPackets.get(random.nextInt(playerPackets.size()));
        return randomPacket.getPacketType();
    }

    public String getCounterPacketType(String originalPacketType) {
        if (isType1Packet(originalPacketType)) {
            return getType2Equivalent(originalPacketType);
        } else if (isType2Packet(originalPacketType)) {
            return getType1Equivalent(originalPacketType);
        }
        return originalPacketType;
    }

    private String getType2Equivalent(String type1Packet) {
        switch (type1Packet) {
            case "square":
                return "confidential_4";
            case "triangle":
                return "confidential_6";
            case "circle":
                return "bulky_8";
            case "protected":
                return "bulky_10";
            default:
                return "confidential_4";
        }
    }

    private String getType1Equivalent(String type2Packet) {
        switch (type2Packet) {
            case "confidential_4":
                return "square";
            case "confidential_6":
                return "triangle";
            case "bulky_8":
                return "circle";
            case "bulky_10":
                return "protected";
            default:
                return "square";
        }
    }

    public int getPacketSpawnPriority(String packetType) {
        PacketTypeInfo info = packetTypes.get(packetType);
        if (info == null) return 999;
        
        if (packetType.contains("bulky_10")) return 1;
        if (packetType.contains("bulky_8")) return 2;
        if (packetType.contains("confidential_6")) return 3;
        if (packetType.contains("confidential_4")) return 4;
        if (packetType.contains("protected")) return 5;
        if (packetType.contains("circle")) return 6;
        if (packetType.contains("triangle")) return 7;
        if (packetType.contains("square")) return 8;
        
        return 9;
    }

    public long getPacketCooldown(String packetType) {
        PacketTypeInfo info = packetTypes.get(packetType);
        if (info == null) return 3000; // Default 3 seconds
        
        if (packetType.contains("bulky_10")) return 12000;
        if (packetType.contains("bulky_8")) return 10000;
        if (packetType.contains("confidential_6")) return 7000;
        if (packetType.contains("confidential_4")) return 6000;
        if (packetType.contains("protected")) return 8000;
        if (packetType.contains("circle")) return 3000;
        if (packetType.contains("triangle")) return 4000;
        if (packetType.contains("square")) return 2000;
        
        return 3000;
    }

    public static class PacketTypeInfo {
        private final String packetType;
        private final String displayName;
        private final Object color;
        private final Integer coinReward;
        
        public PacketTypeInfo(String packetType, String displayName, Object color, Integer coinReward) {
            this.packetType = packetType;
            this.displayName = displayName;
            this.color = color;
            this.coinReward = coinReward;
        }
        
        public String getPacketType() { return packetType; }
        public String getDisplayName() { return displayName; }
        public Object getColor() { return color; }
        public Integer getCoinReward() { return coinReward; }
        
        @Override
        public String toString() {
            return displayName + " (" + packetType + ")";
        }
    }
}
