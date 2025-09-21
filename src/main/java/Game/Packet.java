package Game;

import java.awt.*;
import java.util.List;
import java.util.Random;

public class Packet {
    private IConnection currentConnection;
    private int segmentIndex;
    private double progress;
    private Point displacement;
    private String packetType;
    private double noise = 0;
    private double currentSpeed = GameConfig.PACKET_SPEED;
    private String originalPacketType;
    private boolean isProtected = false;
    
    private int packetSize;
    private long creationTime;
    private long lastMovementTime;
    private boolean isLargePacket = false;
    private String parentPacketId;
    private boolean isBitPacket = false;
    private boolean isReturning = false;
    private double acceleration = 0;
    private String movementBehavior;
    private double distanceTraveled = 0;
    private String parentBulkyPacketId;
    private String vpnSourceNodeId;
    private String playerId;
    private Color playerColor;

    public Packet(IConnection startConnection, String packetType) {
        this.currentConnection = startConnection;
        this.packetType = packetType;
        this.originalPacketType = packetType;
        this.segmentIndex = 0;
        this.progress = 0.0;
        this.displacement = new Point(0, 0);
        this.creationTime = System.currentTimeMillis();
        this.lastMovementTime = this.creationTime;
        this.currentConnection.setPacket(this);
        this.playerId = "default";
        this.playerColor = Color.BLUE;
        initializePacketSize();
        if ("protected".equals(packetType)) {
            initializeProtectedPacketBehavior();
        }
        if (isBulkyPacket()) {
            initializeBulkyPacketProperties();
        }
        System.out.println("Packet created on connection from " + startConnection.getFromNode().getId() + " to " + startConnection.getToNode().getId() + 
                          ", type: " + packetType + ", size: " + packetSize);
    }
    
    public Packet(IConnection startConnection, String packetType, String playerId, Color playerColor) {
        this(startConnection, packetType);
        this.playerId = playerId;
        this.playerColor = playerColor;
    }

    public void updateForward(GameState state, List<Packet> packets) {
        double oldProgress = progress;
        if (System.currentTimeMillis() - lastMovementTime > 1000) { // Log every second
            GameLogger.logPacketMovement(toString(), packetType, getPosition().x, getPosition().y, 
                                       currentSpeed, currentConnection.toString());
            lastMovementTime = System.currentTimeMillis();
        }
        if (isReturning) {
            updateBackward(state, packets);
            return;
        }
        updateSpeed();
        
        progress += currentSpeed;
        if (progress > 1.0) progress = 1.0;

        if (progress >= 1.0) {
            progress = 0.0;
            segmentIndex++;
            java.util.List<Point> path = currentConnection.getDetailedPath();
            if (segmentIndex >= path.size() - 1) {
                String fromNodeId = currentConnection.getFromNode().getId();
                String toNodeId = currentConnection.getToNode().getId();
                long travelTime = System.currentTimeMillis() - creationTime;
                
                GameLogger.logPacketArrival(toString(), packetType, toNodeId, true, travelTime);
                System.out.println("Packet reached node " + toNodeId + " from " + fromNodeId);
                
                if (toNodeId.equals("C")) {
                    state.getUser().setCoin(state.getUser().getCoin() + getCoinReward());
                    
                    generateFeedbackPacket(state, packets);
                    
                    currentConnection.setPacket(null);
                    packets.remove(this);
                    System.out.println("Packet removed at C");
                    return;
                }
                
                INode destinationNode = currentConnection.getToNode();
                processPacketAtNode(destinationNode, state, packets);

                if (isReturning) {
                    updateBackward(state, packets);
                    return;
                }
                
                IConnection nextConnection = null;
                for (IConnection conn : state.getConnections()) {
                    if (conn.getFromNode().getId().equals(toNodeId)) {
                        if (conn.getPacket() == null && !conn.isDestroyed()) {
                            if (conn.getToNode().isDamaged()) {
                                continue;
                            }

                            if (isConfidentialPacket() && !conn.getFromNode().isUncontrollableSystem()) {
                                continue;
                            }

                            String[] outShapes = conn.getFromNode().getOutputShapes();
                            int portIndex = conn.getFromPort();
                            if (portIndex >= 0 && portIndex < outShapes.length) {
                                String portShape = outShapes[portIndex];
                                boolean isSabotage = conn.getFromNode().isSabotageSystem();
                                boolean isCompatible = this.packetType.equals(portShape);
                                if ((!isSabotage && isCompatible) || (isSabotage && !isCompatible)) {
                                    nextConnection = conn;
                                    break;
                                }
                            }
                        }
                    }
                }

                if (nextConnection == null) {
                    for (IConnection conn : state.getConnections()) {
                        if (conn.getFromNode().getId().equals(toNodeId) && conn.getPacket() == null && !conn.getToNode().isDamaged() && !conn.isDestroyed()) {
                            nextConnection = conn;
                            break;
                        }
                    }
                }

                if (nextConnection != null) {
                    currentConnection.setPacket(null);
                    currentConnection = nextConnection;
                    segmentIndex = 0;
                    currentConnection.setPacket(this);
                    System.out.println("Packet transitioned to connection from " + currentConnection.getFromNode().getId() + " to " + currentConnection.getToNode().getId() +
                            ", speed remains: " + String.format("%.4f", GameConfig.PACKET_SPEED));
                    return;
                } else {
                    packets.remove(this);
                    currentConnection.setPacket(null);
                    System.out.println("Packet removed at " + toNodeId + ": no valid next connection");
                }

            }
        }
    }

