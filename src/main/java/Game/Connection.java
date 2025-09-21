package Game;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

class Connection implements IConnection {
    private final INode fromNode;
    private final int fromPort;
    private final INode toNode;
    private final int toPort;
    private final java.util.List<Point> waypoints;
    private final java.util.List<BendPoint> bendPoints;
    private Packet packet;
    private static final int MAX_BEND_POINTS = 3;
    private static final int BEND_POINT_RADIUS = 50;
    private int bulkyPacketPasses = 0;
    private boolean isDestroyed = false;

    public Connection(INode fromNode, int fromPort, INode toNode, int toPort) {
        this.fromNode = fromNode;
        this.fromPort = fromPort;
        this.toNode = toNode;
        this.toPort = toPort;
        this.waypoints = new ArrayList<>();
        this.bendPoints = new ArrayList<>();
    }
    
    @Override
    public String getId() {
        return fromNode.getId() + "_" + toNode.getId() + "_" + fromPort + "_" + toPort;
    }

    @Override
    public java.util.List<Point> getPath() {
        java.util.List<Point> path = new ArrayList<>();
        path.add(fromNode.getPortPosition("output", fromPort));
        path.addAll(waypoints);
        path.add(toNode.getPortPosition("input", toPort));
        return path;
    }

    @Override
    public double getLength() {
        java.util.List<Point> path = getDetailedPath();
        double length = 0.0;
        for (int i = 0; i < path.size() - 1; i++) {
            Point p1 = path.get(i);
            Point p2 = path.get(i + 1);
            length += Math.hypot(p2.x - p1.x, p2.y - p1.y);
        }
        return length;
    }

