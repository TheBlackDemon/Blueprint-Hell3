package Game;

import java.awt.*;

public class BendPoint {
    private Point position;
    private final int maxRadius;
    private boolean isDragging;
    private Point dragOffset;
    
    public BendPoint(int x, int y, int maxRadius) {
        this.position = new Point(x, y);
        this.maxRadius = maxRadius;
        this.isDragging = false;
        this.dragOffset = new Point(0, 0);
    }
    
    public BendPoint(Point position, int maxRadius) {
        this.position = new Point(position);
        this.maxRadius = maxRadius;
        this.isDragging = false;
        this.dragOffset = new Point(0, 0);
    }
    
    public Point getPosition() {
        return new Point(position);
    }
    
    public int getX() {
        return position.x;
    }
    
    public int getY() {
        return position.y;
    }
    
    public void setPosition(Point newPosition) {
        this.position = new Point(newPosition);
    }
    
    public void setPosition(int x, int y) {
        this.position = new Point(x, y);
    }
    
    public int getMaxRadius() {
        return maxRadius;
    }
    
    public boolean isDragging() {
        return isDragging;
    }
    
    public void setDragging(boolean dragging) {
        this.isDragging = dragging;
    }

    public boolean isOverBendPoint(int mx, int my) {
        return Math.hypot(mx - position.x, my - position.y) < 8;
    }

    public boolean isWithinRadius(Point originalPosition, Point newPosition) {
        return Math.hypot(newPosition.x - originalPosition.x, newPosition.y - originalPosition.y) <= maxRadius;
    }
}