    public void updateBackward(GameState state, java.util.List<Packet> packets) {
        double oldProgress = progress;
        updateSpeed();
        
        progress -= currentSpeed;
        if (progress < 0.0) progress = 0.0;

        if (progress <= 0.0 && segmentIndex <= 0) {
            String fromNodeId = currentConnection.getFromNode().getId();
            String toNodeId = currentConnection.getToNode().getId();
            System.out.println("Packet at start of connection from " + fromNodeId + " to " + toNodeId);
            if (fromNodeId.equals("A")) {
                packets.remove(this);
                currentConnection.setPacket(null);
                System.out.println("Packet removed at A");
                return;
            }
            IConnection prevConnection = null;
            for (IConnection conn : state.getConnections()) {
                if (conn.getToNode().getId().equals(fromNodeId)) {
                    if (conn.getPacket() == null) {
                        prevConnection = conn;
                        if ((this.packetType.equals("triangle") && conn.getToPort() == 0) ||
                                (this.packetType.equals("square") && conn.getToPort() == 1)) {
                            prevConnection = conn;
                            break;
                        }
                    }
                }
            }

            if (prevConnection != null) {
                currentConnection.setPacket(null);
                currentConnection = prevConnection;
                currentConnection.setPacket(this);
                java.util.List<Point> path = prevConnection.getDetailedPath();
                segmentIndex = path.size() - 2;
                progress = 1.0 - GameConfig.PACKET_SPEED;
                System.out.println("Packet transitioned backward to connection from " + currentConnection.getFromNode().getId() + " to " + currentConnection.getToNode().getId() +
                        ", speed remains: " + String.format("%.4f", GameConfig.PACKET_SPEED));
                return;
            }

            packets.remove(this);
            currentConnection.setPacket(null);
            System.out.println("Packet removed at " + fromNodeId + ": no valid previous connection");
        } else if (progress <= 0.0) {
            segmentIndex--;
            progress = 1.0 - GameConfig.PACKET_SPEED;
        }


    }

    public Point getPosition() {
        List<Point> path = currentConnection.getDetailedPath();

        if (segmentIndex >= path.size() - 1) {
            Point base = path.get(path.size() - 1);
            return new Point(base.x + displacement.x, base.y + displacement.y);
        }
        Point start = path.get(segmentIndex);
        Point end = path.get(segmentIndex + 1);

        double x = start.x + (end.x - start.x) * progress;
        double y = start.y + (end.y - start.y) * progress;
        Point pos = new Point((int) x, (int) y);

        return new Point(pos.x + displacement.x, pos.y + displacement.y);
    }

    public void applyDisplacement(Point disp) {
        this.displacement = new Point(this.displacement.x + disp.x, this.displacement.y + disp.y);
    }

    public void resetDisplacement() {
        this.displacement = new Point(0, 0);
    }

    public double getNoise() {
        return noise;
    }

    public void setNoise(double noise) {
        this.noise = noise;
    }

    public String getPacketType() {
        return packetType;
    }

    public IConnection getCurrentConnection() {
        return currentConnection;
    }

    public int getSegmentIndex() {
        return segmentIndex;
    }

    public double getProgress() {
        return progress;
    }

