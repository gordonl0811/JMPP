package tech.gordonlee.jmpp.components;

import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import io.pkts.Pcap;
import io.pkts.packet.TransportPacket;
import io.pkts.protocol.Protocol;
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

public class PortRewriterTest {

    private final int testSrcPort = 99;
    private final int testDstPort = 99;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test(timeout = 3000)
    public void testRewritesAddressesCorrectly() throws IOException, InterruptedException {

        File outputPcap = folder.newFile("output.pcap");

        Disruptor<PacketEvent> readerDisruptor = new Disruptor<>(PacketEvent::new, BUFFER_SIZE, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, new BusySpinWaitStrategy());
        Disruptor<PacketEvent> rewriterDisruptor = new Disruptor<>(PacketEvent::new, BUFFER_SIZE, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, new BusySpinWaitStrategy());

        Reader reader = new PcapReader(MULTIPLE_PACKET_PCAP.getAbsolutePath(), readerDisruptor);
        PortRewriter rewriter = new PortRewriter(readerDisruptor, rewriterDisruptor, testSrcPort, testDstPort);
        Writer writer = new Writer(rewriterDisruptor, outputPcap.getAbsolutePath());

        reader.initialize();
        rewriter.initialize();
        writer.initialize();

        reader.start();

        while (writer.getPacketCount() != MULTIPLE_PACKET_PCAP_COUNT) {
            TimeUnit.MILLISECONDS.sleep(1);
        }

        reader.shutdown();
        rewriter.shutdown();
        writer.shutdown();

        // Check that the packet addresses match the changed output
        Pcap generatedPcap = Pcap.openStream(outputPcap);
        generatedPcap.loop(packet -> {
            Protocol layerFourProtocol = packet.hasProtocol(Protocol.TCP) ? Protocol.TCP : Protocol.UDP;
            TransportPacket layerFourPacket = (TransportPacket) packet.getPacket(layerFourProtocol);
            assert layerFourPacket.getSourcePort() == testSrcPort;
            assert layerFourPacket.getDestinationPort() == testDstPort;
            return true;
        });

    }
}
