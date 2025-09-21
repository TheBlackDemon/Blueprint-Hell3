package Game;

import java.awt.*;
import java.util.List;

public interface IConnection {
    String getId();
    
    void setPacket(Packet packet);

    Packet getPacket();

    INode getFromNode();

    int getFromPort();

    INode getToNode();

    int getToPort();

    java.util.List<Point> getWaypoints();

    List<Point> getPath();

    double getLength();

    boolean isNearLine(int mx, int my);

    Point getNearestWaypoint(int mx, int my);
    
    java.util.List<BendPoint> getBendPoints();
    
    boolean canAddBendPoint();
    
    boolean addBendPoint(int x, int y);
    
    BendPoint getNearestBendPoint(int mx, int my);
    
    boolean removeBendPoint(BendPoint bendPoint);
    
    boolean intersectsWithNode(INode node);
    
    boolean intersectsWithAnyNode(java.util.List<INode> allNodes);
    
    List<Point> getDetailedPath();
    
    void incrementBulkyPacketPasses();
    int getBulkyPacketPasses();
    boolean isDestroyed();
    void setDestroyed(boolean destroyed);
}
