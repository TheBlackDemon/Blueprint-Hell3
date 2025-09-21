package Game;

import java.awt.*;
import java.util.List;

class ConnectionRenderer {
    public void render(Graphics g, IConnection conn) {
        List<Point> path = conn.getDetailedPath();
        g.setColor(Color.BLACK);
        
        for (int i = 0; i < path.size() - 1; i++) {
            Point p1 = path.get(i);
            Point p2 = path.get(i + 1);
            g.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
        
        g.setColor(Color.ORANGE);
        for (Point wp : conn.getWaypoints()) {
            g.fillOval(wp.x - 4, wp.y - 4, 8, 8);
        }
        
        for (BendPoint bp : conn.getBendPoints()) {
            Point pos = bp.getPosition();
            g.setColor(Color.RED);
            g.fillOval(pos.x - 6, pos.y - 6, 12, 12);
            g.setColor(Color.WHITE);
            g.fillOval(pos.x - 3, pos.y - 3, 6, 6);
            if (bp.isDragging()) {
                g.setColor(new Color(255, 0, 0, 50));
                g.fillOval(pos.x - bp.getMaxRadius(), pos.y - bp.getMaxRadius(), 
                          bp.getMaxRadius() * 2, bp.getMaxRadius() * 2);
            }
        }
        
        if (!conn.getBendPoints().isEmpty()) {
            Point start = conn.getFromNode().getPortPosition("output", conn.getFromPort());
            g.setColor(Color.BLUE);
            g.drawString("Bends: " + conn.getBendPoints().size() + "/3", start.x + 10, start.y - 10);
        }
    }
}
