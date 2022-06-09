package tech.gordonlee.jmpp.processors;

import org.junit.Test;
import tech.gordonlee.jmpp.MultipleConsumerProcessor;
import tech.gordonlee.jmpp.PacketProcessor;

import java.io.IOException;

import static tech.gordonlee.jmpp.components.utils.TestUtils.*;

public class MultipleConsumerProcessorTest {

    @Test
    public void testMultipleConsumerProcessor() throws IOException, InterruptedException {
        PacketProcessor processor = new MultipleConsumerProcessor(BUFFER_SIZE, HUNDRED_PACKET_PCAP.getAbsolutePath(), HUNDRED_PACKET_PCAP_COUNT);
        processor.initialize();
        processor.start();
        processor.shutdown();
        // Successful termination indicates that each consumer processed their set of packets
    }
}
