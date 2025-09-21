package Game;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AmmunitionManager {
    private final Map<AmmunitionType, Integer> ammunitionCounts;
    private final Map<AmmunitionType, Long> packetCooldowns;
    private final Map<String, Long> systemCooldowns;
    private final Map<AmmunitionType, Long> cooldownCompletionEffects;
    private final String playerId;
    private final Random random;

    public AmmunitionManager(String playerId) {
        this.playerId = playerId;
        this.ammunitionCounts = new ConcurrentHashMap<>();
        this.packetCooldowns = new ConcurrentHashMap<>();
        this.systemCooldowns = new ConcurrentHashMap<>();
        this.cooldownCompletionEffects = new ConcurrentHashMap<>();
        this.random = new Random();
        
        initializeAmmunition();
    }

    private void initializeAmmunition() {
        // Initialize ammunition counts for each type
        for (AmmunitionType type : AmmunitionType.values()) {
            ammunitionCounts.put(type, GameConfig.MAX_AMMUNITION_PER_TYPE);
        }
    }

    public boolean canFirePacket(AmmunitionType type) {
        return ammunitionCounts.getOrDefault(type, 0) > 0 && 
               !isPacketOnCooldown(type);
    }

    public boolean canUseSystem(String systemId) {
        return !isSystemOnCooldown(systemId);
    }

    public boolean firePacket(AmmunitionType type, String systemId) {
        if (!canFirePacket(type) || !canUseSystem(systemId)) {
            return false;
        }

        int currentCount = ammunitionCounts.get(type);
        ammunitionCounts.put(type, Math.max(0, currentCount - 1));

        setPacketCooldown(type);
        setSystemCooldown(systemId);

        return true;
    }


    public boolean isPacketOnCooldown(AmmunitionType type) {
        Long cooldownEnd = packetCooldowns.get(type);
        if (cooldownEnd == null) {
            return false;
        }
        return System.currentTimeMillis() < cooldownEnd;
    }

    public boolean isSystemOnCooldown(String systemId) {
        Long cooldownEnd = systemCooldowns.get(systemId);
        if (cooldownEnd == null) {
            return false;
        }
        return System.currentTimeMillis() < cooldownEnd;
    }

    public long getPacketCooldownRemaining(AmmunitionType type) {
        Long cooldownEnd = packetCooldowns.get(type);
        if (cooldownEnd == null) {
            return 0;
        }
        return Math.max(0, cooldownEnd - System.currentTimeMillis());
    }


    public long getSystemCooldownRemaining(String systemId) {
        Long cooldownEnd = systemCooldowns.get(systemId);
        if (cooldownEnd == null) {
            return 0;
        }
        return Math.max(0, cooldownEnd - System.currentTimeMillis());
    }

    public int getAmmunitionCount(AmmunitionType type) {
        return ammunitionCounts.getOrDefault(type, 0);
    }

    public Map<AmmunitionType, Integer> getAllAmmunitionCounts() {
        return new HashMap<>(ammunitionCounts);
    }

    private void setPacketCooldown(AmmunitionType type) {
        packetCooldowns.put(type, System.currentTimeMillis() + GameConfig.PACKET_COOLDOWN_MS);
    }

    private void setSystemCooldown(String systemId) {
        systemCooldowns.put(systemId, System.currentTimeMillis() + GameConfig.SYSTEM_COOLDOWN_MS);
    }
    public void update() {
        long currentTime = System.currentTimeMillis();
        
        for (Map.Entry<AmmunitionType, Long> entry : packetCooldowns.entrySet()) {
            if (currentTime >= entry.getValue()) {
                AmmunitionType type = entry.getKey();
                if (!cooldownCompletionEffects.containsKey(type)) {
                    cooldownCompletionEffects.put(type, currentTime);
                }
            }
        }
        
        cooldownCompletionEffects.entrySet().removeIf(entry ->
            currentTime - entry.getValue() > GameConfig.COOLDOWN_VISUAL_EFFECT_DURATION_MS);
    }

    public boolean hasCooldownCompletionEffect(AmmunitionType type) {
        return cooldownCompletionEffects.containsKey(type);
    }

    public void removeCooldownCompletionEffect(AmmunitionType type) {
        cooldownCompletionEffects.remove(type);
    }

    public AmmunitionStatus getAmmunitionStatus(AmmunitionType type) {
        int count = getAmmunitionCount(type);
        boolean onCooldown = isPacketOnCooldown(type);
        boolean hasEffect = hasCooldownCompletionEffect(type);
        
        AmmunitionStatus.Status status;
        if (count == 0) {
            status = AmmunitionStatus.Status.OUT_OF_AMMO;
        } else if (onCooldown) {
            status = AmmunitionStatus.Status.ON_COOLDOWN;
        } else {
            status = AmmunitionStatus.Status.AVAILABLE;
        }
        
        return new AmmunitionStatus(type, count, status, hasEffect, getPacketCooldownRemaining(type));
    }

    public List<AmmunitionStatus> getAllAmmunitionStatuses() {
        List<AmmunitionStatus> statuses = new ArrayList<>();
        for (AmmunitionType type : AmmunitionType.values()) {
            statuses.add(getAmmunitionStatus(type));
        }
        return statuses;
    }

    public void reset() {
        ammunitionCounts.clear();
        packetCooldowns.clear();
        systemCooldowns.clear();
        cooldownCompletionEffects.clear();
        initializeAmmunition();
    }

    public static class AmmunitionStatus {
        public enum Status {
            AVAILABLE,
            ON_COOLDOWN,
            OUT_OF_AMMO
        }

        private final AmmunitionType type;
        private final int count;
        private final Status status;
        private final boolean hasVisualEffect;
        private final long cooldownRemaining;

        public AmmunitionStatus(AmmunitionType type, int count, Status status, boolean hasVisualEffect, long cooldownRemaining) {
            this.type = type;
            this.count = count;
            this.status = status;
            this.hasVisualEffect = hasVisualEffect;
            this.cooldownRemaining = cooldownRemaining;
        }

        public AmmunitionType getType() { return type; }
        public int getCount() { return count; }
        public Status getStatus() { return status; }
        public boolean hasVisualEffect() { return hasVisualEffect; }
        public long getCooldownRemaining() { return cooldownRemaining; }
    }
}
