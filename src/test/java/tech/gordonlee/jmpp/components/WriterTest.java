package tech.gordonlee.jmpp.components;

import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import tech.gordonlee.jmpp.readers.PcapReader;
import tech.gordonlee.jmpp.readers.Reader;
import tech.gordonlee.jmpp.utils.PacketEvent;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static tech.gordonlee.jmpp.components.utils.TestUtils.*;

public class WriterTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test(timeout = 3000)
    public void testWriterCopiesSinglePacketExactly() throws IOException, InterruptedException {
        File outputPcap = folder.newFile("output.pcap");
        Disruptor<PacketEvent> readerDisruptor = new Disruptor<>(PacketEvent::new, BUFFER_SIZE, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, new BusySpinWaitStrategy());
        Reader reader = new PcapReader(SINGLE_PACKET_PCAP.getAbsolutePath(), readerDisruptor);
        Writer writer = new Writer(readerDisruptor, outputPcap.getAbsolutePath());

        reader.initialize();
        writer.initialize();

        reader.start();

        while (writer.getPacketCount() != SINGLE_PACKET_PCAP_COUNT) {
            TimeUnit.MILLISECONDS.sleep(1);
        }

        reader.shutdown();
        writer.shutdown();

        assert (FileUtils.contentEquals(SINGLE_PACKET_PCAP, outputPcap));

    }

    @Test(timeout = 3000)
    public void testWriterHandlesMultiplePackets() throws IOException, InterruptedException {
        File outputPcap = folder.newFile("output.pcap");
        Disruptor<PacketEvent> readerDisruptor = new Disruptor<>(PacketEvent::new, BUFFER_SIZE, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, new BusySpinWaitStrategy());
        Reader reader = new PcapReader(MULTIPLE_PACKET_PCAP.getAbsolutePath(), readerDisruptor);
        Writer writer = new Writer(readerDisruptor, outputPcap.getAbsolutePath());

        reader.initialize();
        writer.initialize();

        reader.start();

        while (writer.getPacketCount() != MULTIPLE_PACKET_PCAP_COUNT) {
            TimeUnit.MILLISECONDS.sleep(1);
        }

        reader.shutdown();
        writer.shutdown();

        assert (FileUtils.contentEquals(MULTIPLE_PACKET_PCAP, outputPcap));
    }
}
