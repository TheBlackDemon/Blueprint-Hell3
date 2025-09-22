package view;

import Game.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
public class AmmunitionPanel extends JPanel {
    private final ControllableReferenceSystem system;
    private final AmmunitionSelectionListener listener;
    private List<AmmunitionManager.AmmunitionStatus> ammunitionStatuses;
    private AmmunitionType selectedType;
    private final Font titleFont;
    private final Font countFont;
    private final Font cooldownFont;

    public interface AmmunitionSelectionListener {
        void onAmmunitionSelected(AmmunitionType type, ControllableReferenceSystem system);
    }

    public AmmunitionPanel(ControllableReferenceSystem system, AmmunitionSelectionListener listener) {
        this.system = system;
        this.listener = listener;
        this.selectedType = null;
        this.titleFont = new Font("Arial", Font.BOLD, 14);
        this.countFont = new Font("Arial", Font.PLAIN, 12);
        this.cooldownFont = new Font("Arial", Font.ITALIC, 10);
        
        setPreferredSize(new Dimension(GameConfig.AMMUNITION_PANEL_WIDTH, GameConfig.AMMUNITION_PANEL_HEIGHT));
        setBackground(new Color(240, 240, 240, 240));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        addMouseListener(new AmmunitionMouseListener());
        updateAmmunitionStatuses();
    }

    public void updateAmmunitionStatuses() {
        this.ammunitionStatuses = system.getAmmunitionStatuses();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setFont(titleFont);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Ammunition - " + system.getId(), 5, 20);

        int yOffset = 35;
        int itemSize = GameConfig.AMMUNITION_ITEM_SIZE;
        int spacing = GameConfig.AMMUNITION_ITEM_SPACING;
        int itemsPerRow = 2;
        int currentRow = 0;
        int currentCol = 0;

        for (AmmunitionManager.AmmunitionStatus status : ammunitionStatuses) {
            int x = 5 + currentCol * (itemSize + spacing);
            int y = yOffset + currentRow * (itemSize + spacing);

            drawAmmunitionItem(g2d, status, x, y, itemSize);

            currentCol++;
            if (currentCol >= itemsPerRow) {
                currentCol = 0;
                currentRow++;
            }
        }
    }

    private void drawAmmunitionItem(Graphics2D g2d, AmmunitionManager.AmmunitionStatus status, 
                                  int x, int y, int size) {
        AmmunitionType type = status.getType();
        Color baseColor = type.getColor();
        
        Color displayColor = baseColor;
        if (status.getStatus() == AmmunitionManager.AmmunitionStatus.Status.OUT_OF_AMMO) {
            displayColor = Color.RED;
        } else if (status.getStatus() == AmmunitionManager.AmmunitionStatus.Status.ON_COOLDOWN) {
            displayColor = Color.GRAY;
        }

        if (selectedType == type) {
            g2d.setColor(Color.YELLOW);
            g2d.fillRect(x - 2, y - 2, size + 4, size + 4);
        }

        g2d.setColor(displayColor);
        g2d.fillOval(x, y, size, size);

        g2d.setColor(Color.BLACK);
        g2d.setFont(countFont);
        String countText = String.valueOf(status.getCount());
        FontMetrics fm = g2d.getFontMetrics();
        int countX = x + (size - fm.stringWidth(countText)) / 2;
        int countY = y + size + 15;
        g2d.drawString(countText, countX, countY);

        if (status.getStatus() == AmmunitionManager.AmmunitionStatus.Status.ON_COOLDOWN) {
            long remaining = status.getCooldownRemaining();
            int seconds = (int) (remaining / 1000);
            int milliseconds = (int) ((remaining % 1000) / 100);
            
            g2d.setColor(Color.RED);
            g2d.setFont(cooldownFont);
            String cooldownText = seconds + "." + milliseconds + "s";
            FontMetrics cooldownFm = g2d.getFontMetrics();
            int cooldownX = x + (size - cooldownFm.stringWidth(cooldownText)) / 2;
            int cooldownY = y + size + 25;
            g2d.drawString(cooldownText, cooldownX, cooldownY);
        }

        if (status.hasVisualEffect()) {
            drawCooldownCompletionEffect(g2d, x, y, size);
        }

        g2d.setColor(Color.BLACK);
        g2d.setFont(cooldownFont);
        String typeName = type.getDisplayName();
        FontMetrics nameFm = g2d.getFontMetrics();
        int nameX = x + (size - nameFm.stringWidth(typeName)) / 2;
        int nameY = y + size + 35;
        g2d.drawString(typeName, nameX, nameY);
    }

    private void drawCooldownCompletionEffect(Graphics2D g2d, int x, int y, int size) {
        g2d.setColor(new Color(0, 255, 0, 100));
        g2d.setStroke(new BasicStroke(3));
        
        int effectSize = size + 10;
        int effectX = x - 5;
        int effectY = y - 5;
        
        g2d.drawOval(effectX, effectY, effectSize, effectSize);
        
        effectSize += 10;
        effectX -= 5;
        effectY -= 5;
        g2d.setColor(new Color(0, 255, 0, 50));
        g2d.drawOval(effectX, effectY, effectSize, effectSize);
    }

    private class AmmunitionMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            AmmunitionType clickedType = getAmmunitionTypeAt(e.getX(), e.getY());
            if (clickedType != null && listener != null) {
                selectedType = clickedType;
                listener.onAmmunitionSelected(clickedType, system);
                repaint();
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            AmmunitionType hoveredType = getAmmunitionTypeAt(e.getX(), e.getY());
            if (hoveredType != null) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else {
                setCursor(Cursor.getDefaultCursor());
            }
        }
    }

    private AmmunitionType getAmmunitionTypeAt(int x, int y) {
        int itemSize = GameConfig.AMMUNITION_ITEM_SIZE;
        int spacing = GameConfig.AMMUNITION_ITEM_SPACING;
        int itemsPerRow = 2;
        int startY = 35;
        int currentRow = 0;
        int currentCol = 0;

        for (AmmunitionManager.AmmunitionStatus status : ammunitionStatuses) {
            int itemX = 5 + currentCol * (itemSize + spacing);
            int itemY = startY + currentRow * (itemSize + spacing);

            if (x >= itemX && x <= itemX + itemSize && 
                y >= itemY && y <= itemY + itemSize) {
                return status.getType();
            }

            currentCol++;
            if (currentCol >= itemsPerRow) {
                currentCol = 0;
                currentRow++;
            }
        }

        return null;
    }

}
