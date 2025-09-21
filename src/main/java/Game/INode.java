package Game;

import java.awt.*;
import java.util.Set;

public interface INode {
    int getX();

    int getY();

    String getId();

    String[] getInputShapes();

    String[] getOutputShapes();

    Set<Integer> getConnectedInputs();

    Point getPortPosition(String portType, int portIndex);

    boolean isOverPort(int mx, int my, String portType, int portIndex);

    boolean isOverNode(int mx, int my);

    void setPosition(int x, int y);
    
    boolean isDamaged();
    
    void setDamaged(boolean damaged);
    
    boolean canOperate();
    
    boolean isSpySystem();
    
    boolean isSabotageSystem();
    
    boolean isVpnSystem();
    
    boolean isAntitrojanSystem();
    
    boolean isDistributeSystem();
    
    boolean isMergeSystem();
    
    boolean isMaliciousSystem();
    
    boolean canAntitrojanOperate();
    
    void setLastAntitrojanOperation(long time);
    
    String getSystemType();
    
    boolean isControllableSystem();
    
    boolean isUncontrollableSystem();
}
