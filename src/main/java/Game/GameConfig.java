package Game;

public class GameConfig {
    static final double MAX_WIRE_LENGTH = 2000.0;
    static final double LOW_WIRE_THRESHOLD = 10.0;
    static final int[] NUMBER_PACKET = {20, 30, 40, 50, 60};
    static final int WIRE_BAR_MAX_WIDTH = 100;
    static final int WIRE_BAR_HEIGHT = 10;
    static final int WIRE_BAR_X_OFFSET = 140;
    static final int WIRE_BAR_Y = 15;
    public static final int NODE_SIZE = 50;
    static final double PACKET_SPEED = 0.02;
    static final int ANIMATION_TICK_MS = 16;
    static final int BUTTON_WIDTH = 80;
    static final int BUTTON_HEIGHT = 30;
    static final int RUN_BUTTON_X = 10;
    static final int BUTTON_Y = 10;
    static final double SHOCKWAVE_MAX_RADIUS = 100.0;
    static final long SHOCKWAVE_LIFESPAN_MS = 500;
    static final double SHOCKWAVE_FORCE = 5.0;
    static final double COLLISION_DISTANCE = 2.0;
    static final double PACKET_LOSS_DISTANCE = 10.0;
    static final double MAX_DISPLACEMENT = 5.0;
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    
    static final double LONG_WIRE_THRESHOLD = 300.0; // Wire length threshold for acceleration
    static final double WIRE_ACCELERATION_FACTOR = 2.0; // Acceleration factor for long wires
    static final double MAX_PACKET_SPEED = 0.1; // Maximum allowed packet speed
    static final long SYSTEM_DAMAGE_DURATION_MS = 5000; // Duration system stays damaged
    static final double TROJAN_CONVERSION_PROBABILITY = 0.3; // Probability of trojan conversion
    static final double ANTITROJAN_DETECTION_RADIUS = 100.0; // Detection radius for anti-trojan systems
    static final long ANTITROJAN_COOLDOWN_MS = 3000; // Cooldown after successful operation
    
    static final long SPEED_BOOSTER_DURATION_MS = 8000; // Duration of speed booster effect
    static final long SPEED_LIMITER_DURATION_MS = 10000; // Duration of speed limiter effect
    static final long WIRE_OPTIMIZER_DURATION_MS = 12000; // Duration of wire optimizer effect
    static final double SPEED_BOOSTER_FACTOR = 1.5; // Speed boost multiplier
    static final double SPEED_LIMITER_FACTOR = 0.7; // Speed limit multiplier
    static final double WIRE_OPTIMIZER_FACTOR = 0.8; // Wire length reduction factor
    
    static final int SQUARE_PACKET_SIZE = 2; // Square packet size in units
    static final int TRIANGLE_PACKET_SIZE = 3; // Triangle packet size in units
    static final int CIRCLE_PACKET_SIZE = 1; // Circle packet size in units
    static final int PROTECTED_PACKET_SIZE_MULTIPLIER = 2; // Protected packet size multiplier
    
    static final int SQUARE_PACKET_COINS = 2; // Coins added by square packet
    static final int TRIANGLE_PACKET_COINS = 3; // Coins added by triangle packet
    static final int CIRCLE_PACKET_COINS = 1; // Coins added by circle packet
    static final int PROTECTED_PACKET_COINS = 5; // Coins added by protected packet
    
    static final double COMPATIBLE_PORT_SPEED_FACTOR = 0.5; // Speed factor for compatible ports
    static final double INCOMPATIBLE_PORT_SPEED_FACTOR = 2.0; // Speed factor for incompatible ports
    static final double CIRCLE_ACCELERATION_FACTOR = 1.2; // Acceleration factor for circle packets
    static final double CIRCLE_DECELERATION_FACTOR = 0.8; // Deceleration factor for circle packets on incompatible ports
    
    static final int LARGE_PACKET_THRESHOLD = 5; // Size threshold for large packets
    static final int MAX_BIT_PACKETS = 8; // Maximum number of bit packets from large packet
    static final long PACKET_TIMEOUT_MS = 10000; // Timeout for packets on wires
    
    static final String DISTRIBUTE_SYSTEM_TYPE = "distribute";
    static final String MERGE_SYSTEM_TYPE = "merge";
    static final String MALICIOUS_SYSTEM_TYPE = "malicious";
    static final String SPY_SYSTEM_TYPE = "spy";
    
