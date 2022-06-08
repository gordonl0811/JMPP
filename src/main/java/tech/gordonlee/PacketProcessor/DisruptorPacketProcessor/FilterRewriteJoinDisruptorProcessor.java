package tech.gordonlee.PacketProcessor.DisruptorPacketProcessor;

import tech.gordonlee.PacketProcessor.DisruptorPacketProcessor.components.Dropper;
import tech.gordonlee.PacketProcessor.DisruptorPacketProcessor.components.Filter;
import tech.gordonlee.PacketProcessor.DisruptorPacketProcessor.components.LayerFourPortRewriter;
import tech.gordonlee.PacketProcessor.DisruptorPacketProcessor.components.ProcessorComponent;
import tech.gordonlee.PacketProcessor.DisruptorPacketProcessor.sources.PcapReader;
import tech.gordonlee.PacketProcessor.DisruptorPacketProcessor.utils.PacketEvent;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;

import java.io.IOException;
import java.util.List;

public class FilterRewriteJoinDisruptorProcessor extends AbstractDisruptorProcessor {

    private final PcapReader reader;
    private final Filter filter;
    private final LayerFourPortRewriter tcpRewriter;
    private final LayerFourPortRewriter udpRewriter;
    private final Dropper dropper;

    private final long expectedPackets;

    public FilterRewriteJoinDisruptorProcessor(
            int bufferSize,
            String source,
            int tcpSrcPort,
            int tcpDestPort,
            int udpSrcPort,
            int udpDestPort,
            long expectedPackets
    ) throws IOException {
        Disruptor<PacketEvent> readerDisruptor = new Disruptor<>(PacketEvent::new, bufferSize, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, new YieldingWaitStrategy());
        Disruptor<PacketEvent> tcpDisruptor = new Disruptor<>(PacketEvent::new, bufferSize, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, new YieldingWaitStrategy());
        Disruptor<PacketEvent> udpDisruptor = new Disruptor<>(PacketEvent::new, bufferSize, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, new YieldingWaitStrategy());
        Disruptor<PacketEvent> rewriterDisruptor = new Disruptor<>(PacketEvent::new, bufferSize, DaemonThreadFactory.INSTANCE, ProducerType.MULTI, new YieldingWaitStrategy());

        this.reader = new PcapReader(source, readerDisruptor);
        this.filter = new Filter(readerDisruptor, tcpDisruptor, udpDisruptor);
        this.tcpRewriter = new LayerFourPortRewriter(tcpDisruptor, rewriterDisruptor, tcpSrcPort, tcpDestPort);
        this.udpRewriter = new LayerFourPortRewriter(udpDisruptor, rewriterDisruptor, udpSrcPort, udpDestPort);
        this.dropper = new Dropper(rewriterDisruptor);

        this.expectedPackets = expectedPackets;
    }

    @Override
    protected List<PcapReader> setReaders() {
        return List.of(reader);
    }

    @Override
    protected List<ProcessorComponent> setComponents() {
        return List.of(filter, tcpRewriter, udpRewriter, dropper);
    }

    @Override
    public boolean shouldTerminate() {
        return dropper.getPacketCount() >= expectedPackets;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        FilterRewriteJoinDisruptorProcessor processor = new FilterRewriteJoinDisruptorProcessor(
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
