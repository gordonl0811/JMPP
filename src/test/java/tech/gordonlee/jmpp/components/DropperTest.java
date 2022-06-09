package tech.gordonlee.jmpp.components;

import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import org.junit.Test;
import tech.gordonlee.jmpp.readers.PcapReader;
import tech.gordonlee.jmpp.readers.Reader;
import tech.gordonlee.jmpp.utils.PacketEvent;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static tech.gordonlee.jmpp.components.utils.TestUtils.MULTIPLE_PACKET_PCAP;
import static tech.gordonlee.jmpp.components.utils.TestUtils.MULTIPLE_PACKET_PCAP_COUNT;

public class DropperTest {

    @Test(timeout = 3000)
    public void DropperCountsPacketsCorrectly() throws IOException, InterruptedException {

        Disruptor<PacketEvent> readerDisruptor = new Disruptor<>(PacketEvent::new, 1, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, new BusySpinWaitStrategy());
        Reader reader = new PcapReader(MULTIPLE_PACKET_PCAP.getAbsolutePath(), readerDisruptor);
        Dropper dropper = new Dropper(readerDisruptor);

        reader.initialize();
        dropper.initialize();

        reader.start();

        // Test will be successful if it terminates
        while (dropper.getPacketCount() != MULTIPLE_PACKET_PCAP_COUNT) {
            TimeUnit.MILLISECONDS.sleep(1);
        }

        reader.shutdown();
        dropper.shutdown();

    }
}