    public double getCurrentSpeed() {
        return currentSpeed;
    }

    public void setCurrentSpeed(double speed) {
        this.currentSpeed = speed;
    }

    public String getOriginalPacketType() {
        return originalPacketType;
    }

    public void setOriginalPacketType(String originalPacketType) {
        this.originalPacketType = originalPacketType;
    }

    public boolean isProtected() {
        return isProtected;
    }

    public void setProtected(boolean protected_) {
        this.isProtected = protected_;
    }

    private void updateSpeed() {
        double wireLength = currentConnection.getLength();
        double baseSpeed = GameConfig.PACKET_SPEED;
        
        updateLastMovementTime();
        
        if (isTimedOut()) {
            System.out.println("Packet timed out on wire, removing from network");
            return;
        }
        

        if (isConfidentialPacket()) {
            currentSpeed = baseSpeed;
            return;
        }

        if (wireLength > GameConfig.LONG_WIRE_THRESHOLD) {
            double accelerationFactor = 1.0 + (wireLength - GameConfig.LONG_WIRE_THRESHOLD) / GameConfig.LONG_WIRE_THRESHOLD * GameConfig.WIRE_ACCELERATION_FACTOR;
            currentSpeed = baseSpeed * accelerationFactor;
        } else {
            currentSpeed = baseSpeed;
        }
    }
    

    private boolean isPortCompatible() {
        String fromShape = currentConnection.getFromNode().getOutputShapes()[currentConnection.getFromPort()];
        String toShape = currentConnection.getToNode().getInputShapes()[currentConnection.getToPort()];
        return fromShape.equals(toShape);
    }
    
    private void applySquarePacketBehavior(boolean isCompatiblePort) {
        if (isCompatiblePort) {
            currentSpeed *= GameConfig.COMPATIBLE_PORT_SPEED_FACTOR;
        }
    }
    
    private void applyTrianglePacketBehavior(boolean isCompatiblePort) {
        if (!isCompatiblePort) {
            currentSpeed *= GameConfig.INCOMPATIBLE_PORT_SPEED_FACTOR;
        }
    }
    
    private void applyCirclePacketBehavior(boolean isCompatiblePort) {
        if (isCompatiblePort) {
            acceleration += 0.001;
            currentSpeed += acceleration * GameConfig.CIRCLE_ACCELERATION_FACTOR;
        } else {
            acceleration -= 0.001;
            currentSpeed *= GameConfig.CIRCLE_DECELERATION_FACTOR;
        }
        if (currentSpeed < GameConfig.PACKET_SPEED * 0.1) {
            currentSpeed = GameConfig.PACKET_SPEED * 0.1;
        }
    }
    

    public void applyShopItemEffects(GameState state) {
        if (state.isSpeedBoosterActive()) {
            currentSpeed *= GameConfig.SPEED_BOOSTER_FACTOR;
        }
        if (state.isSpeedLimiterActive()) {
            currentSpeed *= GameConfig.SPEED_LIMITER_FACTOR;
        }
        if (state.isWireOptimizerActive()) {
            double wireLength = currentConnection.getLength();
            double optimizedLength = wireLength * GameConfig.WIRE_OPTIMIZER_FACTOR;
            if (optimizedLength > GameConfig.LONG_WIRE_THRESHOLD) {
                double accelerationFactor = 1.0 + (optimizedLength - GameConfig.LONG_WIRE_THRESHOLD) / GameConfig.LONG_WIRE_THRESHOLD * GameConfig.WIRE_ACCELERATION_FACTOR;
                currentSpeed = GameConfig.PACKET_SPEED * accelerationFactor;
            } else {
                currentSpeed = GameConfig.PACKET_SPEED;
            }
        }
        if (isScrollAergiaActive(state)) {
            currentSpeed = GameConfig.SCROLL_AERGIA_SPEED_FACTOR;
        }
        if (isScrollEliphasActive(state)) {
            restoreCenterOfMassToWire();
        }
    }
    
    private boolean isScrollAergiaActive(GameState state) {
        if (currentConnection == null) return false;
        String connectionId = getConnectionId();
        return state.getScrollAergiaEffects().containsKey(connectionId);
    }
    
    private boolean isScrollEliphasActive(GameState state) {
        if (currentConnection == null) return false;
        String connectionId = getConnectionId();
        return state.getScrollEliphasEffects().containsKey(connectionId);
    }
    
