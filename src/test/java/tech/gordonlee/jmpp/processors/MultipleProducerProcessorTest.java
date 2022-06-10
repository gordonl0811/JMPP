package tech.gordonlee.jmpp.processors;

import org.junit.Test;
import tech.gordonlee.jmpp.examples.MultipleProducerProcessor;
import tech.gordonlee.jmpp.PacketProcessor;

import java.io.IOException;

import static tech.gordonlee.jmpp.components.utils.TestUtils.*;

public class MultipleProducerProcessorTest {

    @Test
    public void testMultipleProducerProcessor() throws IOException, InterruptedException {
        PacketProcessor processor = new MultipleProducerProcessor(
                BUFFER_SIZE,
                HUNDRED_PACKET_PCAP_COPY_1.getAbsolutePath(),
                HUNDRED_PACKET_PCAP_COPY_2.getAbsolutePath(),
                HUNDRED_PACKET_PCAP_COPY_3.getAbsolutePath(),
                HUNDRED_PACKET_PCAP_COUNT * 3
        );

        processor.initialize();
        processor.start();
        processor.shutdown();
        // Successful termination indicates that each consumer processed their set of packets
    }

}
