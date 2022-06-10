package tech.gordonlee.jmpp.examples;

import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import tech.gordonlee.jmpp.AbstractPacketProcessor;
import tech.gordonlee.jmpp.components.Component;
import tech.gordonlee.jmpp.components.outputters.Dropper;
import tech.gordonlee.jmpp.readers.PcapReader;
import tech.gordonlee.jmpp.readers.Reader;
import tech.gordonlee.jmpp.utils.PacketEvent;

import java.io.IOException;
import java.util.List;

public class MultipleConsumerProcessor extends AbstractPacketProcessor {

    private final PcapReader reader;
    private final Dropper consumerOne;
    private final Dropper consumerTwo;
    private final Dropper consumerThree;

    private final long expectedPackets;

    public MultipleConsumerProcessor(int bufferSize, String source, long expectedPackets) throws IOException {
        Disruptor<PacketEvent> readerDisruptor = new Disruptor<>(PacketEvent::new, bufferSize, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, new YieldingWaitStrategy());

        this.reader = new PcapReader(source, readerDisruptor);
        this.consumerOne = new Dropper(readerDisruptor);
        this.consumerTwo = new Dropper(readerDisruptor);
        this.consumerThree = new Dropper(readerDisruptor);

        this.expectedPackets = expectedPackets;
    }

    @Override
    protected List<Reader> setReaders() {
        return List.of(reader);
    }

    @Override
    protected List<Component> setComponents() {
        return List.of(consumerOne, consumerTwo, consumerThree);
    }

    @Override
    protected boolean shouldTerminate() {
        return consumerOne.getPacketCount() == expectedPackets &&
                consumerTwo.getPacketCount() == expectedPackets &&
                consumerThree.getPacketCount() == expectedPackets;
    }

}