    private String getConnectionId() {
        if (currentConnection == null) return "";
        return currentConnection.getFromNode().getId() + "_" + currentConnection.getToNode().getId() + 
               "_" + currentConnection.getFromPort() + "_" + currentConnection.getToPort();
    }
    
    private void restoreCenterOfMassToWire() {
        if (displacement.x != 0 || displacement.y != 0) {
            double restorationSpeed = GameConfig.SCROLL_ELIPHAS_RESTORATION_SPEED;
            double distance = Math.hypot(displacement.x, displacement.y);
            if (distance > 0) {
                double directionX = -displacement.x / distance;
                double directionY = -displacement.y / distance;
                double moveDistance = Math.min(restorationSpeed, distance);
                displacement.x += directionX * moveDistance;
                displacement.y += directionY * moveDistance;
                if (Math.abs(displacement.x) < 0.1) displacement.x = 0;
                if (Math.abs(displacement.y) < 0.1) displacement.y = 0;
            }
        }
    }

    public boolean isConfidential() {
        return "confidential".equals(packetType);
    }

    public boolean isProtectedPacket() {
        return "protected".equals(packetType);
    }

    public boolean isTrojan() {
        return "trojan".equals(packetType);
    }

    public boolean isMessenger() {
        return "messenger".equals(packetType);
    }

    public void convertToTrojan() {
        if (isType1Packet()) {
            // Convert type 1 to type 2
            this.packetType = getType2Equivalent();
        } else if (isType2Packet()) {
            // Convert type 2 to type 1
            this.packetType = getType1Equivalent();
        } else {
            // For other packet types, just mark as trojan
            this.packetType = "trojan";
        }
    }

    public void convertToMessenger() {
        this.packetType = "messenger";
    }
    
    private boolean isType1Packet() {
        return "square".equals(originalPacketType) || "triangle".equals(originalPacketType) || 
               "circle".equals(originalPacketType) || "protected".equals(originalPacketType);
    }
    
    private boolean isType2Packet() {
        return "confidential_4".equals(originalPacketType) || "confidential_6".equals(originalPacketType) ||
               "bulky_8".equals(originalPacketType) || "bulky_10".equals(originalPacketType);
    }
    
