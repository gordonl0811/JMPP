package tech.gordonlee.jmpp.components.outputters;

import com.lmax.disruptor.dsl.Disruptor;
import io.pkts.PcapOutputStream;
import io.pkts.frame.PcapGlobalHeader;
import io.pkts.packet.Packet;
import tech.gordonlee.jmpp.components.Component;
import tech.gordonlee.jmpp.utils.PacketEvent;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static tech.gordonlee.jmpp.utils.Utils.startDisruptor;

/**
 * Writers copy packets to Pcap files.
 * The frequent I/O generates significant overhead for the Processor,
 * so this component is most useful when being used to verify the
 * functional correctness of a processor.
 */
public class Writer extends Component {

    private final Disruptor<PacketEvent> inputDisruptor;
    private final PcapOutputStream output;

    /**
     * Default constructor.
     * @param inputDisruptor Receives packets from this Disruptor
     * @param dst Filename to write packets to
     * @throws FileNotFoundException Invalid address to the pcap given
     */
    public Writer(Disruptor<PacketEvent> inputDisruptor, String dst) throws FileNotFoundException {
        this.inputDisruptor = inputDisruptor;
        inputDisruptor.handleEventsWith(this);
        this.output = PcapOutputStream.create(
                PcapGlobalHeader.createDefaultHeader(),
                new FileOutputStream(dst)
        );
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
    public void process(Packet packet) throws IOException {
//        System.out.println("Processed " + getPacketCount());
        output.write(packet);
    }

}
