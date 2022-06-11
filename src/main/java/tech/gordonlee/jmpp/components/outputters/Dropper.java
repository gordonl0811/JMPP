package tech.gordonlee.jmpp.components.outputters;

import com.lmax.disruptor.dsl.Disruptor;
import io.pkts.packet.Packet;
import tech.gordonlee.jmpp.components.Component;
import tech.gordonlee.jmpp.utils.PacketEvent;

import static tech.gordonlee.jmpp.utils.Utils.startDisruptor;

/**
 * Droppers consume Packets without performing any
 * operations on them. These are useful for discarding
 * packets that are not needed by the processor.
 */
public class Dropper extends Component {

    private final Disruptor<PacketEvent> inputDisruptor;

    /**
     * Default constructor.
     * @param inputDisruptor Receives packets from this Disruptor
     */
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
