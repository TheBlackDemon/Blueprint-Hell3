package Game;

import java.awt.*;

public class Shockwave {
    private final Point position;
    private double radius;
    private final long startTime;
    private final long lifespan;

    public Shockwave(Point position, long virtualTime) {
        this.position = position;
        this.radius = 0.0;
        this.startTime = virtualTime;
        this.lifespan = GameConfig.SHOCKWAVE_LIFESPAN_MS;
        System.out.println("Shockwave created at (" + position.x + ", " + position.y + ") at time " + virtualTime);
    }

    public void update(long virtualTime) {
        long elapsed = virtualTime - startTime;
        this.radius = (elapsed / (double) lifespan) * GameConfig.SHOCKWAVE_MAX_RADIUS;
    }

    public boolean isExpired(long virtualTime) {
        return (virtualTime - startTime) >= lifespan;
    }

    public Point getPosition() {
        return position;
    }

    public double getRadius() {
        return radius;
    }
    
    public String getId() {
        return "shockwave_" + startTime + "_" + hashCode();
    }
    
    public int getX() {
        return position.x;
    }
    
    public int getY() {
        return position.y;
    }
    
    public long getTimestamp() {
        return startTime;
    }

    public Point getDisplacement(Point packetPos) {
        double distance = Math.hypot(packetPos.x - position.x, packetPos.y - position.y);
        if (distance > radius) {
            return new Point(0, 0);
        }
        double strength = GameConfig.SHOCKWAVE_FORCE / (distance * distance + 0.1);
        double dx = (packetPos.x - position.x) * strength;
        double dy = (packetPos.y - position.y) * strength;
        double magnitude = Math.hypot(dx, dy);
        if (magnitude > GameConfig.MAX_DISPLACEMENT) {
            dx = dx * GameConfig.MAX_DISPLACEMENT / magnitude;
            dy = dy * GameConfig.MAX_DISPLACEMENT / magnitude;
        }
        Point displacement = new Point((int) dx, (int) dy);
        System.out.println("Shockwave at (" + position.x + ", " + position.y + ") displaces packet at (" +
                packetPos.x + ", " + packetPos.y + ") by (" + displacement.x + ", " + displacement.y +
                "), distance: " + String.format("%.2f", distance));
        return displacement;
    }
}
