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

public class WriterTest {

    private final File singlePacketPcap = new File("src/test/resources/inputs/input_1.pcap");
    private final File multiplePacketPcap = new File("src/test/resources/inputs/input_10.pcap");
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test(timeout = 3000)
    public void WriterCopiesSinglePacketExactly() throws IOException, InterruptedException {
        File outputPcap = folder.newFile("output.pcap");
        Disruptor<PacketEvent> readerDisruptor = new Disruptor<>(PacketEvent::new, 1, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, new BusySpinWaitStrategy());
        Reader reader = new PcapReader(singlePacketPcap.getAbsolutePath(), readerDisruptor);
        Writer writer = new Writer(readerDisruptor, outputPcap.getAbsolutePath());

        reader.initialize();
        writer.initialize();

        reader.start();

        while (writer.getPacketCount() != 1) {
            TimeUnit.MILLISECONDS.sleep(1);
        }

        reader.shutdown();
        writer.shutdown();

        assert(FileUtils.contentEquals(singlePacketPcap, outputPcap));

    }

    @Test(timeout = 3000)
    public void WriterHandlesMultiplePackets() throws IOException, InterruptedException {
        File outputPcap = folder.newFile("output.pcap");
        Disruptor<PacketEvent> readerDisruptor = new Disruptor<>(PacketEvent::new, 10, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, new BusySpinWaitStrategy());
        Reader reader = new PcapReader(multiplePacketPcap.getAbsolutePath(), readerDisruptor);
        Writer writer = new Writer(readerDisruptor, outputPcap.getAbsolutePath());

        reader.initialize();
        writer.initialize();

        reader.start();

        while (writer.getPacketCount() != 10) {
            TimeUnit.MILLISECONDS.sleep(1);
        }

        reader.shutdown();
        writer.shutdown();

        assert(FileUtils.contentEquals(multiplePacketPcap, outputPcap));
    }
}
