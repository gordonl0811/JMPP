package tech.gordonlee.jmpp;

import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import tech.gordonlee.jmpp.components.Component;
import tech.gordonlee.jmpp.components.Dropper;
import tech.gordonlee.jmpp.components.LayerFourPortRewriter;
import tech.gordonlee.jmpp.sources.PcapReader;
import tech.gordonlee.jmpp.utils.PacketEvent;

import java.io.IOException;
import java.util.List;

public class PipelineDisruptorProcessor extends AbstractPacketProcessor {

    private final PcapReader reader;
    private final LayerFourPortRewriter rewriter;
    private final Dropper dropper;

    private final long expectedPackets;

    public PipelineDisruptorProcessor(int bufferSize, String source, int srcPort, int destPort, long expectedPackets) throws IOException {

        Disruptor<PacketEvent> readerDisruptor = new Disruptor<>(PacketEvent::new, bufferSize, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, new YieldingWaitStrategy());
        Disruptor<PacketEvent> rewriterDisruptor = new Disruptor<>(PacketEvent::new, bufferSize, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, new YieldingWaitStrategy());

        this.reader = new PcapReader(source, readerDisruptor);
        this.rewriter = new LayerFourPortRewriter(readerDisruptor, rewriterDisruptor, srcPort, destPort);
        this.dropper = new Dropper(rewriterDisruptor);

        this.expectedPackets = expectedPackets;
    }

    @Override
    protected List<PcapReader> setReaders() {
        return List.of(reader);
    }

    @Override
    protected List<Component> setComponents() {
        return List.of(rewriter, dropper);
    }

    @Override
    public boolean shouldTerminate() {
        return dropper.getPacketCount() >= expectedPackets;
    }
}
