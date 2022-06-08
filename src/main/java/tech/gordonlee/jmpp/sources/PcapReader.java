package tech.gordonlee.jmpp.sources;

import com.lmax.disruptor.dsl.Disruptor;
import io.pkts.Pcap;
import tech.gordonlee.jmpp.utils.PacketEvent;

import java.io.IOException;

import static tech.gordonlee.jmpp.utils.Utils.startDisruptor;

public class PcapReader {

    private final Pcap source;
    private final Disruptor<PacketEvent> outputDisruptor;

    public PcapReader(String source, Disruptor<PacketEvent> outputDisruptor)
            throws IOException {
        this.source = Pcap.openStream(source);
        this.outputDisruptor = outputDisruptor;
    }

    public void initialize() {
        startDisruptor(outputDisruptor);
    }

    public void shutdown() {
        outputDisruptor.shutdown();
    }

    public void start() {

        try {
            // Load the packets into the RingBuffer
            this.source.loop(packet -> {
                outputDisruptor.publishEvent((event, sequence) -> event.setValue(packet));
                return true;
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        source.close();

    }

}
