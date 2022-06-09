package tech.gordonlee.jmpp.components;

import com.lmax.disruptor.EventHandler;
import io.pkts.packet.Packet;
import tech.gordonlee.jmpp.utils.PacketEvent;

import java.io.IOException;

/**
 * Components are responsible for transforming Packets.
 * The onEvent function is called by consuming Disruptors,
 * which will call a function that carries out the operation.
 * Component subclasses are responsible for defining this operation.
 */
public abstract class Component implements EventHandler<PacketEvent> {

    private long packetCount = 0;

    public long getPacketCount() {
        return packetCount;
    }

    public abstract void initialize();

    public abstract void shutdown();

    public abstract void process(Packet packet) throws IOException;

    /**
     * On top of performing their defined operations, Components
     * keep track of how many packets are passed through their
     * system. This is useful for monitoring the behaviour of the
     * processor using these Components.
     * @param packetEvent
     * @param l
     * @param b
     * @throws IOException
     */
    @Override
    public final void onEvent(PacketEvent packetEvent, long l, boolean b) throws IOException {
        process(packetEvent.getValue());
        packetCount++;
    }
}
