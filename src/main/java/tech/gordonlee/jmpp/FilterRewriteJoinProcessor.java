package tech.gordonlee.jmpp;

import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import tech.gordonlee.jmpp.components.Component;
import tech.gordonlee.jmpp.components.Dropper;
import tech.gordonlee.jmpp.components.TcpUdpFilter;
import tech.gordonlee.jmpp.components.PortRewriter;
import tech.gordonlee.jmpp.readers.PcapReader;
import tech.gordonlee.jmpp.readers.Reader;
import tech.gordonlee.jmpp.utils.PacketEvent;

import java.io.IOException;
import java.util.List;

public class FilterRewriteJoinProcessor extends AbstractPacketProcessor {

    private final PcapReader reader;
    private final TcpUdpFilter filter;
    private final PortRewriter tcpRewriter;
    private final PortRewriter udpRewriter;
    private final Dropper dropper;

    private final long expectedPackets;

    public FilterRewriteJoinProcessor(int bufferSize, String source, int tcpSrcPort, int tcpDstPort, int udpSrcPort, int udpDstPort, long expectedPackets) throws IOException {
        Disruptor<PacketEvent> readerDisruptor = new Disruptor<>(PacketEvent::new, bufferSize, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, new YieldingWaitStrategy());
        Disruptor<PacketEvent> tcpDisruptor = new Disruptor<>(PacketEvent::new, bufferSize, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, new YieldingWaitStrategy());
        Disruptor<PacketEvent> udpDisruptor = new Disruptor<>(PacketEvent::new, bufferSize, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, new YieldingWaitStrategy());
        Disruptor<PacketEvent> rewriterDisruptor = new Disruptor<>(PacketEvent::new, bufferSize, DaemonThreadFactory.INSTANCE, ProducerType.MULTI, new YieldingWaitStrategy());

        this.reader = new PcapReader(source, readerDisruptor);
        this.filter = new TcpUdpFilter(readerDisruptor, tcpDisruptor, udpDisruptor);
        this.tcpRewriter = new PortRewriter(tcpDisruptor, rewriterDisruptor, tcpSrcPort, tcpDstPort);
        this.udpRewriter = new PortRewriter(udpDisruptor, rewriterDisruptor, udpSrcPort, udpDstPort);
        this.dropper = new Dropper(rewriterDisruptor);

        this.expectedPackets = expectedPackets;
    }

    @Override
    protected List<Reader> setReaders() {
        return List.of(reader);
    }

    @Override
    protected List<Component> setComponents() {
        return List.of(filter, tcpRewriter, udpRewriter, dropper);
    }

    @Override
    public boolean shouldTerminate() {
        return dropper.getPacketCount() >= expectedPackets;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        FilterRewriteJoinProcessor processor = new FilterRewriteJoinProcessor(
                1024,
                "src/main/resources/input_thousand.pcap",
                12,
                34,
                56,
                78,
                1000
        );

        processor.initialize();
        processor.start();
        processor.shutdown();
    }
}
