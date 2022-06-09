package tech.gordonlee.jmpp.components;

import com.lmax.disruptor.dsl.Disruptor;
import io.pkts.packet.Packet;
import io.pkts.protocol.Protocol;
import tech.gordonlee.jmpp.utils.PacketEvent;

import java.io.IOException;

import static tech.gordonlee.jmpp.utils.Utils.startDisruptor;

/**
 * Splits a stream of Packets into TCP and UDP
 * packets, sending them to their respective
 * buffers (defined by the constructor).
 */
public class TcpUdpFilter extends Component {

    private final Disruptor<PacketEvent> inputDisruptor;
    private final Disruptor<PacketEvent> tcpDisruptor;
    private final Disruptor<PacketEvent> udpDisruptor;

    public TcpUdpFilter(
            Disruptor<PacketEvent> inputDisruptor,
            Disruptor<PacketEvent> tcpDisruptor,
            Disruptor<PacketEvent> udpDisruptor) {
        this.inputDisruptor = inputDisruptor;
        this.tcpDisruptor = tcpDisruptor;
        this.udpDisruptor = udpDisruptor;
        inputDisruptor.handleEventsWith(this);
    }

    @Override
    public void initialize() {
        startDisruptor(inputDisruptor);
        startDisruptor(tcpDisruptor);
        startDisruptor(udpDisruptor);
    }

    @Override
    public void shutdown() {
        inputDisruptor.shutdown();
        tcpDisruptor.shutdown();
        udpDisruptor.shutdown();
    }

    @Override
    public void process(Packet packet) throws IOException {
        if (packet.hasProtocol(Protocol.TCP)) {
            tcpDisruptor.publishEvent((event, sequence) -> event.setValue(packet));
        } else if (packet.hasProtocol(Protocol.UDP)) {
            udpDisruptor.publishEvent((event, sequence) -> event.setValue(packet));
        }
    }

}
