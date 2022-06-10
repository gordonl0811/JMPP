package tech.gordonlee.jmpp.examples;

import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import tech.gordonlee.jmpp.AbstractPacketProcessor;
import tech.gordonlee.jmpp.PacketProcessor;
import tech.gordonlee.jmpp.components.Component;
import tech.gordonlee.jmpp.components.filters.TCPFilter;
import tech.gordonlee.jmpp.components.filters.UDPFilter;
import tech.gordonlee.jmpp.components.outputters.Writer;
import tech.gordonlee.jmpp.components.rewriters.IPAddressRewriter;
import tech.gordonlee.jmpp.readers.PcapReader;
import tech.gordonlee.jmpp.readers.Reader;
import tech.gordonlee.jmpp.utils.PacketEvent;

import java.io.IOException;
import java.util.List;

public class LayerFourReroutingProcessor extends AbstractPacketProcessor {

    public final static String TCP_PACKET_DESTINATION = "192.168.0.11";
    public final static String UDP_PACKET_DESTINATION = "192.168.0.22";

    private final PcapReader reader;

    private final TCPFilter tcpFilter;
    private final IPAddressRewriter tcpAddressRewriter;

    private final UDPFilter udpFilter;
    private final IPAddressRewriter udpAddressRewriter;

    private final Writer writer;

    private final long expectedPackets;

    public LayerFourReroutingProcessor(int bufferSize, String srcPcap, String dstPcap, long expectedPackets) throws IOException {

        Disruptor<PacketEvent> readerOutputDisruptor = new Disruptor<>(PacketEvent::new, bufferSize, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, new YieldingWaitStrategy());
        Disruptor<PacketEvent> filteredTcpPackets = new Disruptor<>(PacketEvent::new, bufferSize, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, new YieldingWaitStrategy());
        Disruptor<PacketEvent> filteredUdpPackets = new Disruptor<>(PacketEvent::new, bufferSize, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, new YieldingWaitStrategy());
        Disruptor<PacketEvent> writerInputDisruptor = new Disruptor<>(PacketEvent::new, bufferSize, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, new YieldingWaitStrategy());

        this.reader = new PcapReader(srcPcap, readerOutputDisruptor);

        this.tcpFilter = new TCPFilter(readerOutputDisruptor, filteredTcpPackets);
        this.tcpAddressRewriter = new IPAddressRewriter(filteredTcpPackets, writerInputDisruptor, null, TCP_PACKET_DESTINATION);

        this.udpFilter = new UDPFilter(readerOutputDisruptor, filteredUdpPackets);
        this.udpAddressRewriter = new IPAddressRewriter(filteredUdpPackets, writerInputDisruptor, null, UDP_PACKET_DESTINATION);

        this.writer = new Writer(writerInputDisruptor, dstPcap);

        this.expectedPackets = expectedPackets;
    }

    @Override
    protected List<Reader> setReaders() {
        return List.of(reader);
    }

    @Override
    protected List<Component> setComponents() {
        return List.of(tcpFilter, tcpAddressRewriter, udpFilter, udpAddressRewriter, writer);
    }

    @Override
    protected boolean shouldTerminate() {
        return writer.getPacketCount() >= expectedPackets;
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        // Instantiate the Processor
        PacketProcessor processor = new LayerFourReroutingProcessor(
                1024,
                "src/main/resources/inputs/input_10.pcap",
                "src/main/resources/outputs/example.pcap",
                10
        );
        processor.initialize();

        // Process the packets
        processor.start();
        processor.shutdown();

    }
}
