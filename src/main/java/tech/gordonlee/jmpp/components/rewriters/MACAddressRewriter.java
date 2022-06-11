package tech.gordonlee.jmpp.components.rewriters;

import com.lmax.disruptor.dsl.Disruptor;
import io.pkts.packet.MACPacket;
import io.pkts.packet.Packet;
import io.pkts.protocol.Protocol;
import tech.gordonlee.jmpp.components.Component;
import tech.gordonlee.jmpp.utils.PacketEvent;

import java.io.IOException;

import static tech.gordonlee.jmpp.utils.Utils.startDisruptor;

/**
 * Rewrites the Layer 2 source and destination addresses.
 * The class does not handle invalid addresses; it is the
 * user's responsibility to set the ports correctly.
 */
public class MACAddressRewriter extends Component {

    private final Disruptor<PacketEvent> inputDisruptor;
    private final Disruptor<PacketEvent> outputDisruptor;
    private final String srcAddr;
    private final String dstAddr;

    /**
     * Default constructor. Set srcAddr and/or dstAddr to
     * a null String to retain the original address.
     * @param inputDisruptor Receives packets from this Disruptor
     * @param outputDisruptor Sends packets to this Disruptor
     * @param srcAddr the new source MAC address, null if unchanged
     * @param dstAddr the new destination MAC address, null if unchanged
     */
    public MACAddressRewriter(Disruptor<PacketEvent> inputDisruptor, Disruptor<PacketEvent> outputDisruptor, String srcAddr, String dstAddr) {
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
        MACPacket macPacket = (MACPacket) packet.getPacket(Protocol.ETHERNET_II);

        if (srcAddr != null) {
            macPacket.setSourceMacAddress(srcAddr);
        }

        if (dstAddr != null) {
            macPacket.setDestinationMacAddress(dstAddr);
        }

        outputDisruptor.publishEvent((event, sequence) -> event.setValue(packet));
    }
}
