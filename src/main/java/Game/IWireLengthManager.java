package Game;

public interface IWireLengthManager {
    double getTotalWireLength();

    double getRemainingWireLength();

    boolean canAddWireLength(double additionalLength, IConnection excludeConn);
}
