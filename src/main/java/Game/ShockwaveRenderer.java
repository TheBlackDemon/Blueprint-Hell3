package Game;

import java.awt.*;

class ShockwaveRenderer {
    public void render(Graphics g, Shockwave shockwave) {
        Point pos = shockwave.getPosition();
        double radius = shockwave.getRadius();
        float alpha = (float) (0.5 * (1.0 - radius / GameConfig.SHOCKWAVE_MAX_RADIUS));
        g.setColor(new Color(1.0f, 1.0f, 1.0f, Math.max(0.0f, Math.min(1.0f, alpha))));
        int diameter = (int) (radius * 2);
        g.fillOval(pos.x - (int) radius, pos.y - (int) radius, diameter, diameter);
    }
}