    private String getType2Equivalent() {
        switch (originalPacketType) {
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
    
    private String getType1Equivalent() {
        switch (originalPacketType) {
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

    public void convertToProtected() {
        this.packetType = "protected";
        this.isProtected = true;
    }

    public void convertToConfidential() {
        this.packetType = "confidential";
    }

    public void revertToOriginal() {
        this.packetType = this.originalPacketType;
        this.isProtected = false;
    }
    private void initializePacketSize() {
        switch (packetType) {
            case "square":
                this.packetSize = GameConfig.SQUARE_PACKET_SIZE;
                break;
            case "triangle":
                this.packetSize = GameConfig.TRIANGLE_PACKET_SIZE;
                break;
            case "circle":
                this.packetSize = GameConfig.CIRCLE_PACKET_SIZE;
                break;
            case "protected":
                    this.packetSize = getOriginalPacketSize() * GameConfig.PROTECTED_PACKET_SIZE_MULTIPLIER;
                break;
            case "confidential_4":
                this.packetSize = GameConfig.CONFIDENTIAL_PACKET_SIZE_4;
                break;
            case "confidential_6":
                this.packetSize = GameConfig.CONFIDENTIAL_PACKET_SIZE_6;
                break;
            case "bulky_8":
                this.packetSize = GameConfig.BULKY_PACKET_SIZE_8;
                break;
            case "bulky_10":
                this.packetSize = GameConfig.BULKY_PACKET_SIZE_10;
                break;
            case "bit":
                this.packetSize = 1; // Bit packets are always size 1
                break;
            default:
                this.packetSize = 1; // Default size
                break;
        }
        this.isLargePacket = (packetSize >= GameConfig.LARGE_PACKET_THRESHOLD);
    }
    
    private int getOriginalPacketSize() {
        switch (originalPacketType) {
            case "square":
                return GameConfig.SQUARE_PACKET_SIZE;
            case "triangle":
                return GameConfig.TRIANGLE_PACKET_SIZE;
            case "circle":
                return GameConfig.CIRCLE_PACKET_SIZE;
            default:
                return 1;
        }
    }
    
    private void initializeProtectedPacketBehavior() {
        String[] behaviors = {"square", "triangle", "circle"};
        this.movementBehavior = behaviors[(int) (Math.random() * behaviors.length)];
        System.out.println("Protected packet initialized with movement behavior: " + movementBehavior);
    }
    
    public int getPacketSize() {
        return packetSize;
    }
    
    public boolean isLargePacket() {
        return isLargePacket;
    }
    
    public boolean isBitPacket() {
        return isBitPacket;
    }
    
    public void setBitPacket(boolean isBitPacket) {
        this.isBitPacket = isBitPacket;
    }
    
    public String getParentPacketId() {
        return parentPacketId;
    }
    
    public void setParentPacketId(String parentPacketId) {
        this.parentPacketId = parentPacketId;
    }
    
    public boolean isReturning() {
        return isReturning;
    }
    
    public void setReturning(boolean returning) {
        this.isReturning = returning;
    }
    
    public String getMovementBehavior() {
        return movementBehavior;
    }
    
    public long getCreationTime() {
        return creationTime;
    }
    
    public long getLastMovementTime() {
        return lastMovementTime;
    }
    
    public void updateLastMovementTime() {
        this.lastMovementTime = System.currentTimeMillis();
    }
    
    public boolean isTimedOut() {
        return (System.currentTimeMillis() - lastMovementTime) > GameConfig.PACKET_TIMEOUT_MS;
    }
    
    public int getCoinReward() {
        switch (packetType) {
            case "square":
                return GameConfig.SQUARE_PACKET_COINS;
            case "triangle":
                return GameConfig.TRIANGLE_PACKET_COINS;
            case "circle":
                return GameConfig.CIRCLE_PACKET_COINS;
            case "protected":
                return GameConfig.PROTECTED_PACKET_COINS;
            case "confidential_4":
                return GameConfig.CONFIDENTIAL_PACKET_4_COINS;
            case "confidential_6":
                return GameConfig.CONFIDENTIAL_PACKET_6_COINS;
            case "bulky_8":
                return GameConfig.BULKY_PACKET_8_COINS;
            case "bulky_10":
                return GameConfig.BULKY_PACKET_10_COINS;
            case "bit":
                return 0;
            default:
                return 1;
        }
    }

    private void processPacketAtNode(INode node, GameState state, List<Packet> packets) {
        if (!node.canOperate()) {
            this.isReturning = true;
            System.out.println("System " + node.getId() + " is damaged, packet returning");
            return;
        }
        if ("C".equals(node.getId()) && isBitPacket()) {
            state.setPacketLoss(state.getPacketLoss() + 1);
            packets.remove(this);
            if (currentConnection != null) {
                currentConnection.setPacket(null);
            }
            System.out.println("Bit packet reached reference system C -> counted as packet loss");
            return;
        }
        state.getUser().setCoin(state.getUser().getCoin() + getCoinReward());
        if (node.isSpySystem()) {
            processSpySystem(node, state, packets);
        } else if (node.isSabotageSystem()) {
            processSabotageSystem(node, state, packets);
        } else if (node.isVpnSystem()) {
            processVpnSystem(node, state, packets);
        } else if (isDistributeSystem(node)) {
            processDistributeSystem(node, state, packets);
        } else if (isMergeSystem(node)) {
            processMergeSystem(node, state, packets);
        } else if (isMaliciousSystem(node)) {
            processMaliciousSystem(node, state, packets);
        }
        if (isBulkyPacket()) {
            currentConnection.incrementBulkyPacketPasses();
        }
        if (isBulkyPacket()) {
            clearSystemOfPackets(node, state, packets);
        }
        if (isBulkyPacket()) {
            randomizeSystemPorts(node);
        }
        if ("circle".equals(packetType)) {
            handleCirclePacketCollision(node, state, packets);
        }
        if (isIncompatiblePortEntry(node)) {
            currentSpeed *= GameConfig.INCOMPATIBLE_PORT_SPEED_FACTOR;
            System.out.println("Packet entered from incompatible port, speed doubled");
        }
    }
    
    private boolean isDistributeSystem(INode node) {
        return GameConfig.DISTRIBUTE_SYSTEM_TYPE.equals(node.getSystemType());
    }
    
    private boolean isMergeSystem(INode node) {
        return GameConfig.MERGE_SYSTEM_TYPE.equals(node.getSystemType());
    }
    
    private boolean isMaliciousSystem(INode node) {
        return GameConfig.MALICIOUS_SYSTEM_TYPE.equals(node.getSystemType());
    }
    
    private boolean isIncompatiblePortEntry(INode node) {
        String fromShape = currentConnection.getFromNode().getOutputShapes()[currentConnection.getFromPort()];
        String toShape = node.getInputShapes()[currentConnection.getToPort()];
        return !fromShape.equals(toShape);
    }
    
    private void processDistributeSystem(INode node, GameState state, java.util.List<Packet> packets) {
        if (isLargePacket || isBulkyPacket()) {
            distributeLargePacket(node, state, packets);
        } else {
            System.out.println("Normal packet passed through distribute system " + node.getId());
        }
    }
    
    private void processMergeSystem(INode node, GameState state, java.util.List<Packet> packets) {
        if (isBitPacket) {
            mergeBitPackets(node, state, packets);
        } else {
            System.out.println("Normal packet passed through merge system " + node.getId());
        }
    }
    
    private void processMaliciousSystem(INode node, GameState state, java.util.List<Packet> packets) {
    }
    
    private void handleCirclePacketCollision(INode node, GameState state, java.util.List<Packet> packets) {
        for (Packet otherPacket : packets) {
            if (otherPacket != this && otherPacket.getCurrentConnection() != null) {
                double distance = getPosition().distance(otherPacket.getPosition());
                if (distance <= GameConfig.COLLISION_DISTANCE) {
                    setReturning(true);
                    System.out.println("Circle packet collided, returning to source");
                    break;
                }
            }
        }
    }
    
    private void distributeLargePacket(INode node, GameState state, java.util.List<Packet> packets) {
        int numBitPackets = packetSize;
        String parentId = node.getId() + "_" + System.currentTimeMillis();
        packets.remove(this);
        currentConnection.setPacket(null);
        for (int i = 0; i < numBitPackets; i++) {
            for (IConnection conn : state.getConnections()) {
                if (conn.getFromNode().getId().equals(node.getId()) && conn.getPacket() == null) {
                    Packet bitPacket = new Packet(conn, "bit");
                    bitPacket.setBitPacket(true);
                    bitPacket.setParentPacketId(parentId);
                    bitPacket.packetSize = 1; // Bit packets are size 1
                    if (isBulkyPacket()) {
                        bitPacket.setParentBulkyPacketId(parentId);
                    }
                    packets.add(bitPacket);
                    System.out.println("Created bit packet " + i + " from " + (isBulkyPacket() ? "bulky" : "large") + " packet");
                    break;
                }
            }
        }
        
        System.out.println((isBulkyPacket() ? "Bulky" : "Large") + " packet distributed into " + numBitPackets + " bit packets");
    }
    
    private void mergeBitPackets(INode node, GameState state, java.util.List<Packet> packets) {
        java.util.List<Packet> siblingBitPackets = new java.util.ArrayList<>();
        for (Packet packet : packets) {
            if (packet.isBitPacket() && parentPacketId != null && parentPacketId.equals(packet.getParentPacketId())) {
                if (packet.getCurrentConnection() != null && packet.getCurrentConnection().getToNode().getId().equals(node.getId())) {
                    siblingBitPackets.add(packet);
                }
            }
        }
        int expected = this.packetSize > 1 ? this.packetSize : getOriginalPacketSize();
        if (expected < 2) expected = 2;
        if (siblingBitPackets.size() >= expected) {
            Packet mergedPacket = new Packet(currentConnection, originalPacketType);
            mergedPacket.packetSize = expected;
            for (Packet bitPacket : siblingBitPackets) {
                packets.remove(bitPacket);
                if (bitPacket.getCurrentConnection() != null) {
                    bitPacket.getCurrentConnection().setPacket(null);
                }
            }
            packets.add(mergedPacket);
            System.out.println("Merged " + siblingBitPackets.size() + " bit packets into original packet");
        }
    }

    private void processSpySystem(INode node, GameState state, java.util.List<Packet> packets) {
        if (isConfidential() || isConfidentialPacket()) {
            packets.remove(this);
            currentConnection.setPacket(null);
            System.out.println("Confidential packet destroyed by spy system " + node.getId());
        } else if (isProtectedPacket()) {
            System.out.println("Protected packet unaffected by spy system " + node.getId());
        } else if (!isBitPacket()) {
            java.util.List<INode> spySystems = new java.util.ArrayList<>();
            for (INode n : state.getNodes()) {
                if (n.isSpySystem() && !n.getId().equals(node.getId()) && n.canOperate()) {
                    spySystems.add(n);
                }
            }
            
            if (!spySystems.isEmpty()) {
                INode targetSpy = spySystems.get((int) (Math.random() * spySystems.size()));
                for (IConnection conn : state.getConnections()) {
                    if (conn.getFromNode().getId().equals(targetSpy.getId()) && conn.getPacket() == null) {
                        currentConnection.setPacket(null);
                        currentConnection = conn;
                        segmentIndex = 0;
                        progress = 0.0;
                        currentConnection.setPacket(this);
                        System.out.println("Packet teleported from spy system " + node.getId() + " to " + targetSpy.getId());
                        return;
                    }
                }
            }
        }
    }

    private void processSabotageSystem(INode node, GameState state, java.util.List<Packet> packets) {
        if (!isProtectedPacket()) {
            if (noise == 0) {
                noise = 1.0;
                System.out.println("Noise added to packet by sabotage system " + node.getId());
            }
            if (Math.random() < GameConfig.TROJAN_CONVERSION_PROBABILITY) {
                convertToTrojan();
                System.out.println("Packet converted to trojan by sabotage system " + node.getId());
            }
        }
    }

    private void processVpnSystem(INode node, GameState state, java.util.List<Packet> packets) {
        if (!isProtectedPacket() && !isBitPacket()) {
            this.vpnSourceNodeId = node.getId();
            convertToProtected();
            System.out.println("Packet protected by VPN system " + node.getId());
        }
    }
    public boolean isAnotherPacketInTargetSystem(GameState state) {
        if (currentConnection == null) return false;
        
        String targetNodeId = currentConnection.getToNode().getId();
        for (Packet otherPacket : state.getPackets()) {
            if (otherPacket != this && otherPacket.getCurrentConnection() != null) {
                if (otherPacket.getCurrentConnection().getToNode().getId().equals(targetNodeId) ||
                    otherPacket.getCurrentConnection().getFromNode().getId().equals(targetNodeId)) {
                    return true;
                }
            }
        }
        return false;
    }
    public void maintainDistanceFromOtherPackets(GameState state) {
        Point myPosition = getPosition();
        for (Packet otherPacket : state.getPackets()) {
            if (otherPacket != this && otherPacket.getCurrentConnection() != null) {
                Point otherPosition = otherPacket.getPosition();
                double distance = myPosition.distance(otherPosition);
                
                if (distance < GameConfig.CONFIDENTIAL_DISTANCE_MAINTENANCE) {
                    if (distance < GameConfig.CONFIDENTIAL_DISTANCE_MAINTENANCE / 2) {
                        currentSpeed *= 0.5;
                    } else {
                        currentSpeed *= 0.8;
                    }
                }
            }
        }
    }
    private void initializeBulkyPacketProperties() {
        if ("bulky_10".equals(packetType)) {
            distanceTraveled = 0;
        }
    }
    public boolean isConfidentialPacket() {
        return "confidential_4".equals(packetType) || "confidential_6".equals(packetType);
    }
    
    public boolean isBulkyPacket() {
        return "bulky_8".equals(packetType) || "bulky_10".equals(packetType);
    }
    
    public boolean isConfidential4() {
        return "confidential_4".equals(packetType);
    }
    
    public boolean isConfidential6() {
        return "confidential_6".equals(packetType);
    }

    public String getParentBulkyPacketId() {
        return parentBulkyPacketId;
    }
    
    public void setParentBulkyPacketId(String parentBulkyPacketId) {
        this.parentBulkyPacketId = parentBulkyPacketId;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }
    
    public void setSegmentIndex(int segmentIndex) {
        this.segmentIndex = segmentIndex;
    }
    
    public void setDisplacement(Point displacement) {
        this.displacement = displacement;
    }
    
    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }
    
    public void setLastMovementTime(long lastMovementTime) {
        this.lastMovementTime = lastMovementTime;
    }
    
    public void setPacketSize(int packetSize) {
        this.packetSize = packetSize;
    }
    
    public void setLargePacket(boolean isLargePacket) {
        this.isLargePacket = isLargePacket;
    }
    
    public void setDistanceTraveled(double distanceTraveled) {
        this.distanceTraveled = distanceTraveled;
    }
    
    public void setAcceleration(double acceleration) {
        this.acceleration = acceleration;
    }
    
    public void setMovementBehavior(String movementBehavior) {
        this.movementBehavior = movementBehavior;
    }
    
    public String getPlayerId() {
        return playerId;
    }
    

    private void generateFeedbackPacket(GameState state, List<Packet> packets) {
        if (!isType1Packet() && !isType2Packet()) {
            return;
        }
        
        INode uncontrollableSystem = findUncontrollableSystem(state);
        if (uncontrollableSystem == null) {
            return;
        }
        
        IConnection feedbackConnection = findFeedbackConnection(uncontrollableSystem, state);
        if (feedbackConnection == null) {
            return;
        }
        
        String counterPacketType = generateCounterPacketType();
        if (counterPacketType == null) {
            return;
        }
        
        Color opponentColor = "player1".equals(playerId) ? Color.RED : Color.BLUE;
        String opponentId = "player1".equals(playerId) ? "player2" : "player1";
        
        Packet counterPacket = new Packet(feedbackConnection, counterPacketType, opponentId, opponentColor);
        packets.add(counterPacket);
        
        System.out.println("Feedback loop: Generated " + counterPacketType + " counter-packet for opponent " + opponentId);
    }
    
    private INode findUncontrollableSystem(GameState state) {
        for (INode node : state.getNodes()) {
            if (node.isUncontrollableSystem() && node.canOperate()) {
                return node;
            }
        }
        return null;
    }
    
    private IConnection findFeedbackConnection(INode system, GameState state) {
        for (IConnection conn : state.getConnections()) {
            if (conn.getFromNode().getId().equals(system.getId()) && 
                conn.getPacket() == null && !conn.isDestroyed()) {
                return conn;
            }
        }
        return null;
    }
    
    private String generateCounterPacketType() {
        PacketTypeRegistry registry = new PacketTypeRegistry();
        return registry.getCounterPacketType(originalPacketType);
    }
    
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
    
    public Color getPlayerColor() {
        return playerColor;
    }
    
    public void setPlayerColor(Color playerColor) {
        this.playerColor = playerColor;
    }
    
    public double getAcceleration() {
        return acceleration;
    }
    
    public double getDistanceTraveled() {
        return distanceTraveled;
    }
    
    public Point getDisplacement() {
        return displacement;
    }
    
    public long getSpawnTime() {
        return creationTime;
    }
    
    public String getId() {
        return "packet_" + creationTime + "_" + hashCode();
    }
    
    public int getX() {
        return getPosition().x;
    }
    
    public int getY() {
        return getPosition().y;
    }
    
    public IConnection getConnection() {
        return currentConnection;
    }
    
    private void clearSystemOfPackets(INode node, GameState state, java.util.List<Packet> packets) {
        java.util.List<Packet> packetsToRemove = new java.util.ArrayList<>();
        for (Packet packet : packets) {
            if (packet.getCurrentConnection() != null) {
                if (packet.getCurrentConnection().getToNode().getId().equals(node.getId()) ||
                    packet.getCurrentConnection().getFromNode().getId().equals(node.getId())) {
                    packetsToRemove.add(packet);
                }
            }
        }
        
        for (Packet packet : packetsToRemove) {
            packets.remove(packet);
            if (packet.getCurrentConnection() != null) {
                packet.getCurrentConnection().setPacket(null);
            }
        }
        
        System.out.println("Bulky packet cleared " + packetsToRemove.size() + " packets from system " + node.getId());
    }
    
    private void randomizeSystemPorts(INode node) {
        String[] inputShapes = node.getInputShapes();
        String[] outputShapes = node.getOutputShapes();
        
        if (inputShapes.length > 1) {
            int randomIndex = (int) (Math.random() * inputShapes.length);
            String[] shapes = {"square", "triangle", "circle"};
            inputShapes[randomIndex] = shapes[(int) (Math.random() * shapes.length)];
        }
        
        if (outputShapes.length > 1) {
            int randomIndex = (int) (Math.random() * outputShapes.length);
            String[] shapes = {"square", "triangle", "circle"};
            outputShapes[randomIndex] = shapes[(int) (Math.random() * shapes.length)];
        }
        
        System.out.println("Bulky packet randomized ports in system " + node.getId());
    }

}
