package tech.gordonlee.jmpp;

import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import tech.gordonlee.jmpp.components.Component;
import tech.gordonlee.jmpp.components.Dropper;
import tech.gordonlee.jmpp.components.PortRewriter;
import tech.gordonlee.jmpp.readers.PcapReader;
import tech.gordonlee.jmpp.readers.Reader;
import tech.gordonlee.jmpp.utils.PacketEvent;

import java.io.IOException;
import java.util.List;

public class PipelineProcessor extends AbstractPacketProcessor {

    private final PcapReader reader;
    private final PortRewriter rewriter;
    private final Dropper dropper;

    private final long expectedPackets;

    public PipelineProcessor(int bufferSize, String source, int srcPort, int dstPort, long expectedPackets) throws IOException {

        Disruptor<PacketEvent> readerDisruptor = new Disruptor<>(PacketEvent::new, bufferSize, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, new YieldingWaitStrategy());
        Disruptor<PacketEvent> rewriterDisruptor = new Disruptor<>(PacketEvent::new, bufferSize, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, new YieldingWaitStrategy());

        this.reader = new PcapReader(source, readerDisruptor);
        this.rewriter = new PortRewriter(readerDisruptor, rewriterDisruptor, srcPort, dstPort);
        this.dropper = new Dropper(rewriterDisruptor);

        this.expectedPackets = expectedPackets;
    }

    @Override
    protected List<Reader> setReaders() {
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
