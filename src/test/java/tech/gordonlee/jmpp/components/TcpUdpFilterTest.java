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

import static tech.gordonlee.jmpp.components.utils.TestUtils.*;

public class TcpUdpFilterTest {

    @Test(timeout = 3000)
    public void testFilterSortsPacketsCorrectly() throws IOException, InterruptedException {

        Disruptor<PacketEvent> readerDisruptor = new Disruptor<>(PacketEvent::new, BUFFER_SIZE, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, new BusySpinWaitStrategy());
        Disruptor<PacketEvent> tcpDisruptor = new Disruptor<>(PacketEvent::new, BUFFER_SIZE, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, new BusySpinWaitStrategy());
        Disruptor<PacketEvent> udpDisruptor = new Disruptor<>(PacketEvent::new, BUFFER_SIZE, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, new BusySpinWaitStrategy());


        Reader reader = new PcapReader(MULTIPLE_PACKET_PCAP.getAbsolutePath(), readerDisruptor);
        TcpUdpFilter filter = new TcpUdpFilter(readerDisruptor, tcpDisruptor, udpDisruptor);
        Dropper tcpDropper = new Dropper(tcpDisruptor);
        Dropper udpDropper = new Dropper(udpDisruptor);

        reader.initialize();
        filter.initialize();
        tcpDropper.initialize();
        udpDropper.initialize();

        reader.start();

        // Test will be successful if it terminates
        while (tcpDropper.getPacketCount() != TCP_PACKET_COUNT && udpDropper.getPacketCount() != UDP_PACKET_COUNT) {
            TimeUnit.MILLISECONDS.sleep(1);
        }

        reader.shutdown();
        filter.shutdown();
        tcpDropper.shutdown();
        udpDropper.shutdown();

    }
}
