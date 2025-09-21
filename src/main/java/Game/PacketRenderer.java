package Game;

import java.awt.*;

class PacketRenderer {
    public void render(Graphics g, Packet packet) {
        Point pos = packet.getPosition();
        Color packetColor = getPacketColor(packet);
        
        if (packet.getPacketType().equals("triangle")) {
            int[] xPoints = {pos.x - 4, pos.x + 4, pos.x};
            int[] yPoints = {pos.y - 4, pos.y - 4, pos.y + 4};
            g.setColor(packetColor);
            g.fillPolygon(xPoints, yPoints, 3);
        } else if (packet.getPacketType().equals("square")) {
            g.setColor(packetColor);
            g.fillRect(pos.x - 3, pos.y - 3, 6, 6);
        } else if (packet.getPacketType().equals("circle")) {
            g.setColor(packetColor);
            g.fillOval(pos.x - 4, pos.y - 4, 8, 8);
        } else if (packet.getPacketType().equals("confidential")) {
            int[] xPoints = {pos.x, pos.x + 4, pos.x, pos.x - 4};
            int[] yPoints = {pos.y - 4, pos.y, pos.y + 4, pos.y};
            g.setColor(packetColor);
            g.fillPolygon(xPoints, yPoints, 4);
        } else if (packet.getPacketType().equals("protected")) {
            g.setColor(packetColor);
            g.fillOval(pos.x - 4, pos.y - 4, 8, 8);
            g.setColor(Color.WHITE);
            g.drawOval(pos.x - 2, pos.y - 2, 4, 4);
        } else if (packet.getPacketType().equals("trojan")) {
            g.setColor(packetColor);
            g.fillRect(pos.x - 4, pos.y - 4, 8, 8);
            g.setColor(Color.WHITE);
            g.drawLine(pos.x - 2, pos.y - 2, pos.x + 2, pos.y + 2);
            g.drawLine(pos.x + 2, pos.y - 2, pos.x - 2, pos.y + 2);
        } else if (packet.getPacketType().equals("messenger")) {
            g.setColor(packetColor);
            g.fillOval(pos.x - 4, pos.y - 4, 8, 8);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 8));
            g.drawString("M", pos.x - 2, pos.y + 2);
        } else if (packet.getPacketType().equals("confidential_4")) {
            int[] xPoints = {pos.x, pos.x + 5, pos.x, pos.x - 5};
            int[] yPoints = {pos.y - 5, pos.y, pos.y + 5, pos.y};
            g.setColor(packetColor);
            g.fillPolygon(xPoints, yPoints, 4);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 8));
            g.drawString("4", pos.x - 2, pos.y + 2);
        } else if (packet.getPacketType().equals("confidential_6")) {
            int[] xPoints = {pos.x, pos.x + 5, pos.x, pos.x - 5};
            int[] yPoints = {pos.y - 5, pos.y, pos.y + 5, pos.y};
            g.setColor(packetColor);
            g.fillPolygon(xPoints, yPoints, 4);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 8));
            g.drawString("6", pos.x - 2, pos.y + 2);
        } else if (packet.getPacketType().equals("bulky_8")) {
            g.setColor(packetColor);
            g.fillRect(pos.x - 6, pos.y - 6, 12, 12);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 10));
            g.drawString("8", pos.x - 3, pos.y + 3);
        } else if (packet.getPacketType().equals("bulky_10")) {
            g.setColor(packetColor);
            g.fillRect(pos.x - 6, pos.y - 6, 12, 12);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 8));
            g.drawString("10", pos.x - 4, pos.y + 3);
        } else if (packet.getPacketType().equals("bit")) {
            g.setColor(packetColor);
            g.fillOval(pos.x - 2, pos.y - 2, 4, 4);
            if (packet.getParentBulkyPacketId() != null) {
                g.setColor(Color.WHITE);
                g.fillOval(pos.x - 1, pos.y - 1, 2, 2);
            }
        }
        if (packet.getCurrentSpeed() > GameConfig.PACKET_SPEED * 1.5) {
            g.setColor(Color.YELLOW);
            g.drawOval(pos.x - 6, pos.y - 6, 12, 12);
        }
    }
    
    private Color getPacketColor(Packet packet) {
        if (packet.getPlayerColor() != null && !packet.getPlayerId().equals("default")) {
            return packet.getPlayerColor();
        }
        
        switch (packet.getPacketType()) {
            case "triangle":
                return Color.ORANGE;
            case "square":
                return Color.CYAN;
            case "circle":
                return new Color(0, 200, 120);
            case "confidential":
                return Color.PINK;
            case "confidential_4":
                return Color.MAGENTA;
            case "confidential_6":
                return Color.PINK;
            case "protected":
                return Color.BLUE;
            case "trojan":
                return Color.RED;
            case "messenger":
                return Color.GREEN;
            case "bulky_8":
                return new Color(139, 69, 19);
            case "bulky_10":
                return new Color(160, 82, 45);
            case "bit":
                String parentId = packet.getParentBulkyPacketId();
                if (parentId != null) {
                    int hash = Math.abs(parentId.hashCode());
                    int r = 80 + (hash % 176);
                    int g = 80 + ((hash / 7) % 176);
                    int b = 80 + ((hash / 13) % 176);
                    return new Color(r, g, b);
                }
                return new Color(255, 165, 0);
            default:
                return Color.GRAY;
        }
    }
}
