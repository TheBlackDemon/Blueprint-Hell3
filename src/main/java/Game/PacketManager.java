package Game;

import controller.AudioManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PacketManager implements IPacketManager {
    private final GameState state;
    private long lastPacketSpawnTime;
    private long virtualTime;

    public PacketManager(GameState state) {
        this.state = state;
        this.lastPacketSpawnTime = 0;
        this.virtualTime = 0;
    }

    @Override
    public void stepForward() {
        virtualTime += GameConfig.ANIMATION_TICK_MS;
        updatePackets(true);
    }

    @Override
    public void stepBackward() {
        virtualTime -= GameConfig.ANIMATION_TICK_MS;
        updatePackets(false);
    }

    private void updatePackets(boolean forward) {
        List<Packet> packets = state.getPackets();
        List<Shockwave> shockwaves = state.getShockwaves();

        if (state.isClickAtar()){
            state.setAtar(true);
            state.setClickAtar(false);
            state.setStartTimeAtar(virtualTime);
        }
        if (state.isAtar() && ( virtualTime - state.getStartTimeAtar() >= 10000)){
            state.setAtar(false);
            state.setStartTimeAtar(0);
        }
        if (state.isClickAiryaman()){
            state.setAiryaman(true);
            state.setClickAiryaman(false);
            state.setStartTimeAiryaman(virtualTime);
        }
        if (state.isAiryaman() && ( virtualTime - state.getStartTimeAiryaman() >= 5000)){
            state.setAiryaman(false);
            state.setStartTimeAiryaman(0);
        }
        if (state.isClickAnahita()){
            for (int i = 0; i < packets.size(); i++){
                Packet packet = packets.get(i);
                packet.setNoise(0);
            }
            state.setClickAnahita(false);
        }
        
        if (state.isClickSpeedBooster()){
            state.setSpeedBoosterActive(true);
            state.setClickSpeedBooster(false);
            state.setStartTimeSpeedBooster(virtualTime);
        }
        if (state.isSpeedBoosterActive() && (virtualTime - state.getStartTimeSpeedBooster() >= GameConfig.SPEED_BOOSTER_DURATION_MS)){
            state.setSpeedBoosterActive(false);
            state.setStartTimeSpeedBooster(0);
        }
        
        if (state.isClickSpeedLimiter()){
            state.setSpeedLimiterActive(true);
            state.setClickSpeedLimiter(false);
            state.setStartTimeSpeedLimiter(virtualTime);
        }
        if (state.isSpeedLimiterActive() && (virtualTime - state.getStartTimeSpeedLimiter() >= GameConfig.SPEED_LIMITER_DURATION_MS)){
            state.setSpeedLimiterActive(false);
            state.setStartTimeSpeedLimiter(0);
        }
        
        if (state.isClickWireOptimizer()){
            state.setWireOptimizerActive(true);
            state.setClickWireOptimizer(false);
            state.setStartTimeWireOptimizer(virtualTime);
        }
        if (state.isWireOptimizerActive() && (virtualTime - state.getStartTimeWireOptimizer() >= GameConfig.WIRE_OPTIMIZER_DURATION_MS)){
            state.setWireOptimizerActive(false);
            state.setStartTimeWireOptimizer(0);
        }
        
        if (state.isClickScrollAergia()){
            state.setScrollAergiaActive(true);
            state.setClickScrollAergia(false);
            state.setStartTimeScrollAergia(virtualTime);
        }
        if (state.isScrollAergiaActive() && (virtualTime - state.getStartTimeScrollAergia() >= GameConfig.SCROLL_AERGIA_DURATION_MS)){
            state.setScrollAergiaActive(false);
            state.setStartTimeScrollAergia(0);
        }
        
        if (state.isClickScrollEliphas()){
            state.setScrollEliphasActive(true);
            state.setClickScrollEliphas(false);
            state.setStartTimeScrollEliphas(virtualTime);
        }
        if (state.isScrollEliphasActive() && (virtualTime - state.getStartTimeScrollEliphas() >= GameConfig.SCROLL_ELIPHAS_DURATION_MS)){
            state.setScrollEliphasActive(false);
            state.setStartTimeScrollEliphas(0);
        }
        
        cleanupExpiredScrollEffects(virtualTime);

        for (int i = shockwaves.size() - 1; i >= 0; i--) {
            Shockwave shockwave = shockwaves.get(i);
            shockwave.update(virtualTime);
            if (shockwave.isExpired(virtualTime)) {
                shockwaves.remove(i);
            }
        }

        java.util.List<Packet> packetsToRemove = new java.util.ArrayList<>();
        for (Packet packet : packets) {
            if (packet.isTimedOut()) {
                packetsToRemove.add(packet);
                if (packet.getCurrentConnection() != null) {
                    packet.getCurrentConnection().setPacket(null);
                }
                GameLogger.logPacketTimeout(packet.toString(), packet.getPacketType(), 
                                          packet.getCurrentConnection().toString(), 
                                          System.currentTimeMillis() - packet.getSpawnTime());
            } else {
                packet.resetDisplacement();
                packet.applyShopItemEffects(state);
            }
        }
        packets.removeAll(packetsToRemove);


        if (!state.isAiryaman()) {

            List<Packet> collisionPacketsToRemove = new ArrayList<>();
            List<Point> collisionPoints = new ArrayList<>();
            for (int i = 0; i < packets.size(); i++) {
                for (int j = i + 1; j < packets.size(); j++) {
                    Packet p1 = packets.get(i);
                    Packet p2 = packets.get(j);
                    if (p1.getCurrentConnection() != p2.getCurrentConnection()) {
                        Point pos1 = p1.getPosition();
                        Point pos2 = p2.getPosition();
                        double distance = pos1.distance(pos2);
                        if (distance <= GameConfig.PACKET_LOSS_DISTANCE) {
                            collisionPacketsToRemove.add(p1);
                            collisionPacketsToRemove.add(p2);
                            AudioManager.playSound("collision");
                            Point collisionPoint = new Point(
                                    (pos1.x + pos2.x) / 2,
                                    (pos1.y + pos2.y) / 2
                            );
                            
                            GameLogger.logPacketCollision(p1.toString(), p1.getPacketType(), 
                                                        p2.toString(), p2.getPacketType(), 
                                                        collisionPoint.x, collisionPoint.y, distance);
                            collisionPoints.add(collisionPoint);

                            if (!state.isAtar()) {
                                for (int index = 0; index < packets.size(); index++) {
                                    Packet packet = packets.get(index);
                                    double distanceCollision = packet.getPosition().distance(collisionPoint);
                                    packet.setNoise(packet.getNoise() + 1 / (distanceCollision + 1));
                                    if (packet.getNoise() >= 1 / (GameConfig.PACKET_LOSS_DISTANCE + 1)) {
                                        collisionPacketsToRemove.add(packet);
                                        for (IConnection connection : state.getConnections()) {
                                            if (connection.getPacket() != null) {
                                                if (connection.getPacket().equals(packet)) {
                                                    connection.setPacket(null);
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            }
            state.setPacketLoss(state.getPacketLoss() + collisionPacketsToRemove.size());
            packets.removeAll(collisionPacketsToRemove);
            
            GameLogger.logPacketLoss(state.getPacketLoss(), collisionPacketsToRemove.size(), 
                                   GameConfig.NUMBER_PACKET[state.getLevel()] / 2);

            if (2 * state.getPacketLoss() > GameConfig.NUMBER_PACKET[state.getLevel()]) {
                state.setGameOver(true);
                GameLogger.logGameOver("Packet loss exceeded limit", state.getLevel(), 
                                     state.getNumberPacketsSquare() + state.getNumberPacketTriangle(), 
                                     state.getPacketLoss(), state.getUser().getCoin());
            }
        }


        if (forward) {
            processSystemMechanics(packets);
            
            for (int i = packets.size() - 1; i >= 0; i--) {
                packets.get(i).updateForward(state, packets);
            }
            if (virtualTime >= 0 && state.getNumberPacketsSquare() + state.getNumberPacketTriangle() < GameConfig.NUMBER_PACKET[state.getLevel()]) {
                for (IConnection conn : state.getConnections()) {
                    if (conn.getFromNode().getId().equals("A")) {
                        if (conn.getPacket() == null && !conn.isDestroyed()) {
                            String shape = conn.getFromNode().getOutputShapes()[conn.getFromPort()];
                            if (shape.equals("square")) {
                                state.setNumberPacketsSquare(state.getNumberPacketsSquare() + 1);
                                packets.add(new Packet(conn, "square"));
                            } else if (shape.equals("triangle")) {
                                state.setNumberPacketTriangle(state.getNumberPacketTriangle() + 1);
                                packets.add(new Packet(conn, "triangle"));
                            } else if (shape.equals("circle")) {
                                state.setNumberPacketsCircle(state.getNumberPacketsCircle() + 1);
                                packets.add(new Packet(conn, "circle"));
                            } else if (shape.equals("confidential_4")) {
                                state.setNumberPacketsConfidential4(state.getNumberPacketsConfidential4() + 1);
                                packets.add(new Packet(conn, "confidential_4"));
                            } else if (shape.equals("confidential_6")) {
                                state.setNumberPacketsConfidential6(state.getNumberPacketsConfidential6() + 1);
                                packets.add(new Packet(conn, "confidential_6"));
                            } else if (shape.equals("bulky_8")) {
                                state.setNumberPacketsBulky8(state.getNumberPacketsBulky8() + 1);
                                packets.add(new Packet(conn, "bulky_8"));
                            } else if (shape.equals("bulky_10")) {
                                state.setNumberPacketsBulky10(state.getNumberPacketsBulky10() + 1);
                                packets.add(new Packet(conn, "bulky_10"));
                            }
                            GameLogger.logPacketSpawn(shape, conn.toString(), "A", conn.getToNode().getId());
                        }
                    }
                }
            } else if (virtualTime >= 0 && state.getNumberPacketsSquare() + state.getNumberPacketTriangle() >= GameConfig.NUMBER_PACKET[state.getLevel()]) {
                state.setSuccessfully(true);
                GameLogger.logLevelComplete(state.getLevel(), "Level " + state.getLevel(), 
                                          System.currentTimeMillis() - state.getLevelStartTime(),
                                          state.getNumberPacketsSquare() + state.getNumberPacketTriangle(),
                                          state.getPacketLoss(), 0);
            }
        } else {
            for (int i = packets.size() - 1; i >= 0; i--) {
                packets.get(i).updateBackward(state, packets);
            }
        }
    }

    @Override
    public List<Packet> getPackets() {
        return state.getPackets();
    }

    @Override
    public void resetPackets() {
        state.clearPackets();
        state.clearShockwaves();
        lastPacketSpawnTime = virtualTime;
        System.out.println("Packets and shockwaves reset, lastPacketSpawnTime: " + lastPacketSpawnTime);
    }

    private void processSystemMechanics(java.util.List<Packet> packets) {
        processAntitrojanSystems(packets);
        processVpnFailures();
        processVpnConfidentialGeneration();
        processConfidentialPacketBehaviors(packets);
        processSystemDamage(packets);
    }

    private void processAntitrojanSystems(java.util.List<Packet> packets) {
        for (INode node : state.getNodes()) {
            if (node.isAntitrojanSystem() && node.canAntitrojanOperate()) {
                for (Packet packet : packets) {
                    if (packet.isTrojan()) {
                        double distance = Math.hypot(
                            packet.getPosition().x - node.getX(),
                            packet.getPosition().y - node.getY()
                        );
                        
                        if (distance <= GameConfig.ANTITROJAN_DETECTION_RADIUS) {
                            packet.revertToOriginal();
                            node.setLastAntitrojanOperation(System.currentTimeMillis());
                            System.out.println("Anti-trojan system " + node.getId() + " reverted trojan packet to original type: " + packet.getPacketType());
                            break;
                        }
                    }
                }
            }
        }
    }

    private void processVpnFailures() {
        for (INode node : state.getNodes()) {
            if (node.isVpnSystem() && node.isDamaged()) {
                String vpnNodeId = node.getId();
                java.util.Set<Packet> protectedByThisVpn = state.getProtectedPacketsByVpnNode(vpnNodeId);
                if (protectedByThisVpn.isEmpty()) continue;

                java.util.List<Packet> toUnprotect = new java.util.ArrayList<>(protectedByThisVpn);
                for (Packet packet : toUnprotect) {
                    if (packet != null && packet.getPacketType().equals("protected")) {
                        packet.revertToOriginal();
                        state.removeProtectedPacketForVpn(vpnNodeId, packet);
                        System.out.println("VPN system " + vpnNodeId + " failed, reverted a packet to original type");
                    } else {
                        state.removeProtectedPacketForVpn(vpnNodeId, packet);
                    }
                }
            }
        }
    }
    
    private void processVpnConfidentialGeneration() {
        for (INode node : state.getNodes()) {
            if (node.isVpnSystem() && node.canOperate()) {
                for (Packet packet : state.getPackets()) {
                    if (packet.getCurrentConnection() != null && 
                        packet.getCurrentConnection().getToNode().getId().equals(node.getId()) &&
                        packet.isConfidential4()) {
                        generateConfidential6Packet(node, state);
                        break;
                    }
                }
            }
        }
    }
    
    private void generateConfidential6Packet(INode vpnNode, GameState state) {
        for (IConnection conn : state.getConnections()) {
            if (conn.getFromNode().getId().equals(vpnNode.getId()) && 
                conn.getPacket() == null && !conn.isDestroyed()) {
                
                Packet confidential6Packet = new Packet(conn, "confidential_6");
                state.getPackets().add(confidential6Packet);
                state.setNumberPacketsConfidential6(state.getNumberPacketsConfidential6() + 1);
                System.out.println("VPN system " + vpnNode.getId() + " generated confidential_6 packet");
                break;
            }
        }
    }
    
    private void processConfidentialPacketBehaviors(java.util.List<Packet> packets) {
        for (Packet packet : packets) {
            if (packet.isConfidentialPacket()) {
                if (packet.isAnotherPacketInTargetSystem(state)) {
                    packet.setCurrentSpeed(packet.getCurrentSpeed() * GameConfig.CONFIDENTIAL_SPEED_REDUCTION_FACTOR);
                }
                if (packet.isConfidential6()) {
                    packet.maintainDistanceFromOtherPackets(state);
                }
            }
        }
    }

    private void processSystemDamage(java.util.List<Packet> packets) {
        for (Packet packet : packets) {
            if (packet.getCurrentSpeed() > GameConfig.MAX_PACKET_SPEED) {
                INode destinationNode = packet.getCurrentConnection().getToNode();
                if (!destinationNode.isDamaged()) {
                    destinationNode.setDamaged(true);
                    System.out.println("System " + destinationNode.getId() + " damaged by high-speed packet (speed: " + 
                        String.format("%.4f", packet.getCurrentSpeed()) + ")");
                }
            }
        }
    }
    
    private void cleanupExpiredScrollEffects(long virtualTime) {
        java.util.List<String> expiredAergiaEffects = new java.util.ArrayList<>();
        for (java.util.Map.Entry<String, Long> entry : state.getScrollAergiaEffects().entrySet()) {
            if (virtualTime - entry.getValue() >= GameConfig.SCROLL_AERGIA_DURATION_MS) {
                expiredAergiaEffects.add(entry.getKey());
            }
        }
        for (String connectionId : expiredAergiaEffects) {
            state.removeScrollAergiaEffect(connectionId);
        }
        
        java.util.List<String> expiredEliphasEffects = new java.util.ArrayList<>();
        for (java.util.Map.Entry<String, Long> entry : state.getScrollEliphasEffects().entrySet()) {
            if (virtualTime - entry.getValue() >= GameConfig.SCROLL_ELIPHAS_DURATION_MS) {
                expiredEliphasEffects.add(entry.getKey());
            }
        }
        for (String connectionId : expiredEliphasEffects) {
            state.removeScrollEliphasEffect(connectionId);
        }
    }
}
