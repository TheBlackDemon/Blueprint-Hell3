package Game;

import java.util.List;

public class WireLengthManager implements IWireLengthManager {
    private final List<IConnection> connections;

    public WireLengthManager(List<IConnection> connections) {
        this.connections = connections;
    }

    @Override
    public double getTotalWireLength() {
        double total = 0.0;
        for (IConnection conn : connections) {
            total += conn.getLength();
        }
        return total;
    }

    @Override
    public double getRemainingWireLength() {
        return GameConfig.MAX_WIRE_LENGTH - getTotalWireLength();
    }

    @Override
    public boolean canAddWireLength(double additionalLength, IConnection excludeConn) {
        double currentTotal = 0.0;
        for (IConnection conn : connections) {
            if (conn != excludeConn) {
                currentTotal += conn.getLength();
            }
        }
        return currentTotal + additionalLength <= GameConfig.MAX_WIRE_LENGTH;
    }
}
