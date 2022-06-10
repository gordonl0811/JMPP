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
import java.util.Arrays;
import java.util.List;

public class MultipleProducerProcessor extends AbstractPacketProcessor {

    private final PcapReader readerOne;
    private final PcapReader readerTwo;
    private final PcapReader readerThree;
    private final Dropper dropper;

    private final long expectedPackets;

    public MultipleProducerProcessor(int bufferSize, String sourceOne, String sourceTwo, String sourceThree, int expectedPackets) throws IOException {
        Disruptor<PacketEvent> sharedDisruptor = new Disruptor<>(PacketEvent::new, bufferSize, DaemonThreadFactory.INSTANCE, ProducerType.MULTI, new YieldingWaitStrategy());

        this.readerOne = new PcapReader(sourceOne, sharedDisruptor);
        this.readerTwo = new PcapReader(sourceTwo, sharedDisruptor);
        this.readerThree = new PcapReader(sourceThree, sharedDisruptor);
        this.dropper = new Dropper(sharedDisruptor);

        this.expectedPackets = expectedPackets;

    }

    @Override
    protected List<Reader> setReaders() {
        return Arrays.asList(readerOne, readerTwo, readerThree);
    }

    @Override
    protected List<Component> setComponents() {
        return List.of(dropper);
    }

    @Override
    protected boolean shouldTerminate() {
        return dropper.getPacketCount() >= expectedPackets;
    }

}
