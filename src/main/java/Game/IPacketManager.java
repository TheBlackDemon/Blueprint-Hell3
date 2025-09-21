package Game;

import java.util.List;

public interface IPacketManager {
    void stepForward();

    void stepBackward();

    List<Packet> getPackets();

    void resetPackets();
}
