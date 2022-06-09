package tech.gordonlee.jmpp.components.filters;

import com.lmax.disruptor.dsl.Disruptor;
import io.pkts.packet.Packet;
import io.pkts.protocol.Protocol;
import tech.gordonlee.jmpp.components.Component;
import tech.gordonlee.jmpp.utils.PacketEvent;

import java.io.IOException;

import static tech.gordonlee.jmpp.utils.Utils.startDisruptor;

public class IPv6Filter extends Component {

    private final Disruptor<PacketEvent> inputDisruptor;
    private final Disruptor<PacketEvent> outputDisruptor;

    public IPv6Filter(Disruptor<PacketEvent> inputDisruptor, Disruptor<PacketEvent> outputDisruptor) {
        this.inputDisruptor = inputDisruptor;
        this.outputDisruptor = outputDisruptor;
        inputDisruptor.handleEventsWith(this);
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
        if (packet.hasProtocol(Protocol.IPv6)) {
            outputDisruptor.publishEvent((event, sequence) -> event.setValue(packet));
        }
    }
}
