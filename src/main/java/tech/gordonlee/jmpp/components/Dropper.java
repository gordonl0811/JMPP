package tech.gordonlee.jmpp.components;

import com.lmax.disruptor.dsl.Disruptor;
import io.pkts.packet.Packet;
import tech.gordonlee.jmpp.utils.PacketEvent;

import static tech.gordonlee.jmpp.utils.Utils.startDisruptor;

public class Dropper extends Component {

    private final Disruptor<PacketEvent> inputDisruptor;

    public Dropper(Disruptor<PacketEvent> inputDisruptor) {
        this.inputDisruptor = inputDisruptor;
        inputDisruptor.handleEventsWith(this);
    }

    @Override
    public void initialize() {
        startDisruptor(inputDisruptor);
    }

    @Override
    public void shutdown() {
        inputDisruptor.shutdown();
    }


    @Override
    public void process(Packet packet) {
//        System.out.println("Processed " + getPacketCount());
    }
}
