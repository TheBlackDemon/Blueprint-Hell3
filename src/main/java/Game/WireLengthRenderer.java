package Game;

import java.awt.*;
class WireLengthRenderer {
    private final IWireLengthManager wireLengthManager;

    public WireLengthRenderer(IWireLengthManager wireLengthManager) {
        this.wireLengthManager = wireLengthManager;
    }

    public void render(Graphics g, int panelWidth) {
        double remainingWireLength = wireLengthManager.getRemainingWireLength();
        int maxBarWidth = GameConfig.WIRE_BAR_MAX_WIDTH;
        int barHeight = GameConfig.WIRE_BAR_HEIGHT;
        int barX = panelWidth - GameConfig.WIRE_BAR_X_OFFSET;
        int barY = GameConfig.WIRE_BAR_Y;
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(barX, barY, maxBarWidth, barHeight);
        int remainingWidth = (int) ((remainingWireLength / GameConfig.MAX_WIRE_LENGTH) * maxBarWidth);
        g.setColor(remainingWireLength <= GameConfig.LOW_WIRE_THRESHOLD ? Color.RED : Color.GREEN);
        g.fillRect(barX, barY, remainingWidth, barHeight);
        g.setColor(Color.BLACK);
        g.drawRect(barX, barY, maxBarWidth, barHeight);
    }
}
