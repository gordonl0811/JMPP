package tech.gordonlee.jmpp.components.rewriters;

import com.lmax.disruptor.dsl.Disruptor;
import io.pkts.packet.IPPacket;
import io.pkts.packet.IPv4Packet;
import io.pkts.packet.Packet;
import io.pkts.protocol.Protocol;
import tech.gordonlee.jmpp.components.Component;
import tech.gordonlee.jmpp.utils.PacketEvent;

import java.io.IOException;

import static tech.gordonlee.jmpp.utils.Utils.startDisruptor;

/**
 * Rewrites the Layer 3 source and destination addresses.
 * The class does not handle invalid addresses; it is the
 * user's responsibility to set the ports correctly.
 */
public class IPAddressRewriter extends Component {

    private final Disruptor<PacketEvent> inputDisruptor;
    private final Disruptor<PacketEvent> outputDisruptor;
    private final String srcAddr;
    private final String dstAddr;

    /**
     * Default constructor. Set srcAddr and/or dstAddr to
     * a null String to retain the original address.
     * @param inputDisruptor Receives packets from this Disruptor
     * @param outputDisruptor Sends packets to this Disruptor
     * @param srcAddr the new source IP address, null if unchanged
     * @param dstAddr the new destination IP address, null if unchanged
     */
    public IPAddressRewriter(Disruptor<PacketEvent> inputDisruptor, Disruptor<PacketEvent> outputDisruptor, String srcAddr, String dstAddr) {
        this.inputDisruptor = inputDisruptor;
        this.outputDisruptor = outputDisruptor;
        inputDisruptor.handleEventsWith(this);
        this.srcAddr = srcAddr;
        this.dstAddr = dstAddr;
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
        Protocol layerThreeProtocol = packet.hasProtocol(Protocol.IPv4) ? Protocol.IPv4 : Protocol.IPv6;
        IPPacket layerThreePacket = (IPPacket) packet.getPacket(layerThreeProtocol);

        // Set addresses if they have been defined
        if (srcAddr != null) {
            layerThreePacket.setSourceIP(srcAddr);
        }
        if (dstAddr != null) {
            layerThreePacket.setDestinationIP(dstAddr);
        }

        if (layerThreeProtocol == Protocol.IPv4) {
            ((IPv4Packet) layerThreePacket).reCalculateChecksum();
        }

        outputDisruptor.publishEvent((event, sequence) -> event.setValue(packet));
    }
}
