package tech.gordonlee.jmpp.components.rewriters;

import com.lmax.disruptor.dsl.Disruptor;
import io.pkts.packet.Packet;
import io.pkts.packet.TransportPacket;
import io.pkts.protocol.Protocol;
import tech.gordonlee.jmpp.components.Component;
import tech.gordonlee.jmpp.utils.PacketEvent;

import java.io.IOException;

import static tech.gordonlee.jmpp.utils.Utils.startDisruptor;

/**
 * Rewrites the Layer 3 source and destination ports.
 * The class does not handle invalid ports; it is the
 * user's responsibility to set the ports correctly.
 */
public class PortRewriter extends Component {

    private final Disruptor<PacketEvent> inputDisruptor;
    private final Disruptor<PacketEvent> outputDisruptor;
    private final int srcPort;
    private final int dstPort;

    /**
     * Default constructor. Set srcPort and/or dstPort to a
     * negative value (i.e. -1) to retain the original address.
     * @param inputDisruptor
     * @param outputDisruptor
     * @param srcPort -1 if unchanged
     * @param dstPort -1 if unchanged
     */
    public PortRewriter(Disruptor<PacketEvent> inputDisruptor, Disruptor<PacketEvent> outputDisruptor, int srcPort, int dstPort) {
        this.inputDisruptor = inputDisruptor;
        this.outputDisruptor = outputDisruptor;
        inputDisruptor.handleEventsWith(this);
        // Set srcPort/destPort to a negative value (i.e. -1) to retain their values
        this.srcPort = srcPort;
        this.dstPort = dstPort;
    }

    @Override
    public void initialize() {
        startDisruptor(inputDisruptor);
        startDisruptor(outputDisruptor);
    }

    @Override
    public void shutdown() {
        inputDisruptor.shutdown();
        outputDisruptor.shutdown();
    }

    @Override
    public void process(Packet packet) throws IOException {
        Protocol layerFourProtocol = packet.hasProtocol(Protocol.TCP) ? Protocol.TCP : Protocol.UDP;
        TransportPacket layerFourPacket = (TransportPacket) packet.getPacket(layerFourProtocol);

        // Set ports if they have been defined
        if (srcPort >= 0) {
            layerFourPacket.setSourcePort(srcPort);
        }
        if (dstPort >= 0) {
            layerFourPacket.setDestinationPort(dstPort);
        }

        // TODO: Checksums need to be recalculated, but the library does not support this.

        outputDisruptor.publishEvent((event, sequence) -> event.setValue(packet));
    }
}

