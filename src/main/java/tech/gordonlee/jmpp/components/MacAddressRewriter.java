package tech.gordonlee.jmpp.components;

import com.lmax.disruptor.dsl.Disruptor;
import io.pkts.packet.MACPacket;
import io.pkts.packet.Packet;
import io.pkts.protocol.Protocol;
import tech.gordonlee.jmpp.utils.PacketEvent;

import java.io.IOException;

import static tech.gordonlee.jmpp.utils.Utils.startDisruptor;

/**
 *
 */
public class MacAddressRewriter extends Component {

    private final Disruptor<PacketEvent> inputDisruptor;
    private final Disruptor<PacketEvent> outputDisruptor;
    private final String srcAddr;
    private final String dstAddr;

    /**
     * Default constructor. Set srcAddr and/or dstAddr to
     * a null String to retain the original address.
     * @param inputDisruptor
     * @param outputDisruptor
     * @param srcAddr null if unchanged
     * @param dstAddr null if unchanged
     */
    public MacAddressRewriter(Disruptor<PacketEvent> inputDisruptor, Disruptor<PacketEvent> outputDisruptor, String srcAddr, String dstAddr) {
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
