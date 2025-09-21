package Game;

import java.awt.*;
import java.util.List;

class NodeRenderer {
    public void render(Graphics g, INode node, List<IConnection> connections) {
        int x = node.getX();
        int y = node.getY();
        int size = GameConfig.NODE_SIZE;
        
        Color nodeColor = getNodeColor(node);
        g.setColor(nodeColor);
        g.fillRect(x - size / 2, y - size / 2, size, size);
        
        if (node.isDamaged()) {
            g.setColor(Color.RED);
            g.drawRect(x - size / 2 - 2, y - size / 2 - 2, size + 4, size + 4);
        }
        
        g.setColor(Color.BLACK);
        g.drawRect(x - size / 2, y - size / 2, size, size);
        String[] inputShapes = node.getInputShapes();
        for (int i = 0; i < inputShapes.length; i++) {
            int px = x - size / 2 - 10;
            int py = y - size / 2 + (i + 1) * size / (inputShapes.length + 1);
            g.setColor(node.getConnectedInputs().contains(i) ? Color.GREEN : Color.RED);
            drawPortShape(g, inputShapes[i], px, py);
        }
        String[] outputShapes = node.getOutputShapes();
        for (int i = 0; i < outputShapes.length; i++) {
            int px = x + size / 2 + 10;
            int py = y - size / 2 + (i + 1) * size / (outputShapes.length + 1);
            g.setColor(Color.BLUE);
            drawPortShape(g, outputShapes[i], px, py);
        }
        boolean hasOutputConnected = false;
        for (IConnection conn : connections) {
            if (conn.getFromNode() == node) {
                hasOutputConnected = true;
                break;
            }
        }
        g.setColor(node.getConnectedInputs().isEmpty() && !hasOutputConnected ? Color.WHITE : Color.GREEN);
        g.fillOval(x - size / 2 - 5, y - size / 2 - 5, 10, 10);
        g.setColor(Color.BLACK);
        g.drawOval(x - size / 2 - 5, y - size / 2 - 5, 10, 10);
        // Draw node label and system badge
        g.setColor(Color.BLACK);
        g.drawString(node.getId(), x - 5, y);
        drawSystemBadge(g, node, x, y, size);
    }
    
    private Color getNodeColor(INode node) {
        if (node.isDamaged()) {
            return Color.DARK_GRAY;
        }
        
        if (node.isSpySystem()) {
            return new Color(186, 85, 211); // Medium Orchid
        } else if (node.isSabotageSystem()) {
            return new Color(220, 20, 60); // Crimson
        } else if (node.isVpnSystem()) {
            return new Color(30, 144, 255); // DodgerBlue
        } else if (node.isAntitrojanSystem()) {
            return new Color(46, 139, 87); // SeaGreen
        } else if (node.isDistributeSystem()) {
            return new Color(255, 215, 0); // Gold
        } else if (node.isMergeSystem()) {
            return new Color(255, 140, 0); // DarkOrange
        } else if (node.isMaliciousSystem()) {
            return new Color(128, 0, 0); // Maroon
        } else {
            return Color.LIGHT_GRAY;
        }
    }

    // Draw small glyph for each port shape
    private void drawPortShape(Graphics g, String shape, int px, int py) {
        if (shape == null) return;
        switch (shape) {
            case "triangle": {
                int[] xPoints = {px - 4, px + 4, px};
                int[] yPoints = {py - 4, py - 4, py + 4};
                g.fillPolygon(xPoints, yPoints, 3);
                break;
            }
            case "square": {
                g.fillRect(px - 4, py - 4, 8, 8);
                break;
            }
            case "circle": {
                g.fillOval(px - 4, py - 4, 8, 8);
                break;
            }
            case "confidential_4": {
                int[] xPts = {px, px + 4, px, px - 4};
                int[] yPts = {py - 4, py, py + 4, py};
                g.fillPolygon(xPts, yPts, 4);
                break;
            }
            case "confidential_6": {
                int[] xPts6 = {px, px + 4, px, px - 4};
                int[] yPts6 = {py - 4, py, py + 4, py};
                g.fillPolygon(xPts6, yPts6, 4);
                g.setColor(Color.WHITE);
                g.drawLine(px - 2, py, px + 2, py);
                break;
            }
            case "bulky_8": {
                g.fillRect(px - 5, py - 5, 10, 10);
                break;
            }
            case "bulky_10": {
                g.fillRect(px - 6, py - 6, 12, 12);
                g.setColor(Color.WHITE);
                g.drawRect(px - 3, py - 3, 6, 6);
                break;
            }
            default: {
                g.fillOval(px - 3, py - 3, 6, 6);
            }
        }
    }

    private void drawSystemBadge(Graphics g, INode node, int x, int y, int size) {
        String label = null;
        Color bg = Color.BLACK;
        if (node.isVpnSystem()) { label = "VPN"; bg = new Color(0, 102, 204); }
        else if (node.isSpySystem()) { label = "SPY"; bg = new Color(128, 0, 128); }
        else if (node.isAntitrojanSystem()) { label = "AT"; bg = new Color(0, 128, 0); }
        else if (node.isDistributeSystem()) { label = "DIST"; bg = new Color(184, 134, 11); }
        else if (node.isMergeSystem()) { label = "MRG"; bg = new Color(210, 105, 30); }
        else if (node.isMaliciousSystem()) { label = "MAL"; bg = new Color(139, 0, 0); }

        if (label != null) {
            int bx = x + size / 2 - 24;
            int by = y - size / 2 + 4;
            g.setColor(bg);
            g.fillRoundRect(bx, by, 22, 12, 6, 6);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 9));
            g.drawString(label, bx + 3, by + 10);
        }
    }
}