    @Override
    public boolean isNearLine(int mx, int my) {
        List<Point> points = getPath();
        for (int i = 0; i < points.size() - 1; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get(i + 1);
            double dist = pointToLineDistance(mx, my, p1.x, p1.y, p2.x, p2.y);
            if (dist < 5) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Point getNearestWaypoint(int mx, int my) {
        for (Point wp : waypoints) {
            if (Math.hypot(mx - wp.x, my - wp.y) < 8) {
                return wp;
            }
        }
        return null;
    }

    private double pointToLineDistance(int px, int py, int x1, int y1, int x2, int y2) {
        double length = Math.hypot(x2 - x1, y2 - y1);
        if (length == 0) return Math.hypot(px - x1, py - y1);
        double t = Math.max(0, Math.min(1, ((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1)) / (length * length)));
        double projectionX = x1 + t * (x2 - x1);
        double projectionY = y1 + t * (y2 - y1);
        return Math.hypot(px - projectionX, py - projectionY);
    }
    
    @Override
    public List<BendPoint> getBendPoints() {
        return new ArrayList<>(bendPoints);
    }
    
    @Override
    public boolean canAddBendPoint() {
        return bendPoints.size() < MAX_BEND_POINTS;
    }
    
    @Override
    public boolean addBendPoint(int x, int y) {
        if (!canAddBendPoint()) {
            return false;
        }
        BendPoint newBendPoint = new BendPoint(x, y, BEND_POINT_RADIUS);
        if (wouldCauseIntersection(newBendPoint)) {
            return false;
        }
        bendPoints.add(newBendPoint);
        return true;
    }
    
    @Override
    public BendPoint getNearestBendPoint(int mx, int my) {
        for (BendPoint bp : bendPoints) {
            if (bp.isOverBendPoint(mx, my)) {
                return bp;
            }
        }
        return null;
    }
    
    @Override
    public boolean removeBendPoint(BendPoint bendPoint) {
        return bendPoints.remove(bendPoint);
    }
    
    @Override
    public boolean intersectsWithNode(INode node) {
        List<Point> path = getDetailedPath();
        for (int i = 0; i < path.size() - 1; i++) {
            Point p1 = path.get(i);
            Point p2 = path.get(i + 1);
            if (lineIntersectsNode(p1, p2, node)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public List<Point> getDetailedPath() {
        List<Point> detailedPath = new ArrayList<>();
        Point start = fromNode.getPortPosition("output", fromPort);
        Point end = toNode.getPortPosition("input", toPort);
        detailedPath.add(start);
        detailedPath.addAll(waypoints);
        if (!bendPoints.isEmpty()) {
            List<Point> allPoints = new ArrayList<>();
            allPoints.addAll(waypoints);
            List<BendPoint> sortedBendPoints = new ArrayList<>(bendPoints);
            sortedBendPoints.sort((bp1, bp2) -> {
                double dist1 = getDistanceAlongLine(bp1.getPosition());
                double dist2 = getDistanceAlongLine(bp2.getPosition());
                return Double.compare(dist1, dist2);
            });
            for (BendPoint bp : sortedBendPoints) {
                allPoints.add(bp.getPosition());
            }
            allPoints.sort((p1, p2) -> {
                double dist1 = getDistanceAlongLine(p1);
                double dist2 = getDistanceAlongLine(p2);
                return Double.compare(dist1, dist2);
            });
            detailedPath.addAll(allPoints);
        }
        detailedPath.add(end);
        return detailedPath;
    }
    
    private boolean wouldCauseIntersection(BendPoint newBendPoint) {
        bendPoints.add(newBendPoint);
        boolean intersects = intersectsWithAnyNode();
        bendPoints.remove(bendPoints.size() - 1);
        return intersects;
    }
    
    private boolean intersectsWithAnyNode() {
        if (intersectsWithNode(fromNode) || intersectsWithNode(toNode)) {
            return true;
        }
        return false;
    }
    
    public boolean intersectsWithAnyNode(java.util.List<INode> allNodes) {
        for (INode node : allNodes) {
            if (node != fromNode && node != toNode && intersectsWithNode(node)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean lineIntersectsNode(Point p1, Point p2, INode node) {
        int nodeX = node.getX();
        int nodeY = node.getY();
        int nodeSize = 40;
        int minX = Math.min(p1.x, p2.x);
        int maxX = Math.max(p1.x, p2.x);
        int minY = Math.min(p1.y, p2.y);
        int maxY = Math.max(p1.y, p2.y);
        
        return !(maxX < nodeX || minX > nodeX + nodeSize || 
                maxY < nodeY || minY > nodeY + nodeSize);
    }
    
    private double getDistanceAlongLine(Point point) {
        Point start = fromNode.getPortPosition("output", fromPort);
        Point end = toNode.getPortPosition("input", toPort);
        double lineLength = Math.hypot(end.x - start.x, end.y - start.y);
        if (lineLength == 0) return 0;
        double t = ((point.x - start.x) * (end.x - start.x) + (point.y - start.y) * (end.y - start.y)) / (lineLength * lineLength);
        t = Math.max(0, Math.min(1, t));
        return t * lineLength;
    }
    
    public void incrementBulkyPacketPasses() {
        bulkyPacketPasses++;
        if (bulkyPacketPasses >= GameConfig.MAX_BULKY_PASSES_PER_WIRE) {
            isDestroyed = true;
            System.out.println("Wire from " + fromNode.getId() + " to " + toNode.getId() + " destroyed by bulky packets");
        }
    }

    @Override
    public void setPacket(Packet packet) {
        this.packet = packet;
    }

    @Override
    public Packet getPacket() {
        return packet;
    }

    @Override
    public INode getFromNode() {
        return fromNode;
    }

    @Override
    public int getFromPort() {
        return fromPort;
    }

    @Override
    public INode getToNode() {
        return toNode;
    }

    @Override
    public int getToPort() {
        return toPort;
    }

    @Override
    public List<Point> getWaypoints() {
        return waypoints;
    }
    
    public int getBulkyPacketPasses() {
        return bulkyPacketPasses;
    }
    
    public boolean isDestroyed() {
        return isDestroyed;
    }
    
    public void setDestroyed(boolean destroyed) {
        this.isDestroyed = destroyed;
    }
    
    public void setBulkyPacketPasses(int bulkyPacketPasses) {
        this.bulkyPacketPasses = bulkyPacketPasses;
    }
}