    static final int CONFIDENTIAL_PACKET_SIZE_4 = 4; // Size 4 confidential packet
    static final int CONFIDENTIAL_PACKET_SIZE_6 = 6; // Size 6 confidential packet (VPN-generated)
    static final int CONFIDENTIAL_PACKET_4_COINS = 3; // Coins added by size 4 confidential packet
    static final int CONFIDENTIAL_PACKET_6_COINS = 4; // Coins added by size 6 confidential packet
    static final double CONFIDENTIAL_SPEED_REDUCTION_FACTOR = 0.3; // Speed reduction when another packet is in target system
    static final double CONFIDENTIAL_DISTANCE_MAINTENANCE = 50.0; // Distance to maintain from other packets
    
    static final int BULKY_PACKET_SIZE_8 = 8; // Size 8 bulky packet
    static final int BULKY_PACKET_SIZE_10 = 10; // Size 10 bulky packet
    static final int BULKY_PACKET_8_COINS = 8; // Coins added by size 8 bulky packet
    static final int BULKY_PACKET_10_COINS = 10; // Coins added by size 10 bulky packet
    static final int MAX_BULKY_PASSES_PER_WIRE = 3; // Maximum times a wire can handle bulky packets
    static final double BULKY_PACKET_8_ACCELERATION = 0.001; // Acceleration for size 8 bulky packets on curves
    static final double BULKY_PACKET_10_DEVIATION_DISTANCE = 20.0; // Distance for size 10 packet deviation
    static final double BULKY_PACKET_10_DEVIATION_AMOUNT = 5.0; // Amount of deviation for size 10 packets

    public static final int SCROLL_AERGIA_COST = 10; // Cost of Scroll of Aergia
    public static final int SCROLL_SISYPHUS_COST = 15; // Cost of Scroll of Sisyphus
    public static final int SCROLL_ELIPHAS_COST = 20; // Cost of Scroll of Eliphas
    public static final long SCROLL_AERGIA_DURATION_MS = 20000; // Duration of Scroll of Aergia effect (20 seconds)
    public static final long SCROLL_ELIPHAS_DURATION_MS = 30000; // Duration of Scroll of Eliphas effect (30 seconds)
    public static final long SCROLL_AERGIA_COOLDOWN_MS = 30000; // Cooldown for Scroll of Aergia (30 seconds)
    public static final double SCROLL_AERGIA_SPEED_FACTOR = 0.0; // Speed factor for Scroll of Aergia (0 = no movement)
    public static final int SCROLL_SISYPHUS_RADIUS = 100; // Radius for Scroll of Sisyphus system movement
    public static final double SCROLL_ELIPHAS_RESTORATION_SPEED = 0.05; // Speed of center of mass restoration

    public static final String CONTROLLABLE_SYSTEM_TYPE = "controllable";
    public static final String UNCONTROLLABLE_SYSTEM_TYPE = "uncontrollable";
    public static final int MAX_AMMUNITION_PER_TYPE = 10; // Maximum ammunition per packet type
    public static final long SYSTEM_COOLDOWN_MS = 5000; // System cooldown duration (5 seconds)
    public static final long PACKET_COOLDOWN_MS = 3000; // Packet cooldown duration (3 seconds)
    public static final int AMMUNITION_PANEL_WIDTH = 200; // Ammunition panel width
    public static final int AMMUNITION_PANEL_HEIGHT = 150; // Ammunition panel height
    public static final int AMMUNITION_ITEM_SIZE = 30; // Size of ammunition item icons
    public static final int AMMUNITION_ITEM_SPACING = 5; // Spacing between ammunition items
    public static final long COOLDOWN_VISUAL_EFFECT_DURATION_MS = 500; // Duration of cooldown completion visual effect
    public static final double OPPONENT_NETWORK_ALPHA = 0.3; // Alpha value for opponent network display
    public static final long TEMPORAL_PROGRESS_DURATION_MS = 10000; // Duration of temporal progress effect
    
    public static final double PACKET_LOSS_PENALTY = 1.5; // Penalty multiplier for packet loss
    public static final long FEEDBACK_LOOP_COOLDOWN_MS = 2000; // Cooldown for feedback loop generation
    public static final int MAX_FEEDBACK_PACKETS_PER_WAVE = 3; // Maximum feedback packets per wave
    public static final long WAVE_GENERATION_INTERVAL_MS = 1000; // Minimum interval between waves
    public static final double CONFIDENTIAL_PACKET_SPAWN_PROBABILITY = 0.3; // Probability of spawning confidential packets
    public static final long TROJAN_CONVERSION_COOLDOWN_MS = 5000; // Cooldown for trojan conversion

}
