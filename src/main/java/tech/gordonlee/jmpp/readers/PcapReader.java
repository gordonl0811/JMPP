package tech.gordonlee.jmpp.readers;

import com.lmax.disruptor.dsl.Disruptor;
import io.pkts.Pcap;
import tech.gordonlee.jmpp.utils.PacketEvent;

import java.io.IOException;

import static tech.gordonlee.jmpp.utils.Utils.startDisruptor;

/**
 * Provides Packets from a Pcap file.
 */
public class PcapReader implements Reader {

    private final Pcap source;
    private final Disruptor<PacketEvent> outputDisruptor;

    /**
     * Default constructor
     * @param source filepath to the Pcap providing the packets
     * @param outputDisruptor the buffer to send packets to
     * @throws IOException errors related to file opening
     */
    public PcapReader(String source, Disruptor<PacketEvent> outputDisruptor)
            throws IOException {
        this.source = Pcap.openStream(source);
        this.outputDisruptor = outputDisruptor;
    }

    /**
     * Prepares the output buffer to receive packets.
     */
    @Override
    public void initialize() {
        startDisruptor(outputDisruptor);
    }

    /**
     * Begin reading the Pcap and sending packets to the output buffer.
     */
    @Override
    public void start() {

        try {
            // Publish the events to the RingBuffer
            this.source.loop(packet -> {
                outputDisruptor.publishEvent((event, sequence) -> event.setValue(packet));
                return true;
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        source.close();

    }

    /**
     * Shut down the output Disruptor.
     */
    @Override
    public void shutdown() {
        outputDisruptor.shutdown();
    }

}
